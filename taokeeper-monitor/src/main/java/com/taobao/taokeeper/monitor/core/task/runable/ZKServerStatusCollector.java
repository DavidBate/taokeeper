package com.taobao.taokeeper.monitor.core.task.runable;

import static com.taobao.taokeeper.common.constant.SystemConstant.CR;
import static com.taobao.taokeeper.common.constant.SystemConstant.RN;
import static com.taobao.taokeeper.common.constant.SystemConstant.RWPS;
import static com.taobao.taokeeper.common.constant.SystemConstant.STAT;
import static com.taobao.taokeeper.common.constant.SystemConstant.WCHS;
import static common.toolkit.java.constant.BaseConstant.WORD_SEPARATOR;
import static common.toolkit.java.constant.EmptyObjectConstant.EMPTY_STRING;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.taobao.taokeeper.common.GlobalInstance;
import com.taobao.taokeeper.dao.ReportDAO;
import com.taobao.taokeeper.model.AlarmSettings;
import com.taobao.taokeeper.model.TaoKeeperStat;
import com.taobao.taokeeper.model.ZooKeeperCluster;
import com.taobao.taokeeper.model.ZooKeeperStatus;
import com.taobao.taokeeper.model.ZooKeeperStatusV2;
import com.taobao.taokeeper.model.type.Message;
import com.taobao.taokeeper.monitor.core.ThreadPoolManager;
import com.taobao.taokeeper.reporter.alarm.TbMessageSender;
import common.toolkit.java.entity.DateFormat;
import common.toolkit.java.entity.io.Connection;
import common.toolkit.java.exception.DaoException;
import common.toolkit.java.util.DateUtil;
import common.toolkit.java.util.ObjectUtil;
import common.toolkit.java.util.StringUtil;
import common.toolkit.java.util.collection.MapUtil;
import common.toolkit.java.util.io.IOUtil;

/**
 * Description: 采集zooKeeper上的状态信息
 * 
 * @author 银时 yinshi.nc@taobao.com
 * @Date Dec 26, 2011
 */
