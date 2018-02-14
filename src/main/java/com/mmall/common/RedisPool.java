package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author fan
 * @date 2018/2/12 22:18
 */
public class RedisPool {
    private static JedisPool pool; //jedis连接池
    //最大连接数
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total","20"));
    //在jedisPool中最大的idel状态（空闲的）jedis实例的个数
    private static Integer maxIdle =Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle","10"));
    //在jedisPool中最小的idel状态（空闲的）jedis实例的个数
    private static Integer minIdle =Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle","2"));
    //在borrow一个实例的时候，是否要进行验证操作，如果赋值为true,则得到的jedis的实例肯定是可以用的
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow","true"));
    //在return 操作的时候，是否要进行验证，如果赋值为true，则放回jedisPool的实例肯定是可以用的
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return","true"));

    private static String redisIp = PropertiesUtil.getProperty("redis.ip");

    private static Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port"));

    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(maxIdle);
        config.setMaxTotal(maxTotal);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
       //连接耗尽的时候是否阻塞，false会抛出异常，true阻塞直到超时，默认为true
        config.setBlockWhenExhausted(true);

        pool = new JedisPool(config,redisIp,redisPort,1000*2);//连接超时时间2秒
    }

    static {
        initPool();
    }

    public static Jedis getJedis() {
        return pool.getResource();
    }

    public static void returnResource(Jedis jedis) {
        pool.returnResource(jedis);
    }

    public static void returnBrokenResource(Jedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    public static void main(String[] args) {
       Jedis jedis = pool.getResource();
       jedis.set("xiaofan","shuaige");
       returnResource(jedis);

       pool.destroy();//销毁连接池
        System.out.println("program is end");
    }


}
