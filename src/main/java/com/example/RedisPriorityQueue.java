package com.example;

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RedisPriorityQueue {
    private final String redisUrl;
    private final String queueName;
    private final CloseableHttpClient httpClient;

    public RedisPriorityQueue(String redisUrl, String queueName) {
        this.redisUrl = redisUrl;
        this.queueName = queueName;
        this.httpClient = HttpClients.createDefault();
    }

    public void push(String message, int priority) throws IOException {
        long timestamp = System.currentTimeMillis();
        double score = priority * 1000000000L - timestamp; // Higher priority comes first

        String command = String.format("ZADD %s %f \"%s\"", queueName, score, message);
        sendCommand(command);
    }

    public String pull() throws IOException {
        String command = String.format("ZRANGE %s 0 0 WITHSCORES", queueName);
        String response = sendCommand(command);

        if (response == null || response.isEmpty())
            return null;

        String[] parts = response.split("\n");
        if (parts.length < 2)
            return null;

        String message = parts[0];
        remove(message);
        return message;
    }

    public void remove(String message) throws IOException {
        String command = String.format("ZREM %s \"%s\"", queueName, message);
        sendCommand(command);
    }

    private String sendCommand(String command) throws IOException {
        HttpPost request = new HttpPost(redisUrl);
        request.setEntity(new StringEntity("{\"command\":\"" + command + "\"}", StandardCharsets.UTF_8));
        request.setHeader("Content-Type", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return EntityUtils.toString(response.getEntity());
        }
    }
}
