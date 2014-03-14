package com.taobao.taokeeper.model;

import java.util.Date;

import org.apache.zookeeper.data.Stat;

/**
 * 
 * @author pingwei
 * 2014-3-4 上午10:02:02
 */

public class NodeAttribute {
	Stat stat;
	
	String data;
	
	
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public Stat getStat() {
		return stat;
	}
	public void setStat(Stat stat) {
		this.stat = stat;
	}
	public String getCtime(){
		return new Date(stat.getCtime()).toString();
	}
	public String getMtime(){
		return new Date(stat.getMtime()).toString();
	}
}


