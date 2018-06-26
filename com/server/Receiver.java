package com.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class Receiver implements HttpHandler {

    private static ConcurrentHashMap<Integer, long[]> timeFrames = new ConcurrentHashMap<>();

    // use to sync creation of first clientId timeFrame array
    private static final Object mutex = new Object();

    public void handle(HttpExchange t) throws IOException {

        int timeFrameWindow = 5000;
        int maxReqPerTimeFrame = 5;

        int clientId = Integer.parseInt(t.getRequestURI().getQuery().split("=")[1]);
        long[] timeFrame = timeFrames.get(clientId);
        long currentTime;

        //initialize clientId array
        if (timeFrame == null) {
            currentTime = System.currentTimeMillis();
            synchronized (mutex) {
                timeFrame = timeFrames.get(clientId);
                if (timeFrame == null) {
                    timeFrame = new long[]{currentTime, 1};
                    timeFrames.put(clientId,timeFrame);
                }
            }
        }

        // prevent race condition of different thread dealing with the same clientId
        synchronized (timeFrames.get(clientId)) {
            System.out.println("Thread id: " + Thread.currentThread().getId() + "  handle clientId: " + clientId + " request");
            timeFrame = timeFrames.get(clientId);
            currentTime = System.currentTimeMillis();

            // check time frame need to be reset
            if (currentTime - timeFrame[0] > timeFrameWindow) {
                timeFrame[0] = currentTime;
                timeFrame[1] = 1;
                t.sendResponseHeaders(200, 0);
            } else {
                timeFrame[1]++;

                // check for max request from same client in the current time frame
                if (timeFrame[1] <= maxReqPerTimeFrame) {
                    t.sendResponseHeaders(200, 0);
                } else {
                    t.sendResponseHeaders(503, 0);
                }
            }
        }
    }
}
