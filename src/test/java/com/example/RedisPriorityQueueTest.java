package com.example;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.resps.Tuple;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RedisPriorityQueueTest {
    private static RedisPriorityQueueService queue;

    @BeforeClass
    public static void setup() {
        queue = new RedisPriorityQueueService();
        queue.clearQueue();
    }

    @Test
    public void testPushAndPull() {
        queue.push("Task C", 10);
        queue.push("Task B", 5);
        queue.push("Task A", 1);

        // Expect Task A to be pulled first (lowest priority number)
        assertEquals("Task A", queue.pull());
        assertEquals("Task B", queue.pull());
        assertEquals("Task C", queue.pull());

        assertNull(queue.pull());
    }

    @Test
    public void testSamePriorityFCFS() {
        queue.push("First", 1);
        queue.push("Second", 1);
        queue.push("Third", 1);

        assertEquals("First", queue.pull());
        assertEquals("Second", queue.pull());
        assertEquals("Third", queue.pull());
    }

    @Test
    public void testViewAll() {
        queue.push("HighPriority", 1);
        queue.push("LowPriority", 100);

        List<Tuple> tasks = queue.viewAll();
        assertEquals("HighPriority", tasks.get(0).getElement());
        assertEquals("LowPriority", tasks.get(1).getElement());

        queue.clearQueue(); // Clean up after test
    }

    @AfterClass
    public static void cleanup() {
        queue.clearQueue();
    }
}
