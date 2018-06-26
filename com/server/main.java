package com.server;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

public class main {

    public static void main(String args[]) throws InterruptedException {

        int maxNumberOfThreads = 4;
        int port = 8081;
        HttpServer server = null;
        LinkedBlockingQueue<Runnable> tasksQueue = new LinkedBlockingQueue<>();
        ExecutorService threadPoolExecutor =
                new ThreadPoolExecutor(
                        maxNumberOfThreads,
                        maxNumberOfThreads,
                        10,
                        TimeUnit.SECONDS,
                        tasksQueue
                );

        // starting and initialize server
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            System.out.println("Unable to create new server (probably the port " + port + " is already in use)");
            e.printStackTrace();
        }

        System.out.println("server is starting");
        assert server != null;
        server.setExecutor(threadPoolExecutor);
        server.createContext("/", new Receiver());
        server.start();


        // press enter to shutdown the server
        try {
            int exit = System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        threadPoolExecutor.shutdown();
        System.out.println("server is shutting down");


        // wait until all left tasks are done
        while (tasksQueue.size() != 0) {
            System.out.println("There are still " + tasksQueue.size() + " tasks left in queue");
            sleep(1000);
        }
        server.stop(0);
        System.out.println("server has shut down");
    }


}
