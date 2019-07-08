package org.lili.forfun.infra.util;

import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.JSONWriter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Map;


public class JsonUtils {
    public static Map<String, Object> json2Map(String jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(jsonFile), new TypeReference<Map<String, Object>>() {
        });
    }

    public static void saveMap(Map<String, Object> map, String jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(jsonFile), map);
    }

    public static <T> T readJsonFile(String jsonFile, Class<T> c) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File jsonInputFile = new File(jsonFile);
        return mapper.readValue(jsonInputFile, c);
    }

    public static void save(Object object, String jsonFilePath) throws IOException {
        try (JSONWriter jsonWriter = new JSONWriter(new FileWriter(new File(jsonFilePath)))) {
            jsonWriter.writeObject(object);
        }
    }

    public static <T> T load(String jsonFilePath) throws FileNotFoundException {
        try (JSONReader reader = new JSONReader(new FileReader(new File(jsonFilePath)))) {
            return reader.readObject(new com.alibaba.fastjson.TypeReference<T>() {});
        }
    }
}