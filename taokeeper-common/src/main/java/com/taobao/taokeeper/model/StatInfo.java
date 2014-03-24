package com.taobao.taokeeper.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author pingwei 2014-3-21 下午3:46:51
 */

public class StatInfo {
	List<String> clients;
	SrvrInfo srvrInfo;
	String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<String> getClients() {
		return clients;
	}

	public void setClients(List<String> clients) {
		this.clients = clients;
	}

	public SrvrInfo getSrvrInfo() {
		return srvrInfo;
	}

	public void setSrvrInfo(SrvrInfo srvrInfo) {
		this.srvrInfo = srvrInfo;
	}

	public static StatInfo parse(String content) {
		BufferedReader br = null;
		StringReader sr = null;
		StatInfo info = new StatInfo();
		List<String> clients = new ArrayList<String>();
		SrvrInfo srvr = new SrvrInfo();
		info.setClients(clients);
		info.setSrvrInfo(srvr);
		StringBuilder sb = new StringBuilder();
		try {
			sr = new StringReader(content);
			br = new BufferedReader(sr);
			String line = null;
			boolean srvrInfo = false;
			StringBuilder srvrStr = new StringBuilder();
			while ((line = br.readLine()) != null) {
				if(StringUtils.isBlank(line)){
					continue;
				}
				line = line.trim();
				sb.append(line).append("<br/>");
				if (line.charAt(0) == '/') {
					clients.add(line);
				} else if (StringUtils.startsWith(line, "Latency")) {
					srvrInfo = true;
				}
				if(srvrInfo){
					srvrStr.append(line).append("\n");
				}
			}
			SrvrInfo.parse(srvrStr.toString(), srvr);
			info.setContent(sb.toString());
		} catch (Exception e) {
			throw new RuntimeException("parse stat content failed :" + content , e);
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
