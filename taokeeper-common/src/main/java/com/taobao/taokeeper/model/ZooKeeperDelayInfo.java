package com.taobao.taokeeper.model;

/**
 * 
 * @author pingwei 2014-2-26 下午1:49:56
 */

public class ZooKeeperDelayInfo {
	public static final long ZXID_MAX = 0xffffffffL;
//	long syncDelay = -1;
	long inProcessTask = -1;
	long remainZxid = -1;
//	public long getSyncDelay() {
//		return syncDelay;
//	}
//	public void setSyncDelay(long syncDelay) {
//		this.syncDelay = syncDelay;
//	}
	public long getInProcessTask() {
		return inProcessTask;
	}
	public void setInProcessTask(long inProcessTask) {
		this.inProcessTask = inProcessTask;
	}
	public long getRemainZxid() {
		return remainZxid;
	}
	public void setRemainZxid(long remainZxid) {
		this.remainZxid = remainZxid;
	}
	
	
}
