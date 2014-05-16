package com.taobao.taokeeper.monitor.core.task.runable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.taokeeper.common.util.ZKDataUtil;
import com.taobao.taokeeper.model.ZooKeeperRTInfo;

/**
 * 
 * @author pingwei 2014-2-25 下午10:16:36
 */

public class ZKServerRTCollector implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(ZKServerRTCollector.class);
	
	static String a = null;
	
	public static void main(String[] args) throws Exception{
		ZkClient zk = new ZkClient("10.232.6.30:2181");
		zk.waitUntilConnected();
		List<ACL> acls = new ArrayList<ACL>();
        //添加第一个id，采用用户名密码形式
        Id id1 = new Id("digest",
                DigestAuthenticationProvider.generateDigest("admin:admin"));
        ACL acl1 = new ACL(ZooDefs.Perms.ALL, id1);
        acls.add(acl1);
        //添加第二个id，所有用户可读权限
        Id id2 = new Id("world", "anyone");
        ACL acl2 = new ACL(ZooDefs.Perms.READ, id2);
        acls.add(acl2);
//        zk.addAuthInfo("digest", "admin:admin".getBytes());
//        zk.create("/pingwei", "hello", acls, CreateMode.PERSISTENT);
//        zk.create("/pingwei/test", "hello", acls, CreateMode.PERSISTENT);
        zk.addAuthInfo("digest", null);
        Thread.sleep(10900000);
	}
	
	static class DataWatcher implements Watcher{
		ZooKeeper zk;
		

		public DataWatcher(ZooKeeper zk) {
			super();
			this.zk = zk;
		}


		@Override
		public void process(WatchedEvent event) {
			if(event.getType() == Event.EventType.NodeDataChanged){
				Stat s = new Stat();
				try {
					System.out.println("data changed");
					a = new String(zk.getData("/weizhang", this, s));
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	String server;
	int clusterId;
	CountDownLatch latch;
	ZooKeeperRTInfo rtInfo;

	public ZKServerRTCollector(String server, int clusterId, CountDownLatch latch, ZooKeeperRTInfo rtInfo) {
		super();
		this.server = server;
		this.clusterId = clusterId;
		this.latch = latch;
		this.rtInfo = rtInfo;
	}

	@Override
	public void run() {
		ZkClient zkClient = null;
		String[] tmp = server.split(":");
		try {
			if(!ZKDataUtil.ruok(tmp[0], Integer.parseInt(tmp[1]))){
				return ;
			}
			long st = System.currentTimeMillis();
			zkClient = new ZkClient(server, 5000);
			zkClient.waitUntilConnected(5000, TimeUnit.SECONDS);
			long rt = System.currentTimeMillis() - st;
			rtInfo.setCreateSession(rt);
			final int cnt = 3;
			st = System.currentTimeMillis();
			for (int i = 0; i < cnt; i++) {
				try {
					zkClient.create("/qiaoyi.dingqy" + i, "rtMonitor".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
							CreateMode.EPHEMERAL);
				} catch (Exception e) {
				}
			}
			rt = System.currentTimeMillis() - st;
			rtInfo.setCreateNode(rt / cnt);
			st = System.currentTimeMillis();
			for (int i = 0; i < cnt; i++) {
				try {
					zkClient.exists("/qiaoyi.dingqy" + i);
				} catch (Exception e) {
				}
			}
			rt = System.currentTimeMillis() - st;
			rtInfo.setExists(rt / cnt);
			st = System.currentTimeMillis();
			for (int i = 0; i < cnt; i++) {
				try {
					zkClient.writeData("/qiaoyi.dingqy" + i, "rtMonitor".getBytes());
				} catch (Exception e) {
				}
			}
			rt = System.currentTimeMillis() - st;
			rtInfo.setSetData(rt / cnt);
			st = System.currentTimeMillis();
			for (int i = 0; i < cnt; i++) {
				try {
					zkClient.readData("/qiaoyi.dingqy" + i);
				} catch (Exception e) {
				}
			}
			rt = System.currentTimeMillis() - st;
			rtInfo.setGetData(rt / cnt);
			st = System.currentTimeMillis();
			for (int i = 0; i < cnt; i++) {
				try {
					zkClient.delete("/qiaoyi.dingqy" + i);
				} catch (Exception e) {
				}
			}
			rt = System.currentTimeMillis() - st;
			rtInfo.setDelete(rt / cnt);
			st = System.currentTimeMillis();
			for (int i = 0; i < cnt; i++) {
				try {
					zkClient.getChildren("/qiaoyi.dingqy" + i);
				} catch (Exception e) {
				}
			}
			rt = System.currentTimeMillis() - st;
			rtInfo.setGetChildren(rt / cnt);
		} catch (Exception e) {
			log.error("collect " + server + " rt error:" + e.getMessage(), e.getCause());
		} finally {
			try {
				if(zkClient != null){
					zkClient.close();
				}
			} catch (Exception e) {
				log.error("close zkclient error, " + e.getMessage(), e.getCause());
			}
			latch.countDown();
		}
	}

}