public class ZKServerStatusCollector implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(ZKServerStatusCollector.class);

	private static final String MODE_FOLLOWER = "Mode: follower";
	private static final String MODE_LEADERER = "Mode: leader";
	private static final String MODE_STANDALONE = "Mode: standalone";
	private static final String MODE_OBSERVER = "Mode: observer";
	private static final String NODE_COUNT = "Node count:";

	private static final String STRING_CONNECTIONS_WATCHING = "connections watching";
	private static final String STRING_PATHS = "paths";
	private static final String STRING_TOTAL_WATCHES = "Total watches:";

	private static final String STRING_SENT = "Sent:";

	private static final String STRING_RECEIVED = "Received:";

	private String ip;
	private String port;
	private AlarmSettings alarmSettings;
	private ZooKeeperCluster zookeeperCluster;
	private boolean needStoreToDB;

	public ZKServerStatusCollector(String ip, String port, AlarmSettings alarmSettings,
			ZooKeeperCluster zookeeperCluster) {
		this.ip = ip;
		this.port = port;
		this.alarmSettings = alarmSettings;
		this.zookeeperCluster = zookeeperCluster;
		this.needStoreToDB = true;
	}

	public ZKServerStatusCollector(String ip, String port, AlarmSettings alarmSettings,
			ZooKeeperCluster zookeeperCluster, boolean needStoreToDB) {
		this.ip = ip;
		this.port = port;
		this.alarmSettings = alarmSettings;
		this.zookeeperCluster = zookeeperCluster;
		this.needStoreToDB = needStoreToDB;
	}

	@Override
	public void run() {
		try {

			if (StringUtil.isBlank(ip) || StringUtil.isBlank(port)
					|| ObjectUtil.isBlank(alarmSettings, zookeeperCluster)) {
				return;
			}
			ZooKeeperStatusV2 zooKeeperStatus = new ZooKeeperStatusV2();
			sshZooKeeperAndHandleStat(ip, Integer.parseInt(port), zooKeeperStatus);
			telnetZooKeeperAndHandleWchs(ip, Integer.parseInt(port), zooKeeperStatus);
//			sshZooKeeperAndHandleWchc(ip, Integer.parseInt(port), zooKeeperStatus, zookeeperCluster.getClusterId());
			sshZooKeeperAndHandleRwps(ip, Integer.parseInt(port), (ZooKeeperStatusV2) zooKeeperStatus,
					zookeeperCluster.getClusterId());
			checkAndAlarm(alarmSettings, zooKeeperStatus, zookeeperCluster.getClusterName());
			GlobalInstance.putZooKeeperStatus(ip, zooKeeperStatus);
			// Store taokeeper stat to DB
			if (needStoreToDB) {
				storeTaoKeeperStatToDB(zookeeperCluster.getClusterId(), (ZooKeeperStatusV2) zooKeeperStatus);
			}

			LOG.info("Finish #" + zookeeperCluster.getClusterName() + "-" + ip);

		} catch (Exception e) {
			LOG.error(e.getMessage(), e.getCause());
		}
	}
	
	
	static String execCmdByTelnet1(String ip, int port, String cmd){
		TelnetClient tc = new TelnetClient();
		PrintStream out = null;
		InputStream in = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			tc.connect(ip, port);
			in = tc.getInputStream();
			out = new PrintStream(tc.getOutputStream());
			out.println(cmd);
			out.flush();
			byte[] buf = new byte[10224*1024];
			int len = -1;
			while((len = in.read(buf)) >= 0){
				bos.write(buf, 0, len);
			}
			return new String(bos.toByteArray());
		} catch (Exception e) {
			LOG.error("exec command :" + cmd + " failed ." + e.getMessage(), e.getCause());
			return new String(bos.toByteArray());
		} finally {
			try {
				tc.disconnect();
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
			}
		}
	}

	public static String execCmdBySockset(String ip, int port , String cmd){
		Socket so = null;
		PrintStream out = null;
		InputStream in = null;
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		try {
			so = new Socket(ip, port);
			out = new PrintStream(so.getOutputStream());
			in = so.getInputStream();
			out.println(cmd);
			out.flush();
			byte[] bs = new byte[1024 * 1024];
			int len = -1;
			try {
				while((len = in.read(bs)) >= 0){
					byteOut.write(bs, 0, len);
				}
			} catch (Exception e) {
				LOG.error("exec command :" + cmd + " failed ." + e.getMessage(), e.getCause());
			}
			return new String(byteOut.toByteArray());
		} catch (UnknownHostException e) {
			LOG.error("exec command :" + cmd + " failed ." + e.getMessage(), e.getCause());
		} catch (IOException e) {
			LOG.error("exec command :" + cmd + " failed ." + e.getMessage(), e.getCause());
		} finally {
			try {
				if(out != null){
					out.close();
				}
				if(in != null){
					in.close();
				}
				if (so != null) {
					so.close();
				}
				byteOut.close();
			} catch (Exception e) {
			}
		}
		return "";
	}
	
	
	
	static String[] split(String content){
		if(content.contains(RN)){
			return content.split(RN);
		}
		return content.split(CR);
	}
	
	/**
	 * 进行Telnet连接并进行返回处理,执行 stat命令
	 */
	private void sshZooKeeperAndHandleStat(String ip, int port, ZooKeeperStatus zooKeeperStatus) {

		BufferedReader bufferedRead = null;
		StringBuffer sb = new StringBuffer();
		String content = execCmdBySockset(ip, port, STAT);
		try {
			if (StringUtils.isEmpty(content)) {
				LOG.warn("No output of " + StringUtil.replaceSequenced(STAT, ip, port + EMPTY_STRING));
				return;
			}
			bufferedRead = new BufferedReader(new StringReader(content));
			/**
			 * 通常的内容是这样： Zookeeper version: 3.3.3-1073969, built on 02/23/2011
			 * 22:27 GMT Clients:
			 * /1.2.37.111:43681[1](queued=0,recved=434,sent=434)
			 * /10.13.44.47:54811[0](queued=0,recved=1,sent=0)
			 * 
			 * Latency min/avg/max: 0/1/227 Received: 2349 Sent: 2641
			 * Outstanding: 0 Zxid: 0xc00000243 Mode: follower Node count: 8
			 */
			String line = "";
			zooKeeperStatus.setIp(ip);
			List<String> clientConnectionList = new ArrayList<String>();
			while ((line = bufferedRead.readLine()) != null) {
				if (analyseLineIfClientConnection(line)) { // 检查是否是客户端连接
					clientConnectionList.add(line);
				} else if (line.contains(MODE_FOLLOWER)) {
					zooKeeperStatus.setMode("F");
				} else if (line.contains(MODE_LEADERER)) {
					zooKeeperStatus.setMode("L");
				} else if (line.contains(MODE_STANDALONE)) {
					zooKeeperStatus.setMode("S");
				} else if (line.contains(MODE_OBSERVER)) {
					zooKeeperStatus.setMode("O");
				} else if (line.contains(NODE_COUNT)) {
					zooKeeperStatus.setNodeCount(Integer.parseInt(StringUtil.trimToEmpty(line.replace(NODE_COUNT,
							EMPTY_STRING))));
				} else if (line.contains(STRING_SENT)) {
					zooKeeperStatus.setSent(StringUtil.trimToEmpty(line.replace(STRING_SENT, EMPTY_STRING)));
				} else if (line.contains(STRING_RECEIVED)) {
					zooKeeperStatus.setReceived(StringUtil.trimToEmpty(line.replace(STRING_RECEIVED, EMPTY_STRING)));
				}
				sb.append(line).append("<br/>");
			}
			zooKeeperStatus.setClientConnectionList(clientConnectionList);
			zooKeeperStatus.setStatContent(sb.toString());
		} catch (Exception e) {
			LOG.error("程序出错:" + e.getMessage());
		} finally {
			IOUtil.closeReader(bufferedRead);
		}
	}

	/** 分析一行内容, 判断是否为客户端连接 */
	private boolean analyseLineIfClientConnection(String line) {
		if (StringUtil.isBlank(line)) {
			return false;
		}
		// 标准的一行客户端连接是这样的
		// /1.2.37.111:43681[1](queued=0,recved=434,sent=434)
		line = StringUtil.trimToEmpty(line);
		if (line.startsWith("/") && StringUtil.containsIp(line)) {
			return true;
		}
		return false;
	}

	/**
	 * 进行Telnet连接并进行执行wchs。
	 */
	private void telnetZooKeeperAndHandleWchs(String ip, int port, ZooKeeperStatus zooKeeperStatus) {

		try {
			if (StringUtil.isBlank(ip, port + EMPTY_STRING)) {
				LOG.warn("Ip is empty");
				return;
			}
			String wchsOutput = execCmdBySockset(ip, port, WCHS);

			/**
			 * Example: 59 connections watching 161 paths Total watches:405
			 */
			if (StringUtil.isBlank(wchsOutput)) {
				LOG.warn("No output execute " + StringUtil.replaceSequenced(WCHS, ip, port + EMPTY_STRING));
				return;
			}
			String[] wchsOutputArray = split(wchsOutput);
			if (2 != wchsOutputArray.length) {
				LOG.warn("Illegal output of command "
						+ StringUtil.replaceSequenced(WCHS, ip, port + EMPTY_STRING));
				return;
			}
			String firstLine = wchsOutputArray[0].replace(STRING_CONNECTIONS_WATCHING, WORD_SEPARATOR).replace(
					STRING_PATHS, EMPTY_STRING);
			String[] firstLineArray = firstLine.split(WORD_SEPARATOR);

			final Map<String, Connection> consOfServer = GlobalInstance
					.getZooKeeperClientConnectionMapByClusterIdAndServerIp(ip);

			int watchedPaths = Integer.parseInt(StringUtil.trimToEmpty(firstLineArray[1]));
			zooKeeperStatus.setConnections(consOfServer);
			zooKeeperStatus.setWatchedPaths(watchedPaths);

			// 分析第二行来获取watches数
			String secondtLine = wchsOutputArray[1].replace(STRING_TOTAL_WATCHES, EMPTY_STRING);
			int watches = Integer.parseInt(StringUtil.trimToEmpty(secondtLine));
			zooKeeperStatus.setWatches(watches);
		} catch (Exception e) {
			LOG.error("程序出错：" + e.getMessage());
		}
	}

	/**
	 * 进行Telnet连接并进行执行wchc。
	 * 
	 * @throws Exception
	 */
