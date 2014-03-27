package com.taobao.taokeeper.monitor.core2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.taobao.taokeeper.common.GlobalInstance;
import com.taobao.taokeeper.dao.AlarmSettingsDAO;
import com.taobao.taokeeper.dao.ZooKeeperClusterDAO;
import com.taobao.taokeeper.model.AlarmSettings;
import com.taobao.taokeeper.model.ZooKeeperCluster;
import com.taobao.taokeeper.monitor.core2.Events.AlertRuleCheckEvent;
import com.taobao.taokeeper.monitor.core2.Events.CommandEvent;
import com.taobao.taokeeper.monitor.core2.Events.RTEvent;
import com.taobao.taokeeper.monitor.core2.task.AlertRuleCheckTask;
import com.taobao.taokeeper.monitor.core2.task.CommandTask;
import com.taobao.taokeeper.monitor.core2.task.RTTask;

/**
 * 
 * @author pingwei 2014-3-21 下午6:22:10
 */

public class ScheduleManager {
	
	static final Logger log = LoggerFactory.getLogger(ScheduleManager.class);

	private static Map<Integer/* clusterId */, ExecutorService> threadPools = new ConcurrentHashMap<Integer, ExecutorService>();
	private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public static void init() {
		scheduler.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				scheduleWork();
			}
		}, 0, 60, TimeUnit.SECONDS);
	}

	static void scheduleWork() {
		List<ZooKeeperCluster> zooKeeperClusterSet = new ArrayList<ZooKeeperCluster>();
		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
		try {
			Map<Integer, ZooKeeperCluster> zooKeeperClusterMap = GlobalInstance.getAllZooKeeperCluster();
			if (null == zooKeeperClusterMap) {
				ZooKeeperClusterDAO zooKeeperClusterDAO = (ZooKeeperClusterDAO) wac.getBean("zooKeeperClusterDAO");
				zooKeeperClusterSet = zooKeeperClusterDAO.getAllDetailZooKeeperCluster();
			} else {
				zooKeeperClusterSet.addAll(zooKeeperClusterMap.values());
			}
		} catch (Exception e) {
		}
		for (ZooKeeperCluster cluster : zooKeeperClusterSet) {
			try {
				ExecutorService pool = threadPools.get(cluster.getClusterId());
				if (pool == null) {
					pool = Executors.newFixedThreadPool(3);
					threadPools.put(cluster.getClusterId(), pool);
				}
				if(CollectionUtils.isEmpty(cluster.getServerList())){
					continue;
				}
				AlarmSettingsDAO alarmSettingsDAO = ( AlarmSettingsDAO ) wac.getBean( "alarmSettingsDAO" );
				AlarmSettings alarmSettings = alarmSettingsDAO.getAlarmSettingsByCulsterId( cluster.getClusterId() );
				for (String server : cluster.getServerList()) {
					String[] tmp = server.split(":");
					String host = tmp[0];
					int port = NumberUtils.toInt(tmp[1], 2181);
					EventDispatcher.fireEvent(new CommandEvent(new CommandTask(pool, host, port, CommandTask.CMD_RWPS)));
					EventDispatcher.fireEvent(new CommandEvent(new CommandTask(pool, host, port, CommandTask.CMD_SRVR)));
					EventDispatcher.fireEvent(new CommandEvent(new CommandTask(pool, host, port, CommandTask.CMD_STAT)));
					EventDispatcher.fireEvent(new CommandEvent(new CommandTask(pool, host, port, CommandTask.CMD_WCHS)));
					EventDispatcher.fireEvent(new RTEvent(new RTTask(pool, host, port)));
				}
				EventDispatcher.fireEvent(new AlertRuleCheckEvent(new AlertRuleCheckTask(pool, cluster.getServerList(), cluster, alarmSettings)));
			} catch (Exception e) {
				log.error(e.getMessage(), e.getCause());
			}
		}
	}
}
