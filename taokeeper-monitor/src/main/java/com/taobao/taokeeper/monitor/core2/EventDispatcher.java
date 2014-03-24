package com.taobao.taokeeper.monitor.core2;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author pingwei
 * 2014-3-21 下午2:10:08
 */

public class EventDispatcher {
	/**
     * 添加事件监听器
     */
    static public void addEventListener(EventListener listener) {
        for (Class<? extends Event> type : listener.interest()) {
            getListenerList(type).addIfAbsent(listener);
        }
    }

    static public void fireEvent(Event event) {
        if (null == event) {
            return;
        }

        for (Event implyEvent : event.implyEvents()) {
            try {
                if (event != implyEvent) { 
                    fireEvent(implyEvent);
                }
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
        }

        for (EventListener listener : getListenerList(event.getClass())) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
        }
    }

    static CopyOnWriteArrayList<EventListener> getListenerList(Class<? extends Event> eventType) {
        CopyOnWriteArrayList<EventListener> listeners = listenerMap.get(eventType);
        if (null == listeners) {
            listeners = new CopyOnWriteArrayList<EventListener>();
            listenerMap.put(eventType, listeners);
        }
        return listeners;
    }


    static private final Logger log = LoggerFactory.getLogger(EventDispatcher.class);
    
    static final Map<Class<? extends Event>, CopyOnWriteArrayList<EventListener>> listenerMap //
    = new HashMap<Class<? extends Event>, CopyOnWriteArrayList<EventListener>>();
}


