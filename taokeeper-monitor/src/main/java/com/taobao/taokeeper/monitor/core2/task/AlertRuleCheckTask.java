package com.taobao.taokeeper.monitor.core2.task;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.taokeeper.model.AlarmSettings;
import com.taobao.taokeeper.model.AlertInfo;
import com.taobao.taokeeper.model.RwpsInfo;
import com.taobao.taokeeper.model.SrvrInfo;
import com.taobao.taokeeper.model.StatInfo;
import com.taobao.taokeeper.model.ZooKeeperCluster;
import com.taobao.taokeeper.monitor.core2.EventDispatcher;
import com.taobao.taokeeper.monitor.core2.Events.AlertEvent;
import com.taobao.taokeeper.monitor.core2.ZookeeperData;

/**
 * 根据规则检查是否当前状态是否需要报警
 * 
 * @author pingwei 2014-3-27 下午2:01:44
 */

public class AlertRuleCheckTask {

	static final Logger log = LoggerFactory.getLogger(AlertRuleCheckTask.class);
	static  long MAX_ZXID = 0xffffffffL;

	static long shutdownCheckTime = 0L;

	ExecutorService pool;
	List<String> serverList;
	AlarmSettings alarmSettings;
	ZooKeeperCluster cluster;

	public AlertRuleCheckTask(ExecutorService pool, List<String> serverList, ZooKeeperCluster cluster,
			AlarmSettings alarmSettings) {
		this.pool = pool;
		this.serverList = serverList;
		this.cluster = cluster;
		this.alarmSettings = alarmSettings;
	}

	public void work() {
		int readTps = 0;
		for(String server : serverList){
			String[] tmp = server.split(":");
			String host = tmp[0];
			int port = NumberUtils.toInt(tmp[1], 2181);
			SrvrInfo srvr = ZookeeperData.getSrvr(host, port);
			RwpsInfo rwps = ZookeeperData.getRwps(host, port);
			if(srvr != null && srvr.isLeader()){
				checkShutdown(host, port);
				checkWriteCapacity(rwps.getWriteTPS());
			}
			checkConnectionCapacity(host, port);
			readTps += rwps.getReadTPS();
		}
		checkReadCapacity(readTps);
		
	}

	private void checkWriteCapacity(int writeTps) {
		if (writeTps >= alarmSettings.getMaxTpsWrite()) {
			String content = MessageFormat.format("zookeeper 集群【{0}】 当前写入TPS：{1}，超过最大水位{2}，请关注",
					new Object[] { cluster.getClusterName(), writeTps, alarmSettings.getMaxTpsWrite() });
			AlertInfo alert = new AlertInfo(alarmSettings.getWangwangList(), alarmSettings.getPhoneList(), content);
			EventDispatcher.fireEvent(new AlertEvent(new AlertTask(pool, alert)));
		}
	}

	private void checkReadCapacity(int readTps) {
		if(readTps >= alarmSettings.getMaxTpsRead()){
			String content = MessageFormat.format("zookeeper 集群【{0}】 当前读取TPS：{1}，超过最大水位{2}，请关注", new Object[] {
					cluster.getClusterName(),readTps, alarmSettings.getMaxTpsRead() });
			AlertInfo alert = new AlertInfo(alarmSettings.getWangwangList(), alarmSettings.getPhoneList(), content);
			EventDispatcher.fireEvent(new AlertEvent(new AlertTask(pool, alert)));
		}
	}

	private void checkConnectionCapacity(String ip, int port) {
		StatInfo stat = ZookeeperData.getStat(ip, port);
		if (stat != null) {
			int consSize =stat.getConnectionSize() > stat.getClients().size() ? stat.getConnectionSize() : stat.getClients().size();
			if (consSize >= alarmSettings.getMaxConnections()) {
				String content = MessageFormat.format("zookeeper 集群【{0}】 当前连接数：{1}，超过最大水位{2}，请关注",
						new Object[] { cluster.getClusterName(), consSize, alarmSettings.getMaxConnections() });
				AlertInfo alert = new AlertInfo(alarmSettings.getWangwangList(), alarmSettings.getPhoneList(), content);
				EventDispatcher.fireEvent(new AlertEvent(new AlertTask(pool, alert)));
			}
		}
	}

	/**
	 * 检查连接断开
	 */
	private void checkShutdown(String leaderIP, int leaderPort) {// 3小时检查一次
		if (System.currentTimeMillis() - shutdownCheckTime < 1000 * 3600 * 3) {
			return;
		}
		int writeTPS = 1;
		long zxidLeft = 0;
		shutdownCheckTime = System.currentTimeMillis();
		SrvrInfo srvr = ZookeeperData.getSrvr(leaderIP, leaderPort);
		if (srvr != null && srvr.isLeader()) {
			zxidLeft = srvr.getRemainZxid();
			RwpsInfo rwps = ZookeeperData.getRwps(leaderIP, leaderPort);
			if (rwps == null) {
				return;
			}
			writeTPS = rwps.getWriteTPS();
			int hour = (int) (zxidLeft / writeTPS / 3600);
			if (hour <= 24) {
				Date time = new Date(System.currentTimeMillis() + hour * 3600 * 1000);
				String content = MessageFormat.format("zookeeper 集群【{0}】 事务ID预计在{1}用完，需要断开所有连接重新选举生成新的epoll，请关注",
						new Object[] { cluster.getClusterName(), time });
				AlertInfo alert = new AlertInfo(alarmSettings.getWangwangList(), alarmSettings.getPhoneList(), content);
				EventDispatcher.fireEvent(new AlertEvent(new AlertTask(pool, alert)));
			}
		}

	}
	
}
