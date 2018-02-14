package com.mmall.util;

import com.mmall.common.RedisPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 * @author fan
 * @date 2018/2/12 23:41
 */

/**
 * 对jedisd的API方法的封装
 */
@Slf4j
public class RedisPoolUtil {

    /**
     *
     * @param key
     * @param exTime 设置key的有效期 单位是秒
     * @return
     */
    public static Long expire(String key,int exTime) {
        Jedis jedis = null;
        Long result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.expire(key,exTime);
        } catch (Exception e) {
            log.error("set key:{} value:{} error",key,e);
            return null;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     *
     * @param key
     * @param value
     * @param exTime  单位是秒
     * @return
     */
    public static String setEx(String key,String value,int exTime) {
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.setex(key,exTime,value);
        } catch (Exception e) {
            log.error("set key:{} value:{} error",key,value,e);
            return null;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static String set(String key,String value) {
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.set(key,value);
        } catch (Exception e) {
            log.error("set key:{} value:{} error",key,value,e);
            return null;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static String get(String key) {
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("set key:{} value:{} error",key,e);
            return null;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static Long del(String key) {
        Jedis jedis = null;
        Long result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("set key:{} value:{} error",key,e);
            return null;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static void main(String[] args) {
        //一定要先跟服务器拿到连接
       Jedis jedis = RedisPool.getJedis();
        RedisPoolUtil.set("key","value");
        String value = RedisPoolUtil.get("key");
        RedisPoolUtil.setEx("test","test",60*10);
        RedisPoolUtil.expire("key",60*60*12);
        RedisPoolUtil.del("key");
    }
}
