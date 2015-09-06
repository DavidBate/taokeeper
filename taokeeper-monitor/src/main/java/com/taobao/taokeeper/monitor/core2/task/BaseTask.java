package com.taobao.taokeeper.monitor.core2.task;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author pingwei
 * 2014-3-23 下午11:12:22
 */

public abstract class BaseTask {
	static final Logger log = LoggerFactory.getLogger(BaseTask.class);

	ExecutorService pool;
	String host;
	int port;
	public BaseTask(ExecutorService pool, String host, int port) {
		this.pool = pool;
		this.host = host;
		this.port = port;
	}
	
	public abstract void work();
	
	public abstract String taskDesc();

	public ExecutorService getPool() {
		return pool;
	}

	public void setPool(ExecutorService pool) {
		this.pool = pool;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	
}


