package tsm.ebr.thin.bus;

import tsm.ebr.util.LogUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

import static tsm.ebr.util.MiscUtils.checkNotNull;

/**
 *
 * @author catforward
 */
public class AsyncMessageBus {
    private static final Logger logger = Logger.getLogger(AsyncMessageBus.class.getCanonicalName());
    private final String identifier;
    private final Executor executor;
    private final AsyncDispatcher dispatcher;
    private final Map<Class<?>, CopyOnWriteArraySet<MessageSubscriber>> topicSubscriber;

    public AsyncMessageBus(String tag, Executor executor) {
        this.identifier = tag;
        this.executor = executor;
        this.dispatcher = new AsyncDispatcher(this);
        this.topicSubscriber = new ConcurrentHashMap<>();
    }

    Executor getExecutor() {
        return this.executor;
    }

    void handleSubscriberException(Exception ex) {
        LogUtils.dumpError(ex);
    }

    public void subscribe(Class<?> topic, MessageSubscriber subscriber) {
        checkNotNull(topic);
        checkNotNull(subscriber);
        CopyOnWriteArraySet<MessageSubscriber> subscribers = this.topicSubscriber.getOrDefault(topic, null);
        if (subscribers == null) {
            subscribers = new CopyOnWriteArraySet<>();
            this.topicSubscriber.put(topic, subscribers);
        }
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    public void unsubscribe(MessageSubscriber subscriber) {
        checkNotNull(subscriber);
        for (var subscribers : this.topicSubscriber.values()) {
            if (subscribers.contains(subscriber)) {
                subscribers.remove(subscriber);
            }
        }
    }

    public void post(Object obj) {
        Class<?> topic = checkNotNull(obj).getClass();
        CopyOnWriteArraySet<MessageSubscriber> subscribers = this.topicSubscriber.getOrDefault(topic, null);
        if (subscribers != null) {
            Iterator<MessageSubscriber> inter = subscribers.iterator();
            if (inter.hasNext()) {
                this.dispatcher.dispatch(obj, inter);
            }
        }
    }
}
