package com.taobao.taokeeper.monitor.core2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.taobao.taokeeper.model.RwpsInfo;
import com.taobao.taokeeper.model.SrvrInfo;
import com.taobao.taokeeper.model.StatInfo;
import com.taobao.taokeeper.model.WchsInfo;
import com.taobao.taokeeper.model.ZooKeeperRTInfo;
import com.taobao.taokeeper.model.ZooKeeperStatusV2;
import com.taobao.taokeeper.model.ZooKeeperStatusV2.RWStatistics;

/**
 * 
 * @author pingwei 2014-3-21 下午6:04:28
 */

public class ZookeeperData {

	public static Map<String/* ip:port */, SrvrInfo> srvrMap = new ConcurrentHashMap<String, SrvrInfo>();

	public static Map<String, WchsInfo> wchsMap = new ConcurrentHashMap<String, WchsInfo>();

	public static Map<String, StatInfo> statMap = new ConcurrentHashMap<String, StatInfo>();

	public static Map<String, RwpsInfo> rwpsMap = new ConcurrentHashMap<String, RwpsInfo>();
	
	public static Map<String, ZooKeeperRTInfo> rtMap = new ConcurrentHashMap<String, ZooKeeperRTInfo>();
	
	
	public static SrvrInfo getSrvr(String ip, int port){
		String hostId = MonitorUtils.hostId(ip, port);
		return srvrMap.get(hostId);
	}
	
	public static ZooKeeperRTInfo getRTInfo(String ip, int port){
		String hostId = MonitorUtils.hostId(ip, port);
		return rtMap.get(hostId);
	}

	public static ZooKeeperStatusV2 getZooKeeperStatus(String ip, int port) {
		String hostId = MonitorUtils.hostId(ip, port);
		ZooKeeperStatusV2 status = new ZooKeeperStatusV2();
		SrvrInfo srvr = srvrMap.get(hostId);
		WchsInfo wchs = wchsMap.get(hostId);
		StatInfo stat = statMap.get(hostId);
		RwpsInfo rwps = rwpsMap.get(hostId);

		status.setIp(ip);
		if (srvr != null) {
			status.setLeader(StringUtils.equals(srvr.getMode(), "leader"));
			status.setMode(srvr.getMode());
			status.setNodeCount(srvr.getNodeCount());
			status.setReceived(String.valueOf(srvr.getReceived()));
			status.setSent(String.valueOf(srvr.getSent()));
		}
		if (stat != null) {
			status.setClientConnectionList(stat.getClients());
			status.setStatContent(stat.getContent());
		}
		if (wchs != null) {
			status.setWatchedPaths(wchs.getWatchingPathCount());
			status.setWatches(wchs.getTotalWatchers());
		}

		if (rwps != null) {
			RWStatistics rw = new RWStatistics(rwps.getGetChildren2(), rwps.getCreateSession(), rwps.getCloseSession(),
					rwps.getSetData(), rwps.getSetWatches(), rwps.getGetChildren(), rwps.getDelete(), rwps.getCreate(),
					rwps.getExists(), rwps.getGetData());
			status.setRwps(rw);
		}
		return status;
	}
}
