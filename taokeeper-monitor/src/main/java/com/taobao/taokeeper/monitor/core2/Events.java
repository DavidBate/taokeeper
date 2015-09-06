package com.taobao.taokeeper.monitor.core2;

import com.taobao.taokeeper.monitor.core2.task.AlertRuleCheckTask;
import com.taobao.taokeeper.monitor.core2.task.AlertTask;
import com.taobao.taokeeper.monitor.core2.task.CommandTask;
import com.taobao.taokeeper.monitor.core2.task.EventListeners.AlertRuleCheckTaskListener;
import com.taobao.taokeeper.monitor.core2.task.EventListeners.AlertTaskListener;
import com.taobao.taokeeper.monitor.core2.task.EventListeners.CommandTaskListener;
import com.taobao.taokeeper.monitor.core2.task.EventListeners.RTTaskListener;
import com.taobao.taokeeper.monitor.core2.task.RTTask;

/**
 * 
 * @author pingwei 2014-3-21 下午2:41:19
 */

public class Events {

	public static class CommandEvent extends Event {

		final static CommandTaskListener listener = new CommandTaskListener();

		public CommandTask cmdTask;

		public CommandEvent(CommandTask cmdTask) {
			super();
			this.cmdTask = cmdTask;
		}
	}

	public static class RTEvent extends Event {

		final static RTTaskListener listener = new RTTaskListener();
		public RTTask rtTask;

		public RTEvent(RTTask rtTask) {
			this.rtTask = rtTask;
		}
	}

	public static class AlertEvent extends Event {
		
		final static AlertTaskListener listener = new AlertTaskListener();
		public final AlertTask alertTask;

		public AlertEvent(AlertTask alertTask) {
			super();
			this.alertTask = alertTask;
		}

	}
	
	public static class AlertRuleCheckEvent extends Event{
		final static AlertRuleCheckTaskListener listener = new AlertRuleCheckTaskListener();
		public final AlertRuleCheckTask checkTask;
		public AlertRuleCheckEvent(AlertRuleCheckTask task){
			this.checkTask = task;
		}
	}

}
