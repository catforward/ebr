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

import junit.framework.TestCase;
import pers.ebr.cli.core.Broker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static pers.ebr.cli.core.Message.MSG_FROM_BROKER_ID;
import static pers.ebr.cli.core.Message.MSG_TO_BROKER_ID;

/**
 * Forked form The TestCase from Guava
 * @author l.gong
 */
public class AsyncMessageBusTest extends TestCase implements MessageSubscriber {

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
    private HashMap<String, Map<String, Object>> result;

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
        bus.subscribe(TEST_ID, this);
        HashMap<String, Object> data = new HashMap<>();
        data.put(MSG_FROM_BROKER_ID, Broker.Id.APP);
        data.put(MSG_TO_BROKER_ID, Broker.Id.APP);
        bus.publish(TEST_ID, data);

        assertFalse(result.containsKey(TEST_ID));

        List<Runnable> tasks = executor.getTasks();
        assertTrue(tasks.size() == 1);

        tasks.get(0).run();

        assertTrue(result.containsKey(TEST_ID));
        Map<String, Object> msgData = result.get(TEST_ID);
        assertTrue(Broker.Id.APP.equals(msgData.get(MSG_FROM_BROKER_ID)));
        assertTrue(Broker.Id.APP.equals(msgData.get(MSG_TO_BROKER_ID)));
    }

    /**
     * 接受消息
     * @param topic 主题
     * @param message 消息体
     */
    @Override
    public void onMessage(String topic, Map<String, Object> message) {
        if (result.containsKey(topic)) {
            throw new RuntimeException("no....");
        }
        result.put(topic, message);
    }
}