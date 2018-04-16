package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissionManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisSharedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * Created by 53254 on 2018/3/18 13:14 /mmal
 */
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private RedissionManager redissionManager;

    @PreDestroy
    public void delLock() {
        RedisSharedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    }

    //每1分钟
    //@Scheduled(cron = "0 */1 * * * ?")
    //public void closeOrderTaskV1() {
    //    log.info("关闭订单定时任务启动");
    //    int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
    //    iOrderService.closeOrder(hour);
    //    log.info("关闭订单定时任务结束");
    //}
    //
    ////@Scheduled(cron = "0 */1 * * * ?")
    //public void closeOrderTaskV2() {
    //    log.info("关闭订单定时任务启动");
    //    long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));
    //    Long setnxResult = RedisSharedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
    //    if (setnxResult != null && setnxResult.intValue() == 1) {
    //        closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    //    } else {
    //        log.info("没有获得分布式锁:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    //    }
    //    log.info("关闭订单定时任务结束");
    //}
    //
    ////@Scheduled(cron = "0 */1 * * * ?")
    //public void closeOrderTaskV3() {
    //    log.info("关闭订单定时任务启动");
    //    long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));
    //    Long setnxResult = RedisSharedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
    //    if (setnxResult != null && setnxResult.intValue() == 1) {
    //        closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    //    } else {
    //        //未获取到锁，继续判断
    //        String lockValueStr = RedisSharedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    //        if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
    //            String getSetResult = RedisSharedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
    //            //再次判断
    //            if (getSetResult == null || (getSetResult != null && StringUtils.equals(lockValueStr, getSetResult))) {
    //                //真正获取到锁
    //                closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    //            } else {
    //                log.info("没有获得分布式锁:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    //            }
    //        } else {
    //            log.info("没有获得分布式锁:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    //        }
    //    }
    //    log.info("关闭订单定时任务结束");
    //}

    private void closeOrder(String lockName) {
        RedisSharedPoolUtil.expire(lockName, 5);//防止死锁
        log.info("获取:{},ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        iOrderService.closeOrder(hour);
        RedisSharedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        log.info("释放:{},ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        log.info("=====================================");
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV4() {
        RLock lock = redissionManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        boolean getLock = false;
        try {
            if (getLock = lock.tryLock(0, 50, TimeUnit.SECONDS)) {
                log.info("Redission获取:{},ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
                iOrderService.closeOrder(hour);
            } else {
                log.info("Redission未获取:{},ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.error("Redsssion获取锁异常", e);
            e.printStackTrace();
        } finally {
            if (!getLock) {
                return;
            }
            lock.unlock();
            log.error("redsssion锁释放");
        }
    }
}
