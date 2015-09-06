package com.taobao.taokeeper.model;
/**
 * 
 * @author pingwei
 * 2014-2-25 下午10:35:30
 */

public class ZooKeeperRTInfo {
	long createSession = -1;
	long createNode = -1;
	long exists = -1;
	long setData = -1;
	long getData = -1;
	long delete = -1;
	long getChildren = -1;
	public long getCreateSession() {
		return createSession;
	}
	public void setCreateSession(long createSession) {
		this.createSession = createSession;
	}
	public long getCreateNode() {
		return createNode;
	}
	public void setCreateNode(long createNode) {
		this.createNode = createNode;
	}
	public long getExists() {
		return exists;
	}
	public void setExists(long exists) {
		this.exists = exists;
	}
	public long getSetData() {
		return setData;
	}
	public void setSetData(long setData) {
		this.setData = setData;
	}
	public long getGetData() {
		return getData;
	}
	public void setGetData(long getData) {
		this.getData = getData;
	}
	public long getDelete() {
		return delete;
	}
	public void setDelete(long delete) {
		this.delete = delete;
	}
	public long getGetChildren() {
		return getChildren;
	}
	public void setGetChildren(long getChildren) {
		this.getChildren = getChildren;
	}
	
}


