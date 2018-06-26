package com.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import static java.lang.Thread.sleep;

public class Sender implements  Runnable{
    private static Object mutex = new Object();
    private static volatile boolean  active = true;

    private int clientId;
    private int maxWaitingTimeInMillis = 10000;
    private Random rand = new Random();
    private URL urlObj = null;
    private HttpURLConnection con = null;


    Sender(int clientId, String serverUrl){
        this.clientId = clientId;
        try {
            this.urlObj = new URL(serverUrl + "/?clientId=" + clientId);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.println("clientId : " + this.clientId + " (" +Thread.currentThread().getId() +") is running");
        while(active) {
            try {
                sendGet();
                sleep(rand.nextInt(this.maxWaitingTimeInMillis) + 1);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("clientId : " + this.clientId + " (" +Thread.currentThread().getId() +") has been stopped");
    }

    private void sendGet() {
        openConnection();
        int responseCode = 0;
        try {
            responseCode = con.getResponseCode();
        } catch (ConnectException e) {
            System.out.println("Unable to connect to server (trying again later)");

            // only one thread keep trying the connection
            synchronized (mutex) {
                while(!checkConnection()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("clientId : " + this.clientId + " (" +Thread.currentThread().getId() +") Response Code : " + responseCode);
        con.disconnect();
    }

    private boolean checkConnection() {
        try {
            openConnection();
            int responseCode = con.getResponseCode();
        } catch (ConnectException e) {
            System.out.println("Unable to connect to server (trying again later) - check");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;

        }
        return true;
    }

    private void openConnection(){
        try {
            this.con = (HttpURLConnection) this.urlObj.openConnection();
            con.setRequestMethod("GET");
        } catch (IOException e) {
            System.out.println("clientId : " + this.clientId + "(" +Thread.currentThread().getId() + ") was unable to open new connection");
            e.printStackTrace();
        }
    }

    public static void setActive(boolean newActive){
        active = newActive;
    }
}
