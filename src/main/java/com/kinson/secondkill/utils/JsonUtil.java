package com.kinson.secondkill.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * Json工具类
 *
 * @author
 * @date
 */
@Slf4j(topic = "JsonUtil")
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 将对象转换成json字符串
     *
     * @param obj
     * @return
     */
    public static String object2JsonStr(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            // 打印异常信息
            log.error("对象转json出现异常", e);
        }

        return null;
    }

    /**
     * 将字符串转换为对象
     *
     * @param <T> 泛型
     */
    public static <T> T jsonStr2Object(String jsonStr, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonStr.getBytes("UTF-8"), clazz);
        } catch (JsonParseException e) {
            log.error("json转对象出现JsonParseException异常", e);
        } catch (JsonMappingException e) {
            log.error("json转对象出现JsonMappingException异常", e);
        } catch (IOException e) {
            log.error("json转对象出现IOException异常", e);
        }

        return null;
    }

    /**
     * 将json数据转换成pojo对象list
     *
     * @param beanType @return
     */
    public static <T> List<T> jsonToList(String jsonStr, Class<T> beanType) {
        JavaType javaType =
                objectMapper.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            List<T> list = objectMapper.readValue(jsonStr, javaType);
            return list;
        } catch (Exception e) {
            log.error("json转List出现异常", e);
        }

        return null;
    }
}
