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
package pers.ebr.cli.core.util.bus;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <pre>
 * 异步分派
 * </pre>
 * @author l.gong
 */
class AsyncDispatcher {

    static final class MessageWithSubscriber {
        final Object message;
        final MessageSubscriber subscriber;
        MessageWithSubscriber(Object message, MessageSubscriber subscriber) {
            this.message = message;
            this.subscriber = subscriber;
        }
    }

    /** 全局的消息队列 */
    private final ConcurrentLinkedQueue<MessageWithSubscriber> queue;
    private final AsyncMessageBus bus;

    AsyncDispatcher(AsyncMessageBus bus) {
        this.bus = bus;
        this.queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * <pre>
     * 向消息队列添加消息后
     * 向订阅者推送消息
     * </pre>
     * @param obj 分发对象
     * @param subscribers 订阅者
     */
    void dispatch(Object obj, Iterator<MessageSubscriber> subscribers) {
        while (subscribers.hasNext()) {
            this.queue.add(new MessageWithSubscriber(obj, subscribers.next()));
        }

        MessageWithSubscriber ms;
        while ((ms = queue.poll()) != null) {
            dispatchMessage(ms);
        }
    }

    private void dispatchMessage(MessageWithSubscriber ms) {
        this.bus.getExecutor().execute(() -> {
            try {
                ms.subscriber.onMessage(ms.message);
            } catch (RuntimeException e) {
                bus.handleSubscriberException(e);
            }
        });
    }
}
