package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisSharedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * @author fan
 * @date 2018/2/24 9:06
 */
@Slf4j
@Component
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private RedissonManager redissonManager;

    @PreDestroy    //表示关键之前一定要进行的操作
    public void delLock() {
        RedisSharedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    }

   // @Scheduled(cron = "0 */1 * * * ?")  //每一分钟（每一分钟的整数倍）
    public void closeOrderTaskV1() {
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
       // iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

   // @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV2() {
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout","5000"));
        //如果不存在设置锁
       Long setnxResult = RedisSharedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis()+lockTimeout));
        if (setnxResult != null && setnxResult.intValue() == 1) {
            //如果返回是1，代表设置成功，获得锁
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else {
            log.info("没有获得分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");
    }


    // @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV3() {
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout","5000"));
        //如果不存在设置锁
        Long setnxResult = RedisSharedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis()+lockTimeout));
        if (setnxResult != null && setnxResult.intValue() == 1) {
            //如果返回是1，代表设置成功，获得锁
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else {
            //未获取到锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = RedisSharedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
                //表示锁已经到有效期
                String getSetResult = RedisSharedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
                //再次使用当前时间戳getset, 返回给定key的旧值  --》旧值判断是否可以获取锁
                //当旧值为null的时候，表示key不存在，直接获取锁
                //旧值和新值相等，表示当前没有其他进程占用锁
                if (getSetResult == null || (getSetResult != null && StringUtils.equals(lockValueStr,getSetResult))) {
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }else {
                    log.info("没有获得分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            }else {
                log.info("没有获得分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        }
        log.info("关闭订单定时任务结束");
    }

    //利用Redisson分布式框架实现分布式锁
    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV4() {

       RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
       boolean getlock = false;
        try {
            //等待时间一定要设置为0，不等待，让每个进程每次公平竞争
            if (getlock = lock.tryLock(0,5,TimeUnit.SECONDS)) {
                log.info("获取到分布式锁:{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
                int hour =  Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
                 iOrderService.closeOrder(hour);
            }else {
                log.info("没有获取到分布式锁:{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
            }

        } catch (InterruptedException e) {
            log.error("Redis 分布式锁获取异常",e);
        }finally {
            if (!getlock) {
                return;
            }
            lock.unlock();
            log.info("redis分布式锁释放");
        }
    }



    private void closeOrder(String lockName) {
        RedisSharedPoolUtil.expire(lockName,50); //设置有效期五秒 ，防止死锁
        log.info("获取锁:{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        //关闭订单操作
        int hour =  Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
       // iOrderService.closeOrder(hour);
        //释放锁
        RedisSharedPoolUtil.del(lockName);
        log.info("释放锁:{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
    }




}
