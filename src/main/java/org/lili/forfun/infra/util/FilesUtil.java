package org.lili.forfun.infra.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.Objects;

/**
 * https://github.com/bbejeck/Java-7/blob/master/src/main/java/bbejeck/nio/util/DirUtils.java
 *
 */
@Slf4j
public class FilesUtil {
    private FilesUtil() {
    }

    public static void clean(Path path) throws IOException {
        validate(path);
        Files.walkFileTree(path, new CleanDirVisitor());
    }

    /**
     * Walks file tree starting at the given path and deletes all files
     * but leaves the directory structure intact. If the given Path does not exist nothing
     * is done.
     *
     * @param path
     * @throws IOException
     */
    public static void cleanIfExists(Path path) throws IOException {
        if (Files.exists(path)) {
            validate(path);
            Files.walkFileTree(path, new CleanDirVisitor());
        }
    }

    /**
     * Completely removes given file tree starting at and including the given path.
     *
     * @param path
     * @throws IOException
     */
    public static void delete(Path path) throws IOException {
        validate(path);
        Files.walkFileTree(path, new DeleteDirVisitor());
    }

    /**
     * If the path exists, completely removes given file tree starting at and including the given path.
     *
     * @param path
     * @throws IOException
     */
    public static void deleteIfExists(Path path) throws IOException {
        if (Files.exists(path)) {
            validate(path);
            Files.walkFileTree(path, new DeleteDirVisitor());
        }
    }

    /**
     * Copies a directory tree
     *
     * @param from
     * @param to
     * @throws IOException
     */
    public static void copy(Path from, Path to) throws IOException {
        validate(from);
        Files.walkFileTree(from, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
            new CopyDirVisitor(from, to));
    }

    /**
     * Moves one directory tree to another.  Not a true move operation in that the
     * directory tree is copied, then the original directory tree is deleted.
     *
     * @param from
     * @param to
     * @throws IOException
     */
    public static void move(Path from, Path to) throws IOException {
        validate(from);
        Files.walkFileTree(from, new CopyDirVisitor(from, to));
        Files.walkFileTree(from, new DeleteDirVisitor());
    }

    /**
     * Traverses the directory structure and applies the given function to each file
     *
     * @param target
     * @param function
     * @throws IOException
     */
    public static void apply(Path target, Function<Path, FileVisitResult> function) throws IOException {
        validate(target);
        Files.walkFileTree(target, new FunctionVisitor(function));
    }

    /**
     * Traverses the directory structure and will only copy sub-tree structures where the provided predicate is true
     *
     * @param from
     * @param to
     * @param predicate
     * @throws IOException
     */

    public static void copyWithPredicate(Path from, Path to, Predicate<Path> predicate) throws IOException {
        validate(from);
        Files.walkFileTree(from, new CopyPredicateVisitor(from, to, predicate));
    }

    /**
     * create directory when the directory is not exist.
     *
     * @param path
     * @return
     */
    public static boolean createDirectoriesIfNotExists(String path) {
        if (Files.exists(Paths.get(path))) {
            return false;
        }

        if (path.endsWith(File.separator)) {
            try {
                Files.createDirectories(Paths.get(path));
            } catch (IOException e) {
                log.error("createDirectories {} failed , error : {}", path, e);
                return true;
            }
        }

        return false;
    }

    private static void validate(Path... paths) {
        for (Path path : paths) {
            Objects.requireNonNull(path);
            if (!Files.isDirectory(path)) {
                throw new IllegalArgumentException(String.format("%s is not a directory", path.toString()));
            }
        }
    }
}

class CleanDirVisitor extends SimpleFileVisitor<Path> {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }
}

class FunctionVisitor extends SimpleFileVisitor<Path> {

    final Function<Path, FileVisitResult> pathFunction;

    public FunctionVisitor(Function<Path, FileVisitResult> pathFunction) {
        this.pathFunction = pathFunction;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return pathFunction.apply(file);
    }
}

class CopyPredicateVisitor extends SimpleFileVisitor<Path> {

    private final Path fromPath;
    private final Path toPath;
    private final Predicate<Path> copyPredicate;

    public CopyPredicateVisitor(Path fromPath, Path toPath, Predicate<Path> copyPredicate) {
        this.fromPath = fromPath;
        this.toPath = toPath;
        this.copyPredicate = copyPredicate;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (copyPredicate.apply(dir)) {
            Path targetPath = toPath.resolve(fromPath.relativize(dir));
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.copy(file, toPath.resolve(fromPath.relativize(file)));
        return FileVisitResult.CONTINUE;
    }
}

class CopyDirVisitor extends SimpleFileVisitor<Path> {

    private final Path fromPath;
    private final Path toPath;
    private final StandardCopyOption copyOption;

    public CopyDirVisitor(Path fromPath, Path toPath, StandardCopyOption copyOption) {
        this.fromPath = fromPath;
        this.toPath = toPath;
        this.copyOption = copyOption;
    }

    public CopyDirVisitor(Path fromPath, Path toPath) {
        this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

        Path targetPath = toPath.resolve(fromPath.relativize(dir));
        if (!Files.exists(targetPath)) {
            Files.createDirectory(targetPath);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
        return FileVisitResult.CONTINUE;
    }
}

class DeleteDirVisitor extends SimpleFileVisitor<Path> {

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc == null) {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
        throw exc;
    }
}

