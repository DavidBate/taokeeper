package com.taobao.taokeeper.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * 
 * @author pingwei
 * 2014-3-21 下午4:55:59
 */

public class RwpsInfo {
	
	static final String NOT_SUPPORT = "server_process_stats not open, use -Dserver_process_stats=true to open."; 

	int getChildren2 = -1;
	int createSession = -1;
	int closeSession = -1;
	int setData = -1;
	int setWatches = -1;
	int getChildren = -1;
	int delete = -1;
	int create = -1;
	int exists = -1;
	int getData = -1;
	
	public int getWriteTPS(){
		int tps = createSession + closeSession + setData + delete + create;
		return tps < 0 ? 1 : tps;
	}
	
	public int getReadTPS(){
		int tps = getChildren2 + setWatches + getChildren + exists + getData ;
		return tps < 0 ? 1 : tps;
	}

	
	static int string2int(String line, String prefix){
		String num = line.substring(prefix.length());
		return (int)NumberUtils.toDouble(num);
	}
	
	public static RwpsInfo parse(String content){
		if(StringUtils.contains(content, NOT_SUPPORT)){
			return new RwpsInfo();
		}
		BufferedReader br = null;
		StringReader sr = null;
		RwpsInfo info = new RwpsInfo();
		try {
			sr = new StringReader(content);
			br = new BufferedReader(sr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if(StringUtils.isBlank(line)){
					continue;
				}
				line = line.trim();
				if(StringUtils.startsWith(line, "getChildren2")){
					info.setGetChildren2(string2int(line,"getChildren2:"));
				} else if(StringUtils.startsWith(line, "createSession")){
					info.setCreateSession(string2int(line,"createSession:"));
				} else if(StringUtils.startsWith(line, "closeSession")){
					info.setCloseSession(string2int(line, "closeSession:"));
				} else if(StringUtils.startsWith(line, "setData")){
					info.setSetData(string2int(line, "setData:"));
				} else if(StringUtils.startsWith(line, "setWatches")){
					info.setSetWatches(string2int(line, "setWatches:"));
				} else if(StringUtils.startsWith(line, "getChildren")){
					info.setGetChildren(string2int(line, "getChildren:"));
				} else if(StringUtils.startsWith(line, "delete")){
					info.setDelete(string2int(line, "delete:"));
				} else if(StringUtils.startsWith(line, "create")){
					info.setCreate(string2int(line, "create:"));
				} else if(StringUtils.startsWith(line, "exists")){
					info.setExists(string2int(line, "exists:"));
				} else if(StringUtils.startsWith(line, "getDate")){
					info.setGetData(string2int(line, "getDate:"));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("parse rwps content failed", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
		return info;
	}
	
	public int getGetChildren2() {
		return getChildren2;
	}
	public void setGetChildren2(int getChildren2) {
		this.getChildren2 = getChildren2;
	}
	public int getCreateSession() {
		return createSession;
	}
	public void setCreateSession(int createSession) {
		this.createSession = createSession;
	}
	public int getCloseSession() {
		return closeSession;
	}
	public void setCloseSession(int closeSession) {
		this.closeSession = closeSession;
	}
	public int getSetData() {
		return setData;
	}
	public void setSetData(int setData) {
		this.setData = setData;
	}
	public int getSetWatches() {
		return setWatches;
	}
	public void setSetWatches(int setWatches) {
		this.setWatches = setWatches;
	}
	public int getGetChildren() {
		return getChildren;
	}
	public void setGetChildren(int getChildren) {
		this.getChildren = getChildren;
	}
	public int getDelete() {
		return delete;
	}
	public void setDelete(int delete) {
		this.delete = delete;
	}
	public int getCreate() {
		return create;
	}
	public void setCreate(int create) {
		this.create = create;
	}
	public int getExists() {
		return exists;
	}
	public void setExists(int exists) {
		this.exists = exists;
	}
	public int getGetData() {
		return getData;
	}
	public void setGetData(int getData) {
		this.getData = getData;
	}
}


