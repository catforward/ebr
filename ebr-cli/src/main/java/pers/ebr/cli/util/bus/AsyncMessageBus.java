/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pers.ebr.cli.util.bus;

import pers.ebr.cli.util.AppLogger;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;

import static pers.ebr.cli.util.MiscUtils.checkNotNull;

/**
 * <pre>
 *　异步消息总线
 * </pre>
 * @author l.gong
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
     * @param topic 订阅对象
     * @param subscriber 订阅者
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
     * @param subscriber 订阅者
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
     * @param obj 分发对象
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
