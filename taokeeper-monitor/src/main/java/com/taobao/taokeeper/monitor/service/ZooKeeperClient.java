package com.taobao.taokeeper.monitor.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
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

	public static ConcurrentHashMap<Integer/* clusterId */, ZkClient> clientMap = new ConcurrentHashMap<Integer, ZkClient>();

	public static List<String> getChildren(int clusterId, String path) {
		try {
			ZkClient client = getClient(clusterId);
			return client.getChildren(path);
		} catch (Exception e) {
			return null;
		}
	}

	public static NodeAttribute getNode(int clusterId, String path) {
		try {
			ZkClient client = getClient(clusterId);
			Stat stat = new Stat();
			byte[] bs = client.readData(path, stat);
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
			ZkClient client = getClient(clusterId);
			if(client.exists(path)){
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
			ZkClient client = getClient(clusterId);
			client.delete(path);
		} catch (Exception e) {
			return;
		}
	}
	
	public static void setData(int cid, String path, int version, String data){
		try {
			ZkClient client = getClient(cid);
			client.writeData(path, data);
		} catch (Exception e) {
		}
	}

	static ZkClient getClient(int clusterId) throws Exception {
		ZkClient client = clientMap.get(clusterId);
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
			client = new ZkClient(servers, 10 * 1000);
			ZkClient tmpClient = clientMap.putIfAbsent(clusterId, client);
			if(tmpClient == null){
				return client;
			}
			client.close();
			return tmpClient;
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
