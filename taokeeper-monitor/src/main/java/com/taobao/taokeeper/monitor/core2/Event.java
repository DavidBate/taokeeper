package com.taobao.taokeeper.monitor.core2;

import java.util.Collections;
import java.util.List;

/**
 * 
 * @author pingwei 2014-3-21 下午2:11:01
 */

public abstract class Event {
	
	protected List<Event> implyEvents() {
		return Collections.emptyList();
	}

}
