package xyz.fz.docdoc.helper.event;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus {
    private static ConcurrentHashMap<Class, List<EventListener>> EVENT_MAP = new ConcurrentHashMap<>();

    public static void addListener(EventListener eventListener) {
        Type[] types = eventListener.getClass().getGenericInterfaces();
        Type[] params = ((ParameterizedType) types[0]).getActualTypeArguments();
        Class clazz = (Class) params[0];
        List<EventListener> listeners = EVENT_MAP.get(clazz);
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(eventListener);
        EVENT_MAP.put(clazz, listeners);
    }

    @SuppressWarnings("unchecked")
    public static void publishEvent(Event event) {
        List<EventListener> listeners = EVENT_MAP.get(event.getClass());
        if (listeners != null) {
            for (EventListener listener : listeners) {
                listener.on(event);
            }
        }
    }
}
