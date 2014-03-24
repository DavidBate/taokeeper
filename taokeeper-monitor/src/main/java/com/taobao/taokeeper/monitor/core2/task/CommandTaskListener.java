package com.taobao.taokeeper.monitor.core2.task;

import java.util.ArrayList;
import java.util.List;

import com.taobao.taokeeper.monitor.core2.Event;
import com.taobao.taokeeper.monitor.core2.EventListener;
import com.taobao.taokeeper.monitor.core2.Events.CommandEvent;

/**
 * 
 * @author pingwei
 * 2014-3-21 下午2:44:22
 */

public class CommandTaskListener extends EventListener{
	
	
	@Override
	public List<Class<? extends Event>> interest() {
		List<Class<? extends Event>> list = new ArrayList<Class<? extends Event>>();
		list.add(CommandEvent.class);
		return list;
	}

	@Override
	public void onEvent(Event event) {
		CommandEvent ce = (CommandEvent)event;
		CommandTask task = ce.cmdTask;
		task.work();
	}
	
}


