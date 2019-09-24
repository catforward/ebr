/*
  MIT License

  Copyright (c) 2019 catforward

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.

 */
package ebr.core.util.bus;

import ebr.core.util.AppLogger;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;

import static ebr.core.util.MiscUtils.checkNotNull;

/**
 * <pre>
 *　异步消息总线
 * </pre>
 * @author catforward
 */
public class AsyncMessageBus {
    private final Executor executor;
    private final AsyncDispatcher dispatcher;
    private final Map<Class<?>, CopyOnWriteArraySet<MessageSubscriber>> topicSubscriber;

    public AsyncMessageBus(Executor executor) {
        this.executor = executor;
        this.dispatcher = new AsyncDispatcher(this);
        this.topicSubscriber = new ConcurrentHashMap<>();
    }

    Executor getExecutor() {
        return this.executor;
    }

    void handleSubscriberException(Exception ex) {
        AppLogger.dumpError(ex);
    }

    /**
     * <pre>
     * 订阅某种类型的消息
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
        subscribers.add(subscriber);
    }

    /**
     * <pre>
     *　退订所有类型的消息
     * </pre>
     * @param subscriber
     */
    public void unsubscribe(MessageSubscriber subscriber) {
        checkNotNull(subscriber);
        for (var subscribers : this.topicSubscriber.values()) {
            subscribers.remove(subscriber);
        }
    }

    /**
     * <pre>
     * 发送消息
     * </pre>
     * @param obj
     */
    public void publish(Object obj) {
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