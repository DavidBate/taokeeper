package com.taobao.taokeeper.model;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author pingwei 2014-2-26 下午2:25:12
 */

public class SrvrInfo {
	
	static final long MAX_ZXID = 0xffffffffL;
	
	int minLatency;
	int avgLatency;
	int maxLatency;
	long received;
	long sent;
	long Outstanding;
	String zxid;
	String mode;
	long nodeCount;

	public static SrvrInfo parse(String content) {
		if (StringUtils.isEmpty(content)) {
			return new SrvrInfo();
		}
		SrvrInfo info = new SrvrInfo();
		parse(content, info);
		return info;
	}

	public static void parse(String content, SrvrInfo info) {
		if (StringUtils.isEmpty(content)) {
			return;
		}
		String[] lines = content.split("\n");
		for (String line : lines) {
			if(StringUtils.isBlank(line)){
				continue;
			}
			line = line.trim();
			if (line.startsWith("Latency")) {
				String val = line.split(":")[1].trim();
				String[] tmp = val.split("/");
				info.setMinLatency(Integer.parseInt(tmp[0]));
				info.setAvgLatency(Integer.parseInt(tmp[1]));
				info.setMaxLatency(Integer.parseInt(tmp[2]));
			} else if (line.startsWith("Received: ")) {
				info.setReceived(Long.parseLong(line.substring("Received: ".length())));
			} else if (line.startsWith("Sent: ")) {
				info.setSent(Long.parseLong(line.substring("Sent: ".length())));
			} else if (line.startsWith("Outstanding: ")) {
				info.setOutstanding(Long.parseLong(line.substring("Outstanding: ".length())));
			} else if (line.startsWith("Zxid: ")) {
				info.setZxid(line.substring("Zxid: ".length()));
			} else if (line.startsWith("Mode: ")) {
				info.setMode(line.substring("Mode: ".length()));
			} else if (line.startsWith("Node count: ")) {
				info.setNodeCount(Long.parseLong(line.substring("Node count: ".length())));
			}
		}
	}

	public int getMinLatency() {
		return minLatency;
	}

	public void setMinLatency(int minLatency) {
		this.minLatency = minLatency;
	}

	public int getAvgLatency() {
		return avgLatency;
	}

	public void setAvgLatency(int avgLatency) {
		this.avgLatency = avgLatency;
	}

	public int getMaxLatency() {
		return maxLatency;
	}

	public void setMaxLatency(int maxLatency) {
		this.maxLatency = maxLatency;
	}

	public long getReceived() {
		return received;
	}

	public void setReceived(long received) {
		this.received = received;
	}

	public long getSent() {
		return sent;
	}

	public void setSent(long sent) {
		this.sent = sent;
	}

	public long getOutstanding() {
		return Outstanding;
	}

	public void setOutstanding(long outstanding) {
		Outstanding = outstanding;
	}

	public String getZxid() {
		return zxid;
	}

	public void setZxid(String zxid) {
		this.zxid = zxid;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public long getNodeCount() {
		return nodeCount;
	}

	public void setNodeCount(long nodeCount) {
		this.nodeCount = nodeCount;
	}
	public boolean isLeader(){
		return StringUtils.equals(mode, "leader");
	}
	
	public long getRemainZxid(){
		long xid = Long.parseLong(zxid.substring(2), 16) & MAX_ZXID;
		return MAX_ZXID - xid;
	}
}
