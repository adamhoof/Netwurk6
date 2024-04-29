package common;

import com.google.common.eventbus.EventBus;

public class GlobalEventBus {
    private static final EventBus eventBus = new EventBus();

    private GlobalEventBus() {
    }

    public static void register(Object observer) {
        eventBus.register(observer);
    }

    public static void post(Event event) {
        eventBus.post(event);
    }
}
