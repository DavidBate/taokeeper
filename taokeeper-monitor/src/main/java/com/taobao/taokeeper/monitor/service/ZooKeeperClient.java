package com.taobao.taokeeper.monitor.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.taobao.taokeeper.dao.ZooKeeperClusterDAO;
import com.taobao.taokeeper.model.NodeAttribute;
import com.taobao.taokeeper.model.ZooKeeperCluster;

/**
 * 
 * @author pingwei 2014-3-4 上午9:17:38
 */

public class ZooKeeperClient {

	public static ConcurrentHashMap<Integer/* clusterId */, ZooKeeper> clientMap = new ConcurrentHashMap<Integer, ZooKeeper>();

	public static List<String> getChildren(int clusterId, String path) {
		try {
			ZooKeeper client = getClient(clusterId);
			return client.getChildren(path, false);
		} catch (Exception e) {
			return null;
		}
	}

	public static NodeAttribute getNode(int clusterId, String path) {
		try {
			ZooKeeper client = getClient(clusterId);
			Stat stat = new Stat();
			byte[] bs = client.getData(path, false, stat);
			NodeAttribute node = new NodeAttribute();
			node.setStat(stat);
			node.setData(bs == null ? null : new String(bs));
			return node;
		} catch (Exception e) {
			return null;
		}
	}

	public static void create(int clusterId, String path, String data, boolean persistent) {
		try {
			ZooKeeper client = getClient(clusterId);
			if(client.exists(path, false) != null){
				return ;
			}
			client.create(path, data.getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, persistent ? CreateMode.PERSISTENT
					: CreateMode.EPHEMERAL);
		} catch (Exception e) {
			return;
		}
	}

	public static void delete(int clusterId, String path) {
		try {
			ZooKeeper client = getClient(clusterId);
			client.delete(path, -1);
		} catch (Exception e) {
			return;
		}
	}
	
	public static Stat setData(int cid, String path, int version, String data){
		try {
			ZooKeeper client = getClient(cid);
			return client.setData(path, data.getBytes("UTF-8"), version);
		} catch (Exception e) {
			return null;
		}
	}

	static ZooKeeper getClient(int clusterId) throws Exception {
		ZooKeeper client = clientMap.get(clusterId);
		if (client == null) {
			WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
			ZooKeeperClusterDAO zooKeeperClusterDAO = (ZooKeeperClusterDAO) wac.getBean("zooKeeperClusterDAO");
			ZooKeeperCluster zkCluster = zooKeeperClusterDAO.getZooKeeperClusterByCulsterId(clusterId);
			if (zkCluster == null) {
				return null;
			}
			String servers = list2String(zkCluster.getServerList());
			if (servers == null) {
				return null;
			}
			final CountDownLatch l = new CountDownLatch(1);
			client = new ZooKeeper(servers, 10 * 1000, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					l.countDown();
				}
			});
			l.await();
			ZooKeeper tmpClient = clientMap.putIfAbsent(clusterId, client);
			return tmpClient == null ? client : tmpClient;
		}
		if(!client.getState().isConnected()){
			client.close();
			return getClient(clusterId);
		}
		return client;
	}

	static String list2String(List<String> list) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		StringBuilder s = new StringBuilder();
		for (String str : list) {
			s.append(str).append(',');
		}
		s.deleteCharAt(s.length() - 1);
		return s.toString();
	}

	public static void main(String[] args) {

	}
}
