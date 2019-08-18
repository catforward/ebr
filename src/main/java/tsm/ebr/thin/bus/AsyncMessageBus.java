<<<<<<< .mine
/**
 * MIT License
 *
 * Copyright (c) 2019 catforward
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package tsm.ebr.thin.bus;

import tsm.ebr.util.LogUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import static tsm.ebr.util.MiscUtils.checkNotNull;

/**
 * <pre>
 *���첽��Ϣ����
 * </pre>
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

    /**
     * <pre>
     * ����ĳ�����͵���Ϣ
     * </pre>
     * @param topic
     * @param subscriber
     */
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

    /**
     * <pre>
     *���˶��������͵���Ϣ
     * </pre>
     * @param subscriber
     */
    public void unsubscribe(MessageSubscriber subscriber) {
        checkNotNull(subscriber);
        for (var subscribers : this.topicSubscriber.values()) {
            if (subscribers.contains(subscriber)) {
                subscribers.remove(subscriber);
            }
        }
    }

    /**
     * <pre>
     * ������Ϣ
     * </pre>
     * @param obj
     */
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
=======
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















































>>>>>>> .theirs
