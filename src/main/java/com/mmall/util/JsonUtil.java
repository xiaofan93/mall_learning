package com.mmall.util;

import com.google.common.collect.Lists;
import com.mmall.pojo.TestPojo;
import com.mmall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author fan
 * @date 2018/2/13 18:36
 */
@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static{
        //对象的所有字段全部列入
        objectMapper.setSerializationInclusion(Inclusion.ALWAYS);

        //取消默认装换timestamps形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,false);

        //忽略空bean转json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,false);

        //所有的日期格式统一转换成以下格式  yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        //忽略在Json字符串中存在，但是在Java对象中属性不存在的现象，防止报错
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    public static <T> String obj2String(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Parse Object to String error",e);
            return null;
        }
    }

    public static <T> String obj2StringPretty(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Parse Object to String error",e);
            return null;
        }
    }

    public static <T> T String2Obj(String str,Class<T> clazz) {
        if (StringUtils.isEmpty(str) || clazz == null) {
            return null;
        }
        try {
            return clazz.equals(String.class)? (T) str : objectMapper.readValue(str,clazz);
        } catch (Exception e) {
            log.error("Parse String to Object error",e);
            return null;
        }
    }

    public static <T> T String2Obj(String str, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }
        try {
            return (T) (typeReference.getType().equals(String.class)? (T) str : objectMapper.readValue(str,typeReference));
        } catch (Exception e) {
            log.error("Parse Object to String error",e);
            return null;
        }
    }

    public static <T> T String2Obj(String str,Class<?> collectionClass,Class<?>... elementClass) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClass);
        try {
            return objectMapper.readValue(str,javaType);
        } catch (IOException e) {
            log.error("Parse Object to String error",e);
            return null;
        }
    }


    public static void main(String[] args) {
       /* User u1 = new User();
        u1.setId(11);
        u1.setUsername("xiaofan");
        u1.setCreateTime(new Date());
        String userJson2 = JsonUtil.obj2String(u1);
        log.info("userJson2:{}",userJson2);*/

      TestPojo testPojo = new TestPojo();
       testPojo.setId(2);
       testPojo.setName("tom");
        String testPojoval = JsonUtil.obj2String(testPojo);
        log.info("testPojoval:{}",testPojoval);

        String json = "{\"id\":\"2\",\"color\":\"red\",\"name\":\"tom\"}";
        JsonUtil.String2Obj(json,TestPojo.class);


      /*  User u2 = new User();
        u2.setId(2);
        u2.setUsername("geely");
        String userJson2 = JsonUtil.obj2String(u1);
        log.info(userJson2);
        String userJson = JsonUtil.obj2StringPretty(u1);
        log.info(userJson);

        User user = JsonUtil.String2Obj(userJson2,User.class);

        List<User> userList = Lists.newArrayList();
        userList.add(u1);
        userList.add(u2);
        //序列化集合
        String userlistJson = JsonUtil.obj2StringPretty(userList);*/

        log.info("=============================");

        //反序列化集合

      // List<User> listUserObj = JsonUtil.String2Obj(userlistJson,List.class);
       /* List<User> userListObj1 = JsonUtil.String2Obj(userlistJson, new TypeReference<List<User>>() {
        });*/

       //List<User> userListObj2 = JsonUtil.String2Obj(userlistJson,List.class,User.class);



    }


}
