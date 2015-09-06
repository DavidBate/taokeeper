package com.taobao.taokeeper.monitor.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.taobao.taokeeper.monitor.core.task.ZooKeeperDelayCollectJob;
import com.taobao.taokeeper.monitor.core.task.ZooKeeperRTCollectJob;

/**
 * 
 * @author pingwei
 * 2014-2-25 下午11:05:17
 */

public class SchedulerManager {
	
	public static ScheduledExecutorService rtScheduler = Executors.newScheduledThreadPool(1);
	public static ScheduledExecutorService delayScheduler = Executors.newScheduledThreadPool(1);
	
	public static void init(){
		rtScheduler.scheduleWithFixedDelay(new ZooKeeperRTCollectJob(), 30, 30, TimeUnit.SECONDS);
		delayScheduler.scheduleWithFixedDelay(new ZooKeeperDelayCollectJob(), 30, 30, TimeUnit.SECONDS);
	}
}


