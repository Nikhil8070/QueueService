package com.example;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class InMemoryPriorityQueueTest {
    private InMemoryPriorityQueue queue;
    private String queueUrl = "https://sqs.ap-1.amazonaws.com/007/MyQueue";

    @Before
    public void setup() {
        queue = new InMemoryPriorityQueue(5); // 5 seconds visibility timeout
    }

    @Test
    public void testSendMessage() {
        queue.push(queueUrl, "Good message!", 1);
        Message msg = queue.pull(queueUrl);

        assertNotNull(msg);
        assertEquals("Good message!", msg.getBody());
    }

    @Test
    public void testPriorityOrdering() {
        queue.push(queueUrl, "Message 1", 1);
        queue.push(queueUrl, "Message 2", 3); // Higher priority
        queue.push(queueUrl, "Message 3", 2);

        assertEquals("Message 2", queue.pull(queueUrl).getBody()); // Highest priority first
        assertEquals("Message 3", queue.pull(queueUrl).getBody()); // Next highest priority
        assertEquals("Message 1", queue.pull(queueUrl).getBody()); // Lowest priority
    }

    @Test
    public void testFCFSWithinSamePriority() {
        queue.push(queueUrl, "Message A", 2);
        queue.push(queueUrl, "Message B", 2);

        assertEquals("Message A", queue.pull(queueUrl).getBody()); // FCFS order
        assertEquals("Message B", queue.pull(queueUrl).getBody());
    }

    @Test
    public void testPullEmptyQueue() {
        Message msg = queue.pull(queueUrl);
        assertNull(msg);
    }

    @Test
    public void testDoublePull() {
        queue.push(queueUrl, "Message A.", 2);
        queue.pull(queueUrl);
        Message msg = queue.pull(queueUrl);
        assertNull(msg);
    }

    @Test
    public void testDeleteMessage() {
        queue.push(queueUrl, "Message X", 2);
        Message msg = queue.pull(queueUrl);
        queue.delete(queueUrl, msg.getReceiptId());
        msg = queue.pull(queueUrl);
        assertNull(msg);
    }

    @Test
    public void testVisibilityTimeout() throws InterruptedException {
        queue.push(queueUrl, "Hidden Message", 5);
        Message msg = queue.pull(queueUrl);

        assertNotNull(msg);
        assertNull(queue.pull(queueUrl)); // Message is temporarily invisible

        Thread.sleep(6000); // Wait for visibility timeout
        assertNotNull(queue.pull(queueUrl)); // Now visible again
    }
}
