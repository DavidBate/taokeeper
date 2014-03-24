package com.taobao.taokeeper.monitor.core2.task;

import java.util.ArrayList;
import java.util.List;

import com.taobao.taokeeper.monitor.core2.Event;
import com.taobao.taokeeper.monitor.core2.EventListener;
import com.taobao.taokeeper.monitor.core2.Events.RTEvent;

/**
 * 
 * @author pingwei
 * 2014-3-23 下午11:25:08
 */

public class RTTaskListener extends EventListener{

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


