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

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <pre>
 * 异步分派
 * </pre>
 * @author catforward
 */
class AsyncDispatcher {

    final static class MessageWithSubscriber {
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
     * @param obj
     * @param subscribers
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