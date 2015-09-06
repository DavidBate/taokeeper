package com.taobao.taokeeper.monitor.core2.task;

import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.StringUtils;

import com.taobao.taokeeper.common.util.ZKDataUtil;
import com.taobao.taokeeper.model.RwpsInfo;
import com.taobao.taokeeper.model.SrvrInfo;
import com.taobao.taokeeper.model.StatInfo;
import com.taobao.taokeeper.model.WchsInfo;
import com.taobao.taokeeper.monitor.core2.MonitorUtils;
import com.taobao.taokeeper.monitor.core2.ZookeeperData;

/**
 * 
 * @author pingwei 2014-3-21 下午2:43:23
 */

public class CommandTask extends BaseTask {

	public static final String CMD_SRVR = "srvr";
	public static final String CMD_WCHS = "wchs";
	public static final String CMD_STAT = "stat";
	public static final String CMD_RWPS = "rwps";

	
	public CommandTask(ExecutorService pool, String host, int port, String command) {
		super(pool, host, port);
		this.command = command;
	}

	@Override
	public void work() {
		pool.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					log.info("run task: " + taskDesc());
					String result = ZKDataUtil.execCmdBySocket(host, port, command);
					if (StringUtils.equals(command, CMD_SRVR)) {
						SrvrInfo info = SrvrInfo.parse(result);
						ZookeeperData.srvrMap.put(MonitorUtils.hostId(host, port), info);
					} else if (StringUtils.equals(command, CMD_WCHS)) {
						WchsInfo info = WchsInfo.parse(result);
						ZookeeperData.wchsMap.put(MonitorUtils.hostId(host, port), info);
					} else if (StringUtils.equals(command, CMD_STAT)) {
						StatInfo info = StatInfo.parse(result);
						ZookeeperData.statMap.put(MonitorUtils.hostId(host, port), info);
					} else if (StringUtils.equals(command, CMD_RWPS)) {
						RwpsInfo info = RwpsInfo.parse(result);
						ZookeeperData.rwpsMap.put(MonitorUtils.hostId(host, port), info);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally{
				}
			}
		});
	}

	String command;


	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
	
	@Override
	public String taskDesc() {
		return toString();
	}

	@Override
	public String toString() {
		return "CommandTask [command=" + command + ", host=" + host + ", port=" + port + "]";
	}

	
}
