package tsm.ebr.thin.bus;

import junit.framework.TestCase;
import tsm.ebr.base.Broker;
import tsm.ebr.base.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Forked form The TestCase from Guava
 */
public class AsyncMessageBusTest extends TestCase implements MessageSubscriber<Message>{

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
        bus = new AsyncMessageBus("TEST", executor);
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
        bus.post(msg);

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
