package com.taobao.taokeeper.monitor.core2;

import java.util.List;

/**
 * 
 * @author pingwei
 * 2014-3-21 下午2:12:09
 */

public abstract class EventListener {
    
    public EventListener() {
        EventDispatcher.addEventListener(this); 
    }
    
    /**
     * 感兴趣的事件列表
     */
    abstract public List<Class<? extends Event>> interest();

    /**
     * 处理事件
     */
    abstract public void onEvent(Event event);
}


