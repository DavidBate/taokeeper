package com.taobao.taokeeper.monitor.core2;

import com.taobao.taokeeper.monitor.core2.task.CommandTask;
import com.taobao.taokeeper.monitor.core2.task.CommandTaskListener;
import com.taobao.taokeeper.monitor.core2.task.RTTask;
import com.taobao.taokeeper.monitor.core2.task.RTTaskListener;

/**
 * 
 * @author pingwei
 * 2014-3-21 下午2:41:19
 */

public class Events {

	public static class CommandEvent extends Event{

		final static CommandTaskListener listener = new CommandTaskListener();
		
		public CommandTask cmdTask;

		public CommandEvent(CommandTask cmdTask) {
			super();
			this.cmdTask = cmdTask;
		}
	}
	
	
	public static class RTEvent extends Event{
		
		final static RTTaskListener listener = new RTTaskListener();
		public RTTask rtTask;
		public RTEvent(RTTask rtTask) {
			this.rtTask = rtTask;
		}
	}
	
	
}


