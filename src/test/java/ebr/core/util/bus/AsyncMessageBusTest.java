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
package ebr.core.util.bus;

import junit.framework.TestCase;
import ebr.core.base.Broker;
import ebr.core.base.Message;
import ebr.core.util.bus.AsyncMessageBus;
import ebr.core.util.bus.MessageSubscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Forked form The TestCase from Guava
 * @author catforward
 */
public class AsyncMessageBusTest extends TestCase implements MessageSubscriber<Message> {

    static class FakeExecutor implements Executor {
        List<Runnable> tasks = new ArrayList<>();

        @Override
        public void execute(Runnable task) {
            tasks.add(task);
        }

        public List<Runnable> getTasks() {
            return tasks;
        }
    }

    private FakeExecutor executor;
    private AsyncMessageBus bus;
    private HashMap<String, Message> result;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        result = new HashMap<>();
        executor = new FakeExecutor();
        bus = new AsyncMessageBus(executor);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        bus.unsubscribe(this);
        result.clear();
    }

    public void testBasic_01() {
        final String TEST_ID = "test_01";
        bus.subscribe(Message.class, this);
        Message msg = new Message(TEST_ID, Broker.Id.APP, Broker.Id.APP);
        bus.publish(msg);

        assertFalse(result.containsKey(TEST_ID));

        List<Runnable> tasks = executor.getTasks();
        assertTrue(tasks.size() == 1);

        tasks.get(0).run();

        assertTrue(result.containsKey(TEST_ID));
        assertTrue(msg.equals(result.get(TEST_ID)));
    }

    /**
     * 接受消息
     *
     * @param message 消息体
     */
    @Override
    public void onMessage(Message message) {
        if (result.containsKey(message.act)) {
            throw new RuntimeException("no....");
        }
        result.put(message.act, message);
    }
}