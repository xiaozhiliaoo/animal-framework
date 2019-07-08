package org.lili.forfun.infra.middleware.mongo;


import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSDownloadOptions;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.lili.forfun.infra.domain.config.FileConfig;
import org.lili.forfun.infra.util.FilesUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static com.mongodb.client.model.Filters.eq;


@Slf4j
public class FileProcessor {
	private static final int BUFFER_SIZE = 8 * 1024;
	private static final int BLOCK_SIZE = 358400;
	public static final String DATABASE_NAME = "mydb";
	public static final String BUCKET_NAME = "my-bucket";

	/**
	 * 指定最大返回数量，最多不能超过1000条
	 */
	private static final int WITH_MAX_KEYS = 1000;

	/**
	 * @param config
	 * @param filePath
	 * @return 0:OK;1:FAILED
	 */
	public static int download(FileConfig config, FilePath filePath, boolean replace) {
		log.info("download(cfg:{}, FilePath={}, replace={})", config.getEndpoint(), filePath, replace);
		try (MongoClient client = buildFileClient(config)) {
			MongoDatabase database = client.getDatabase(DATABASE_NAME);
			GridFSBucket gridFSBucket = GridFSBuckets.create(database, BUCKET_NAME);
			File f = new File(filePath.getLocalPath());
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			} else {
				if (f.exists() && f.length() == FileProcessor.sizeOf(config, filePath.getOssPath())) {
					// 文件已经存在且大小相等，则不下载
					return 1;
				} else {
					try (FileOutputStream streamToDownloadTo = new FileOutputStream(f)) {
						GridFSDownloadOptions downloadOptions = new GridFSDownloadOptions().revision(0);
						gridFSBucket.downloadToStream(filePath.getOssPath(), streamToDownloadTo, downloadOptions);
						streamToDownloadTo.flush();
						streamToDownloadTo.close();
						System.out.println("file size: " + f.length());
						return 0;
					} catch (IOException e) {
						log.error("", e);
					}
				}
			}
			return 1;
		}
	}

	/**
	 * 如果replace为true，则本地已经存在的文件不再下载
	 *
	 * @param config
	 * @param ossFileList
	 * @param replace
	 * @return 成功返回0，失败返回1。
	 */
	public static int download(FileConfig config, List<FilePath> ossFileList, boolean replace) {
		log.debug("download(cfg:{}, ossFileList={}, replace={})", config.getEndpoint(), ossFileList, replace);
		try (MongoClient client = buildFileClient(config)) {
			MongoDatabase database = client.getDatabase(DATABASE_NAME);
			GridFSBucket gridFSBucket = GridFSBuckets.create(database, BUCKET_NAME);

			for (FilePath filePath : ossFileList) {
				if (!replace && Paths.get(filePath.getLocalPath()).toFile().exists()) {
					log.info("continue: fileexists: replace:{}, file: {}", replace, filePath.getLocalPath());
					continue;
				} else if (Paths.get(filePath.getLocalPath()).toFile().isDirectory()) {
					log.info("continue: isDir: replace: {}, dirfile: {}", replace, filePath.getLocalPath());
					continue;
				}
				try (FileOutputStream streamToDownloadTo = new FileOutputStream(filePath.getLocalPath())) {
					GridFSDownloadOptions downloadOptions = new GridFSDownloadOptions().revision(0);
					gridFSBucket.downloadToStream(filePath.getOssPath(), streamToDownloadTo, downloadOptions);
				} catch (IOException e) {
					log.error("", e);
					return 1;
				}
			}
			return 0;
		}
	}

	public static void decompress(String localTar, String localDirectory, boolean isGZipped) throws IOException {
		InputStream inputStream = new FileInputStream(localTar);
		try (TarArchiveInputStream in = (isGZipped) ? new TarArchiveInputStream(new GZIPInputStream(inputStream),
		    BUFFER_SIZE)
		    : new TarArchiveInputStream(new BufferedInputStream(inputStream), BUFFER_SIZE)) {
			TarArchiveEntry entry = in.getNextTarEntry();
			while (entry != null) {
				if (entry.isDirectory()) {
					entry = in.getNextTarEntry();
					continue;
				}
				File curFile = new File(localDirectory, entry.getName());
				File parent = curFile.getParentFile();
				if (!parent.exists()) {
					boolean result = parent.mkdirs();
					log.debug("MkDir result={}", result);
				}
				try (OutputStream out = new FileOutputStream(curFile)) {
					IOUtils.copy(in, out);
				}
				entry = in.getNextTarEntry();
			}
		}
	}

	public static void uploadDir(FileConfig config, String localPath, String ossPath)
	    throws FileNotFoundException {
		log.info("uploadDir(localPath={}, ossPath={})", localPath, ossPath);
		try (MongoClient client = buildFileClient(config)) {
			File f = new File(localPath);
			if (f.isDirectory()) {
				File[] fs = f.listFiles();
				if (fs != null) {
					for (File tmp : fs) {
						uploadDir(config, localPath + "/" + tmp.getName(), ossPath + "/" + tmp.getName());
					}
				}
			} else {
				upload(config, f, ossPath);
			}
		}
	}

	public static void downloadAndUnzip(FileConfig config, String localDirectory, String localTar, String ossTar)
	    throws IOException {
		if (localTar == null || ossTar == null) {
			log.warn("localPath[{}] or ossPath[{}] should not be empty!", localTar, ossTar);
			return;
		}
		if (!(Files.exists(Paths.get(localTar)))) {
			if (0 == download(config, new FilePath(localTar, ossTar), true)) {
				decompress(localTar, localDirectory, true);
			}
		} else if (!(Files.exists(Paths.get(localDirectory)))) {
			decompress(localTar, localDirectory, true);
		}
	}

	public static int downloadToDir(FileConfig config, String localPath, String ossPrefix, boolean replace) {
		log.info("config.endpoint={}, localPath={}, ossPrefix={}, replace={}", config.getEndpoint(), localPath, ossPrefix, replace);
		if (FilesUtil.createDirectoriesIfNotExists(localPath)) {
			return 1;
		}
		if (ossPrefix.endsWith("/")) {
			ossPrefix = ossPrefix.substring(0, ossPrefix.length() - 1);
		}
		if (localPath.endsWith("/")) {
			localPath = localPath.substring(0, localPath.length() - 1);
		}
		try (MongoClient client = buildFileClient(config)) {
			MongoDatabase database = client.getDatabase(DATABASE_NAME);
			GridFSBucket gridFSBucket = GridFSBuckets.create(database, BUCKET_NAME);

			List<FilePath> ossFileList = new ArrayList<>();
			String ossPath = ossPrefix;
			String local = localPath;
			gridFSBucket.find().forEach(
			    (Block<GridFSFile>) gridFSFile -> {
				    String filename = gridFSFile.getFilename();
				    if (filename.startsWith(ossPath + "/")) {
					    ossFileList.add(new FilePath(local + filename.substring(ossPath.length()), filename));
				    }
			    });
			client.close();
			return download(config, ossFileList, replace);
		}
	}

	public static boolean isExist(FileConfig config, String ossPath) {
		List<BsonValue> idList = getFileIds(config, ossPath);
		return !idList.isEmpty();
	}

	private static List<BsonValue> getFileIds(FileConfig config, String ossPath) {
		try (MongoClient client = buildFileClient(config)) {
			MongoDatabase database = client.getDatabase(DATABASE_NAME);
			GridFSBucket gridFSBucket = GridFSBuckets.create(database, BUCKET_NAME);
			List<BsonValue> idList = new ArrayList<>();
			gridFSBucket.find(eq("filename", ossPath)).forEach((Block<GridFSFile>) gridFSFile -> {
				idList.add(gridFSFile.getId());
			});
			return idList;
		}
	}

	public static void upload(FileConfig config, File localfile, String ossfile) throws FileNotFoundException {
		log.info("upload(localfile={}, ossfile={}, localfile.size={})", localfile, ossfile, localfile.length());
		upload(config, new FileInputStream(localfile), ossfile);
	}

	public static void upload(FileConfig config, InputStream streamToUploadFrom, String ossPath) {
		log.info("upload(cfg:{}, is=..., ossPath={}", config.getEndpoint(), ossPath);
		try (MongoClient client = buildFileClient(config)) {
			MongoDatabase database = client.getDatabase(DATABASE_NAME);
			GridFSBucket gridFSBucket = GridFSBuckets.create(database, BUCKET_NAME);
			// Create some custom options
			GridFSUploadOptions options = new GridFSUploadOptions()
			    .chunkSizeBytes(BLOCK_SIZE)
			    .metadata(new Document("type", "presentation"));
			List<BsonValue> idList = getFileIds(config, ossPath);
			for (BsonValue bv : idList) {
				gridFSBucket.delete(bv);
			}
			ObjectId fileId = gridFSBucket.uploadFromStream(ossPath, streamToUploadFrom, options);
			log.info("Stored success {}, id={}, localfile.size()={}, ossfile.size()={}", ossPath, fileId, sizeOf(config, ossPath));
		}
	}

	/**
	 * 删除指定文件
	 *
	 * @param config
	 * @param ossPath
	 * @return
	 */
	public static boolean deleteFile(FileConfig config, String ossPath) {
		log.info("deleteFile(cfg:{}, path={})", config.getEndpoint(), ossPath);
		try (MongoClient client = buildFileClient(config)) {
			MongoDatabase database = client.getDatabase(DATABASE_NAME);
			GridFSBucket gridFSBucket = GridFSBuckets.create(database, BUCKET_NAME);
			List<BsonValue> idList = getFileIds(config, ossPath);
			for (BsonValue bv : idList) {
				gridFSBucket.delete(bv);
			}
			return true;
		}
	}

	/**
	 * 查看文件最后更新时间。
	 *
	 * @param config
	 * @param filepath
	 * @return 如果文件不存在则返回空;否则返回文件最后修改时间
	 */
	public static Date getLastModify(FileConfig config, String filepath) {
		try (MongoClient client = buildFileClient(config)) {
			DB db = client.getDB(DATABASE_NAME);
			GridFS gridFS = new GridFS(db, BUCKET_NAME);
			DBObject query = new BasicDBObject("filename", filepath);
			log.info("filename: {}", query.get("filename"));
			GridFSDBFile gridFSDBFile = gridFS.findOne(query);
			if (gridFSDBFile == null) {
				return null;
			} else {
				return gridFSDBFile.getUploadDate();
			}
		}
	}

	/**
	 * 查找某一目录下的所有文件
	 *
	 * @param config
	 * @param ossPath
	 * @return
	 */
	public static List<String> listFile(FileConfig config, String ossPath) {
		try (MongoClient client = buildFileClient(config)) {
			MongoDatabase database = client.getDatabase(DATABASE_NAME);
			GridFSBucket gridFSBucket = GridFSBuckets.create(database, BUCKET_NAME);
			List<String> ossFileList = new ArrayList<>();
			gridFSBucket.find().forEach(
			    (Block<GridFSFile>) gridFSFile -> {
				    String filename = gridFSFile.getFilename();
				    if (filename.startsWith(ossPath)) {
					    ossFileList.add(filename);
				    }
			    });
			client.close();
			return ossFileList;
		}
	}

	private static MongoClient buildFileClient(FileConfig config) {
		return getMongoClient(config.getEndpoint(), 27017);
	}

	private static MongoClient getMongoClient(String endpoints, int port) {
		log.debug("getMongoClients(endpoints={}, port={})", endpoints, port);
		if (endpoints.startsWith("mongodb://")) {
			MongoClientURI uri = new MongoClientURI(endpoints);
			return new MongoClient(uri);
		} else {
			String[] eps = endpoints.split(",");
			List<ServerAddress> serverAddressList = new ArrayList<>();
			for (String ep : eps) {
				if (ep.indexOf(":") > 0) {
					String[] ss = ep.split(":");
					serverAddressList.add(new ServerAddress(ss[0], Integer.parseInt(ss[1])));
				} else {
					serverAddressList.add(new ServerAddress(ep, port));
				}
			}
			return new MongoClient(serverAddressList);
		}
	}

	/**
	 * 查看oss文件的meta长度信息。
	 *
	 * @param config
	 * @param ossFile
	 */
	public static Long getContentLength(FileConfig config, String ossFile) {
		return sizeOf(config, ossFile);
	}

	public static long sizeOf(FileConfig config, String filepath) {
		try (MongoClient client = buildFileClient(config)) {
			DB db = client.getDB(DATABASE_NAME);
			// GridFSBucket gridFSBucket = GridFSBuckets.create(database, BUCKET_NAME);
			// List<BsonValue> idList = getFileIds(config, filepath);
			GridFS gridFS = new GridFS(db, BUCKET_NAME);
			DBObject query = new BasicDBObject("filename", filepath);
			// log.info("filename: {}", query.get("filename"));
			GridFSDBFile gridFSDBFile = gridFS.findOne(query);
			if (gridFSDBFile == null) {
				return 0;
			} else {
				return gridFSDBFile.getLength();
			}
		}
	}

	public static boolean downloadWithCheckPoint(FileConfig config, FilePath filePath) {
		return downloadWithCheckPoint(config, filePath, true);
	}

	public static boolean downloadWithCheckPoint(FileConfig config, FilePath filePath, boolean replace) {
		return download(config, filePath, replace) == 0;
	}
}
