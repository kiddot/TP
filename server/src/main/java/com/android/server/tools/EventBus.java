package com.android.server.tools;

import com.android.server.api.event.Event;
import com.android.server.tools.thread.pool.ThreadPoolManager;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kiddo on 17-7-13.
 */

public class EventBus {
    private final Logger LOGGER = LoggerFactory.getLogger(EventBus.class);
    public static final EventBus I = new EventBus();
    private final com.google.common.eventbus.EventBus eventBus;

    public EventBus() {
        //Executor executor =
        ExecutorService executor = Executors.newSingleThreadExecutor();
        eventBus = new AsyncEventBus(executor, new SubscriberExceptionHandler() {
            @Override
            public void handleException(Throwable throwable, SubscriberExceptionContext subscriberExceptionContext) {

            }
        });
    }

    public void post(Event event) {
        eventBus.post(event);
    }

    public void register(Object bean) {
        eventBus.register(bean);
    }

    public void unregister(Object bean) {
        eventBus.unregister(bean);
    }
}