//	private void sshZooKeeperAndHandleWchc(String ip, int port, ZooKeeperStatus zooKeeperStatus, int clusterId) {
//
//		Map<String, Connection> connectionMapOfCluster = GlobalInstance
//				.getZooKeeperClientConnectionMapByClusterId(clusterId);
//		if (null == connectionMapOfCluster)
//			connectionMapOfCluster = new HashMap<String, Connection>();
//
//		try {
//			if (StringUtil.isBlank(ip, port + EMPTY_STRING)) {
//				LOG.warn("Ip is empty");
//				return;
//			}
//			String wchcOutput = execCmdBySockset(ip, port, WCHC);
//
//			/**
//			 * Example: 59 connections watching 161 paths Total watches:405
//			 */
//			if (StringUtil.isBlank(wchcOutput)) {
//				LOG.warn("No output execute " + StringUtil.replaceSequenced(WCHC, ip, port + EMPTY_STRING));
//				return;
//			}
//
//			StringBuffer wchcOutputWithIp = new StringBuffer();
//			String[] wchcOutputArray = split(wchcOutput);
//			if (0 == wchcOutputArray.length) {
//				LOG.warn("No output of command " + StringUtil.replaceSequenced(WCHC, ip, port + EMPTY_STRING));
//				return;
//			}
//			Map<String, List<String>> watchedPathMap = new HashMap<String, List<String>>();
//			String sessionId = EMPTY_STRING;
//			List<String> watchedPathList = new ArrayList<String>();
//
//			for (String line : wchcOutputArray) {
//				if (StringUtil.isBlank(line)) {
//					wchcOutputWithIp.append(line).append(BR);
//					continue;
//				} else if (line.startsWith("0x")) {
//					// 将上次list放入
//					if (!StringUtil.isBlank(sessionId)) {
//						watchedPathMap.put(sessionId, watchedPathList);
//					}
//
//					sessionId = StringUtil.trimToEmpty(line);
//					Connection conn = connectionMapOfCluster.get(sessionId);
//					if (null != conn)
//						sessionId += conn.getClientIp();
//					wchcOutputWithIp.append(sessionId).append(BR);
//				} else {
//					watchedPathList.add(StringUtil.trimToEmpty(line));
//					wchcOutputWithIp.append(line).append(BR);
//				}
//			}// 遍历wchc返回的内容
//				// 将最后一次list放入
//			if (!StringUtil.isBlank(sessionId)) {
//				Connection conn = connectionMapOfCluster.get(sessionId);
//				if (null != conn)
//					sessionId += "-" + conn.getClientIp();
//				watchedPathMap.put(sessionId, watchedPathList);
//			}
//			LOG.debug(ip + " 的所有Watch情况是:" + watchedPathMap.keySet());
//			zooKeeperStatus.setWatchedPathMap(watchedPathMap);
//			zooKeeperStatus.setWatchedPathMapContent(wchcOutputWithIp.toString());
//		} catch (Exception e) {
//			LOG.error("程序错误: " + e.getMessage());
//			e.printStackTrace();
//		}
//	}

	/**
	 * 进行Telnet连接并进行执行rwps。
	 * 
	 * @throws Exception
	 */
	private void sshZooKeeperAndHandleRwps(String ip, int port, ZooKeeperStatusV2 zooKeeperStatus, int clusterId) {

		try {
			if (StringUtil.isBlank(ip, port + EMPTY_STRING)) {
				LOG.warn("Ip is empty");
				return;
			}
			String rwpsOutput = execCmdBySockset(ip, port, RWPS);

			/**
			 * RealTime R/W Statistics: getChildren2: 0.0
			 * 
			 * createSession: 0.0
			 * 
			 * closeSession: 0.1
			 * 
			 * setData: 11.9
			 * 
			 * setWatches: 0.0
			 * 
			 * getChildren: 27.9
			 * 
			 * delete: 0.0
			 * 
			 * create: 0.0
			 * 
			 * exists: 51.5
			 * 
			 * getDate: 2881.1
			 */
			if (StringUtil.isBlank(rwpsOutput)) {
				LOG.warn("No output execute " + StringUtil.replaceSequenced(RWPS, ip, port + EMPTY_STRING));
				return;
			}

			String[] rwpsOutputArray = split(rwpsOutput);
			if (0 == rwpsOutputArray.length) {
				LOG.warn("No output of command " + StringUtil.replaceSequenced(RWPS, ip, port + EMPTY_STRING));
				return;
			}

			double getChildren2 = 0, createSession = 0, closeSession = 0, setData = 0, setWatches = 0, getChildren = 0, delete = 0, create = 0, exists = 0, getData = 0;

			ZooKeeperStatusV2.RWStatistics rwps = null;

			for (String line : rwpsOutputArray) {

				if (StringUtil.isBlank(line)) {
					continue;
				} else if (line.contains("getChildren2")) {

					line = line.substring(line.indexOf("getChildren2") + ("getChildren2".length() + 1));
					getChildren2 = Math.round(Double.valueOf(StringUtil.trimToEmpty(line)) * 100) / 100;

				} else if (line.contains("createSession")) {

					line = line.substring(line.indexOf("createSession") + ("createSession".length() + 1));
					createSession = Math.round(Double.valueOf(StringUtil.trimToEmpty(line)) * 100) / 100;

				} else if (line.contains("closeSession")) {

					line = line.substring(line.indexOf("closeSession") + ("closeSession".length() + 1));
					closeSession = Math.round(Double.valueOf(StringUtil.trimToEmpty(line)) * 100) / 100;

				} else if (line.contains("setData")) {

					line = line.substring(line.indexOf("setData") + ("setData".length() + 1));
					setData = Math.round(Double.valueOf(StringUtil.trimToEmpty(line)) * 100) / 100;

				} else if (line.contains("setWatches")) {

					line = line.substring(line.indexOf("setWatches") + ("setWatches".length() + 1));
					setWatches = Math.round(Double.valueOf(StringUtil.trimToEmpty(line)) * 100) / 100;

				} else if (line.contains("getChildren")) {

					line = line.substring(line.indexOf("getChildren") + ("getChildren".length() + 1));
					getChildren = Math.round(Double.valueOf(StringUtil.trimToEmpty(line)) * 100) / 100;

				} else if (line.contains("delete")) {

					line = line.substring(line.indexOf("delete") + ("delete".length() + 1));
					delete = Math.round(Double.valueOf(StringUtil.trimToEmpty(line)) * 100) / 100;

				} else if (line.contains("create")) {

					line = line.substring(line.indexOf("create") + ("create".length() + 1));
					create = Math.round(Double.valueOf(StringUtil.trimToEmpty(line)) * 100) / 100;

				} else if (line.contains("exists")) {

					line = line.substring(line.indexOf("exists") + ("exists".length() + 1));
					exists = Math.round(Double.valueOf(StringUtil.trimToEmpty(line)) * 100) / 100;

				} else if (line.contains("getDate")) {

					line = line.substring(line.indexOf("getDate") + ("getDate".length() + 1));
					getData = Math.round(Double.valueOf(StringUtil.trimToEmpty(line)) * 100) / 100;

				} else {
					continue;
				}
			}// 遍历rwps返回的内容

			rwps = new ZooKeeperStatusV2.RWStatistics(getChildren2, createSession, closeSession, setData, setWatches,
					getChildren, delete, create, exists, getData);
			zooKeeperStatus.setRwps(rwps);

		} catch (Exception e) {
			LOG.error("程序错误: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// 检查并进行报警
	private void checkAndAlarm(AlarmSettings alarmSettings, ZooKeeperStatus zooKeeperStatus, String clusterName) {

		if (null == alarmSettings)
			return;

		try {
			boolean needAlarm = false;
			StringBuilder sb = new StringBuilder();
			String maxConnectionPerIp = StringUtil.trimToEmpty(alarmSettings.getMaxConnectionPerIp());
			String maxWatchPerIp = StringUtil.trimToEmpty(alarmSettings.getMaxWatchPerIp());

			if (!StringUtil.isBlank(maxConnectionPerIp)) {
				Map<String, Connection> conns = zooKeeperStatus.getConnections();
				int connectionsPerIp = 0;
				if (null != conns)
					connectionsPerIp = conns.size();

				if (Integer.parseInt(maxConnectionPerIp) < connectionsPerIp) {
					needAlarm = true;
					sb.append(zooKeeperStatus.getIp() + " 上的连接数达到了: " + connectionsPerIp + ", 超过设置的报警阀值: "
							+ maxConnectionPerIp + ".  ");
				}
			}

			if (!StringUtil.isBlank(maxWatchPerIp)) {
				int watchesPerIp = zooKeeperStatus.getWatches();
				if (Integer.parseInt(maxWatchPerIp) < watchesPerIp) {
					needAlarm = true;
					sb.append(zooKeeperStatus.getIp() + " 上的Watch数达到了: " + watchesPerIp + ", 超过设置的报警阀值: "
							+ maxWatchPerIp + ".  ");
				}
			}

			if (needAlarm) {
				LOG.warn("ZooKeeper连接数，Watcher数报警" + sb.toString());
				if (GlobalInstance.needAlarm.get()) {
					String wangwangList = alarmSettings.getWangwangList();
					String phoneList = alarmSettings.getPhoneList();

					ThreadPoolManager.addJobToMessageSendExecutor(new TbMessageSender(new Message(wangwangList,
							"ZooKeeper连接数，Watcher数报警-" + clusterName, clusterName + "-" + sb.toString(),
							Message.MessageType.WANGWANG)));
					ThreadPoolManager.addJobToMessageSendExecutor(new TbMessageSender(new Message(phoneList,
							"ZooKeeper连接数，Watcher数报警-" + clusterName, clusterName + "-" + sb.toString(),
							Message.MessageType.WANGWANG)));
				}
			}// need alarm
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * store taokeeper stat to DB TODO 这个方法要异步处理
	 * 
	 * @param clusterId
	 * @param zooKeeperStatus
	 */
	private void storeTaoKeeperStatToDB(int clusterId, ZooKeeperStatusV2 zooKeeperStatus) {

		try {
			WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
			ReportDAO reportDAO = (ReportDAO) wac.getBean("reportDAO");

			TypeToken<ZooKeeperStatusV2.RWStatistics> type = new TypeToken<ZooKeeperStatusV2.RWStatistics>() {
			};

			String rwStatistics = "";
			if (!ObjectUtil.isBlank(zooKeeperStatus.getRwps())) {
				rwStatistics = new Gson().toJson(zooKeeperStatus.getRwps(), type.getType());
			}

			reportDAO.addTaoKeeperStat(new TaoKeeperStat(clusterId, zooKeeperStatus.getIp(), DateUtil
					.getNowTime(DateFormat.DateTime), DateUtil.getNowTime(DateFormat.Date), MapUtil
					.size(zooKeeperStatus.getConnections()), zooKeeperStatus.getWatches(), Long
					.parseLong(zooKeeperStatus.getSent()), Long.parseLong(zooKeeperStatus.getReceived()),
					zooKeeperStatus.getNodeCount(), rwStatistics));
		} catch (NumberFormatException e) {
			LOG.error("将统计信息记入数据库出错：" + e.getMessage());
			e.printStackTrace();
		} catch (DaoException e) {
			LOG.error("将统计信息记入数据库出错：" + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			LOG.error("将统计信息记入数据库出错：" + e.getMessage());
			e.printStackTrace();
		}
	}

}