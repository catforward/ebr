package tsm.ebr.thin.bus;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

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
        this.queue = new ConcurrentLinkedQueue();
    }

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
        this.bus.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ms.subscriber.onMessage(ms.message);
                } catch (RuntimeException e) {
                    bus.handleSubscriberException(e);
                }
            }
        });
    }
}
