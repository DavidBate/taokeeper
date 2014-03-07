package com.taobao.taokeeper.monitor.core.task.runable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
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

	static class DefaultWatcher implements Watcher {

		CountDownLatch connectSignal = null;

		private DefaultWatcher(CountDownLatch connectSignal) {
			this.connectSignal = connectSignal;
		}

		@Override
		public void process(WatchedEvent event) {
			if (event.getType() == Event.EventType.None && event.getState() == Event.KeeperState.SyncConnected && connectSignal != null) {
				connectSignal.countDown();
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
		CountDownLatch l = new CountDownLatch(1);
		ZooKeeper zk = new ZooKeeper("10.125.192.53:2181", 30*1000, new DefaultWatcher(l));
		l.await();
		System.out.println(zk.getData("/pingwei", null, null));
		zk.close();
	}

}
