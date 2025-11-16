package com.diom.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * JSON 工具类
 */
public class JsonUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 配置 ObjectMapper
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        OBJECT_MAPPER.setTimeZone(TimeZone.getTimeZone("GMT+8"));
    }

    private JsonUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Object to JSON failed", e);
            throw new RuntimeException("Object to JSON failed", e);
        }
    }

    /**
     * 对象转格式化的 JSON 字符串
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Object to pretty JSON failed", e);
            throw new RuntimeException("Object to pretty JSON failed", e);
        }
    }

    /**
     * JSON 字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON to object failed", e);
            throw new RuntimeException("JSON to object failed", e);
        }
    }

    /**
     * JSON 字符串转对象（支持泛型）
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON to object failed", e);
            throw new RuntimeException("JSON to object failed", e);
        }
    }

    /**
     * 对象转换（通过 JSON 序列化/反序列化）
     */
    public static <T> T convert(Object obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }
        try {
            String json = toJson(obj);
            return fromJson(json, clazz);
        } catch (Exception e) {
            log.error("Object convert failed", e);
            throw new RuntimeException("Object convert failed", e);
        }
    }

    /**
     * 获取 ObjectMapper 实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}

