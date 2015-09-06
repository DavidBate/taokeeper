package com.taobao.taokeeper.monitor.core2.task;

import java.util.ArrayList;
import java.util.List;

import com.taobao.taokeeper.monitor.core2.Event;
import com.taobao.taokeeper.monitor.core2.EventListener;
import com.taobao.taokeeper.monitor.core2.Events.AlertEvent;
import com.taobao.taokeeper.monitor.core2.Events.AlertRuleCheckEvent;
import com.taobao.taokeeper.monitor.core2.Events.CommandEvent;
import com.taobao.taokeeper.monitor.core2.Events.RTEvent;

/**
 * 
 * @author pingwei 2014-3-25 下午1:17:32
 */

public class EventListeners {
	public static class CommandTaskListener extends EventListener {

		@Override
		public List<Class<? extends Event>> interest() {
			List<Class<? extends Event>> list = new ArrayList<Class<? extends Event>>();
			list.add(CommandEvent.class);
			return list;
		}

		@Override
		public void onEvent(Event event) {
			CommandEvent ce = (CommandEvent) event;
			CommandTask task = ce.cmdTask;
			task.work();
		}
	}
	
	
	public static class RTTaskListener extends EventListener{

		@Override
		public List<Class<? extends Event>> interest() {
			List<Class<? extends Event>> list = new ArrayList<Class<? extends Event>>();
			list.add(RTEvent.class);
			return list;
		}

		@Override
		public void onEvent(Event event) {
			RTEvent e = (RTEvent)event;
			e.rtTask.work();
		}

	}
	
	public static class AlertTaskListener extends EventListener{

		@Override
		public List<Class<? extends Event>> interest() {
			List<Class<? extends Event>> list = new ArrayList<Class<? extends Event>>();
			list.add(AlertEvent.class);
			return list;
		}

		@Override
		public void onEvent(Event event) {
			AlertEvent e = (AlertEvent)event;
			e.alertTask.work();
		}
	}
	
	public static class AlertRuleCheckTaskListener extends EventListener{

		@Override
		public List<Class<? extends Event>> interest() {
			List<Class<? extends Event>> list = new ArrayList<Class<? extends Event>>();
			list.add(AlertRuleCheckEvent.class);
			return list;
		}

		@Override
		public void onEvent(Event event) {
			AlertRuleCheckEvent e = (AlertRuleCheckEvent)event;
			e.checkTask.work();
		}
		
	}
}
