package com.taobao.taokeeper.monitor.core.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.taobao.taokeeper.common.GlobalInstance;
import com.taobao.taokeeper.dao.ZooKeeperClusterDAO;
import com.taobao.taokeeper.model.SrvrInfo;
import com.taobao.taokeeper.model.ZooKeeperCluster;
import com.taobao.taokeeper.model.ZooKeeperDelayInfo;
import com.taobao.taokeeper.monitor.core.ThreadPoolManager;
import com.taobao.taokeeper.monitor.core.task.runable.ZKServerDelayCollector;
import common.toolkit.java.constant.SymbolConstant;
import common.toolkit.java.exception.DaoException;
import common.toolkit.java.util.DateUtil;

/**
 * 
 * @author pingwei 2014-2-26 下午2:48:41
 */

public class ZooKeeperDelayCollectJob implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(ZooKeeperDelayCollectJob.class);

	@Override
	public void run() {
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
						Map<String, SrvrInfo> map = new HashMap<String, SrvrInfo>();
						for (String server : zookeeperCluster.getServerList()) {
							SrvrInfo info = new SrvrInfo();
							map.put(server, info);
							String[] tmp = server.split(SymbolConstant.COLON);
							ThreadPoolManager.zooKeeperdelayCollectorExecutor.execute(new ZKServerDelayCollector(tmp[0],
									Integer.parseInt(tmp[1]), info, latch));
						}
						latch.await();
						SrvrInfo leaderInfo = null;
						for(Entry<String, SrvrInfo> en : map.entrySet()){
							if("leader".equalsIgnoreCase(en.getValue().getMode())){
								leaderInfo = en.getValue();
								break;
							}
						}
						Map<String, ZooKeeperDelayInfo> delayMap = new HashMap<String, ZooKeeperDelayInfo>();
						for(Entry<String, SrvrInfo> en : map.entrySet()){
							ZooKeeperDelayInfo info = new ZooKeeperDelayInfo();
							if(en.getValue().getZxid() != null){
								info.setInProcessTask(en.getValue().getOutstanding());
								long leadZxid = Long.parseLong(leaderInfo.getZxid().substring(2), 16);
								long zxid = Long.parseLong(en.getValue().getZxid().substring(2), 16);
								info.setSyncDelay(leadZxid - zxid);
								zxid = leadZxid & ZooKeeperDelayInfo.ZXID_MAX;
								info.setRemainZxid(ZooKeeperDelayInfo.ZXID_MAX - zxid);
							}
							delayMap.put(en.getKey(), info);
						}
						GlobalInstance.delayInfoMap.put(zookeeperCluster.getClusterId(), delayMap);
					}
				}
			}
		} catch (DaoException daoException) {
			log.warn("Error when handle data base" + daoException.getMessage());
		} catch (Exception e) {
			log.error("程序出错:" + e.getMessage());
		} finally{
			GlobalInstance.timeOfUpdateDelay = DateUtil.convertDate2String(new Date());
		}
	}

}
