package com.taobao.taokeeper.monitor.core2.task;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.taobao.jm.alert.AlertManager;
//import com.taobao.jm.alert.DefaultAlertManager;
import com.taobao.taokeeper.model.AlertInfo;

/**
 * 
 * @author pingwei 2014-3-25 下午1:19:22
 */

public class AlertTask {

	static final Logger log = LoggerFactory.getLogger(AlertTask.class);
//	static final AlertManager alertManager = new DefaultAlertManager("taokeeper", "pingwei@0325");
//	static{
//		alertManager.init();
//	}

	
//	public static void main(String[] args) {
//		System.out.println(alertManager.sendTbWWAlert("平威", "taokeeper报警", "daily test"));
//	}
	AlertInfo alert;
	ExecutorService pool;

	public AlertTask(ExecutorService pool, AlertInfo alert) {
		this.pool = pool;
		this.alert = alert;
	}

	public String taskDesc() {
		return "";
	}

	public void work() {
		if(pool == null){
			alert();
		} else {
			pool.execute(new Runnable() {

				@Override
				public void run() {
					alert();
				}
			});
		}
	}
	
	private void alert(){
//		if (alert == null) {
//			return;
//		}
//		for (String ww : alert.getWangwangAsList()) {
//			log.info(alertManager.sendTbWWAlert(ww, "taokeeper报警", alert.getContent()).toString());
//		}
//		
//		for(String mobile : alert.getMobileAsList()){
//			log.info(alertManager.sendSms(mobile, alert.getContent()).toString());
//		}
	}

}
