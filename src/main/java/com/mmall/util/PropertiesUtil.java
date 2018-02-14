package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 加载资源文件的工具类
 * @author fan
 * @date 2018/1/25 22:10
 */
@Slf4j
public class PropertiesUtil {

    private static Properties props;

    //静态代码块，在class执行之前就执行了，并且只执行一次，一般用作静态成员的初始化，执行的优先顺序>普通代码块>构造器代码块
    static {
        //要加载的资源文件
        String fileName = "mmall.properties";
        props = new Properties();
        try {
            props.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),"utf-8"));
        } catch (IOException e) {
            log.error("配置文件读取异常",e);
        }
    }

    public static String getProperty(String key){
        String value = props.getProperty(key.trim());
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    public static String getProperty(String key,String defaultValue){
        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            value = defaultValue;
        }
        return value.trim();
    }
}
