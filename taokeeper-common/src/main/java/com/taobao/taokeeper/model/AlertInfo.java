package com.taobao.taokeeper.model;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author pingwei
 * 2014-3-25 下午1:31:51
 */

public class AlertInfo {

	String wangwangList;
	String content;
	String mobileList;
	public AlertInfo(String wangwangList, String mobileList, String content) {
		super();
		this.wangwangList = wangwangList;
		this.content = content;
		this.mobileList = mobileList;
	}
	public String getWangwangList() {
		return wangwangList;
	}
	public void setWangwangList(String wangwangList) {
		this.wangwangList = wangwangList;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public List<String> getWangwangAsList(){
		return Arrays.asList(wangwangList.split(","));
	}
	
	public List<String> getMobileAsList(){
		return Arrays.asList(mobileList.split(","));
	}
	
	public String getMobileList() {
		return mobileList;
	}
	public void setMobileList(String mobileList) {
		this.mobileList = mobileList;
	}
}


