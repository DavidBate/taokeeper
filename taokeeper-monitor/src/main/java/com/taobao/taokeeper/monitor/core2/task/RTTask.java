package com.taobao.taokeeper.monitor.core2.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

import com.taobao.taokeeper.common.util.ZKDataUtil;
import com.taobao.taokeeper.model.ZooKeeperRTInfo;
import com.taobao.taokeeper.monitor.core2.MonitorUtils;
import com.taobao.taokeeper.monitor.core2.ZookeeperData;

/**
 * 
 * @author pingwei
 * 2014-3-23 下午11:11:48
 */

public class RTTask extends BaseTask{
	

	public RTTask(ExecutorService pool, String host, int port) {
		super(pool, host, port);
	}
	

	@Override
	public void work() {
		
		pool.execute(new Runnable() {
			
			@Override
			public void run() {
				log.info("run task :" + taskDesc());
				ZkClient zkClient = null;
				ZooKeeperRTInfo rtInfo = new ZooKeeperRTInfo();
				try {
					if(!ZKDataUtil.ruok(host, port)){
						return ;
					}
					long st = System.currentTimeMillis();
					zkClient = new ZkClient(MonitorUtils.hostId(host, port), 5000);
					zkClient.waitUntilConnected(5000, TimeUnit.SECONDS);
					long rt = System.currentTimeMillis() - st;
					rtInfo.setCreateSession(rt);
					final int cnt = 3;
					st = System.currentTimeMillis();
					for (int i = 0; i < cnt; i++) {
						try {
							zkClient.create("/qiaoyi.dingqy" + host + i, "rtMonitor".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
									CreateMode.EPHEMERAL);
						} catch (Exception e) {
						}
					}
					rt = System.currentTimeMillis() - st;
					rtInfo.setCreateNode(rt / cnt);
					st = System.currentTimeMillis();
					for (int i = 0; i < cnt; i++) {
						try {
							zkClient.exists("/qiaoyi.dingqy" + host + i);
						} catch (Exception e) {
						}
					}
					rt = System.currentTimeMillis() - st;
					rtInfo.setExists(rt / cnt);
					st = System.currentTimeMillis();
					for (int i = 0; i < cnt; i++) {
						try {
							zkClient.writeData("/qiaoyi.dingqy" + host + i, "rtMonitor".getBytes());
						} catch (Exception e) {
						}
					}
					rt = System.currentTimeMillis() - st;
					rtInfo.setSetData(rt / cnt);
					st = System.currentTimeMillis();
					for (int i = 0; i < cnt; i++) {
						try {
							zkClient.readData("/qiaoyi.dingqy"+ host  + i);
						} catch (Exception e) {
						}
					}
					rt = System.currentTimeMillis() - st;
					rtInfo.setGetData(rt / cnt);
					
					st = System.currentTimeMillis();
					for (int i = 0; i < cnt; i++) {
						try {
							zkClient.getChildren("/qiaoyi.dingqy" + host + i);
						} catch (Exception e) {
						}
					}
					rt = System.currentTimeMillis() - st;
					rtInfo.setGetChildren(rt / cnt);
					
					st = System.currentTimeMillis();
					for (int i = 0; i < cnt; i++) {
						try {
							zkClient.delete("/qiaoyi.dingqy"+ host  + i);
						} catch (Exception e) {
						}
					}
					rt = System.currentTimeMillis() - st;
					rtInfo.setDelete(rt / cnt);
					
				} catch (Exception e) {
					log.error("collect " + host + " rt error:" + e.getMessage(), e.getCause());
				} finally {
					try {
						ZookeeperData.rtMap.put(MonitorUtils.hostId(host, port), rtInfo);
						if(zkClient != null){
							zkClient.close();
						}
					} catch (Exception e) {
						log.error("close zkclient error, " + e.getMessage(), e.getCause());
					}
				}
			}
		});
		
		
	}
	
	@Override
	public String taskDesc() {
		return toString();
	}

	@Override
	public String toString() {
		return "RTTask [host=" + host + ", port=" + port + "]";
	}
	
	

}


