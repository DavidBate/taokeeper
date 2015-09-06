package com.taobao.taokeeper.monitor.core.task.runable;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.taokeeper.common.util.ZKDataUtil;
import com.taobao.taokeeper.model.SrvrInfo;

/**
 * 
 * @author pingwei
 * 2014-2-26 下午1:17:07
 */

public class ZKServerDelayCollector implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(ZKServerDelayCollector.class);
	String ip;
	int port;
	SrvrInfo srvr;
	CountDownLatch latch;
	
	
	public ZKServerDelayCollector(String ip, int port, SrvrInfo srvr, CountDownLatch latch) {
		super();
		this.ip = ip;
		this.port = port;
		this.srvr = srvr;
		this.latch = latch;
	}


	@Override
	public void run() {
		try {
			if(!ZKDataUtil.ruok(ip, port)){
				log.error(ip + ":" + port + " is not ok.");
				return ;
			}
			ZKDataUtil.srvr(ip, port, srvr);
		} catch (Exception e) {
		} finally{
			latch.countDown();
		}
	}

}


