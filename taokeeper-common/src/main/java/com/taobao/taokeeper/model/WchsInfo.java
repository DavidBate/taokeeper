package com.taobao.taokeeper.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * 
 * @author pingwei
 * 2014-3-21 下午4:47:11
 */

public class WchsInfo {
	
	int connectionCount;
	int watchingPathCount;
	int totalWatchers;
	public int getConnectionCount() {
		return connectionCount;
	}
	public void setConnectionCount(int connectionCount) {
		this.connectionCount = connectionCount;
	}
	public int getWatchingPathCount() {
		return watchingPathCount;
	}
	public void setWatchingPathCount(int watchingPathCount) {
		this.watchingPathCount = watchingPathCount;
	}
	public int getTotalWatchers() {
		return totalWatchers;
	}
	public void setTotalWatchers(int totalWatchers) {
		this.totalWatchers = totalWatchers;
	}
	public static WchsInfo parse(String content){
		if(StringUtils.isEmpty(content)){
			return new WchsInfo();
		}
		BufferedReader br = null;
		StringReader sr = null;
		WchsInfo info = new WchsInfo();
		try {
			sr = new StringReader(content);
			br = new BufferedReader(sr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if(StringUtils.isBlank(line)){
					continue;
				}
				line = line.trim();
				if(StringUtils.startsWith(line, "Total")){
					info.setTotalWatchers(Integer.parseInt(line.split(":")[1]));
				} else {
					String[] vals = line.split(" ");
					info.setConnectionCount(NumberUtils.toInt(vals[0]));
					info.setWatchingPathCount(NumberUtils.toInt(vals[3]));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("parse wchs content failed", e);
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
}


