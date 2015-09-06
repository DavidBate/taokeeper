package com.taobao.taokeeper.monitor.core.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.taobao.taokeeper.common.GlobalInstance;
import com.taobao.taokeeper.dao.ZooKeeperClusterDAO;
import com.taobao.taokeeper.model.ZooKeeperCluster;
import com.taobao.taokeeper.model.ZooKeeperRTInfo;
import com.taobao.taokeeper.monitor.core.ThreadPoolManager;
import com.taobao.taokeeper.monitor.core.task.runable.ZKServerRTCollector;
import common.toolkit.java.exception.DaoException;
import common.toolkit.java.util.DateUtil;

/**
 * 
 * @author pingwei 2014-2-25 下午10:48:03
 */

public class ZooKeeperRTCollectJob implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(ZooKeeperRTCollectJob.class);
	static boolean running = false;

	@Override
	public void run() {
		if (!GlobalInstance.need_zk_rt_collect) {
			return;
		}
		if(running){
			log.warn("ZooKeeperRTCollectJob is running");
			return ;
		}
		running = true;
		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
		ZooKeeperClusterDAO zooKeeperClusterDAO = (ZooKeeperClusterDAO) wac.getBean("zooKeeperClusterDAO");
		try {
			List<ZooKeeperCluster> zooKeeperClusterSet = null;
			Map<Integer, ZooKeeperCluster> zooKeeperClusterMap = GlobalInstance.getAllZooKeeperCluster();
			if (null == zooKeeperClusterMap) {
				zooKeeperClusterSet = zooKeeperClusterDAO.getAllDetailZooKeeperCluster();
			} else {
				zooKeeperClusterSet = new ArrayList<ZooKeeperCluster>();
				zooKeeperClusterSet.addAll(zooKeeperClusterMap.values());
			}

			if (null == zooKeeperClusterSet || zooKeeperClusterSet.isEmpty()) {
				log.warn("No zookeeper cluster");
			} else {
				for (ZooKeeperCluster zookeeperCluster : zooKeeperClusterSet) { // 对每个cluster处理
					if (null != zookeeperCluster && null != zookeeperCluster.getServerList()) {
						CountDownLatch latch = new CountDownLatch(zookeeperCluster.getServerList().size());
						Map<String, ZooKeeperRTInfo> map = new HashMap<String, ZooKeeperRTInfo>();
						for (String server : zookeeperCluster.getServerList()) {
							ZooKeeperRTInfo rtInfo = new ZooKeeperRTInfo();
							map.put(server, rtInfo);
							ThreadPoolManager.zooKeeperRTCollectorExecutor.execute(new ZKServerRTCollector(server,
									zookeeperCluster.getClusterId(), latch, rtInfo));
						}
						latch.await();
						GlobalInstance.rtInfoMap.put(zookeeperCluster.getClusterId(), map);
					}
				}
			}
			GlobalInstance.timeOfUpdateRT = DateUtil.convertDate2String(new Date());
		} catch (DaoException daoException) {
			log.warn("Error when handle data base" + daoException.getMessage());
		} catch (Exception e) {
			log.error("程序出错:" + e.getMessage());
		} finally {
			running = false;
		}
	}

}
