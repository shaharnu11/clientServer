package com.client;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class main {
    public static void main(String args[]){
        int port = 8081;
        int maxNumberOfClients = 500;
        int numberOfClients;
        String serverUrl = "http://localhost:" + port;
        Scanner scanner = new Scanner(System.in);
        Random rand = new Random();
        String input;
        ExecutorService executor;

        // read user input - number of clients
        do {
            System.out.print("Please enter the number of HTTP clients : ");
            input = scanner.nextLine();
        }while(!isValidInteger(input));

        numberOfClients = Integer.parseInt(input);

        // limit number of clients, prevent host from crushing
        numberOfClients = Math.min(numberOfClients,maxNumberOfClients);
        if(numberOfClients == maxNumberOfClients)
            System.out.println("Number of clients reduce to " + maxNumberOfClients + " to prevent host from crushing");
        executor = Executors.newFixedThreadPool(numberOfClients);

        for(int i =0;i<numberOfClients;i++){
            int clientId = rand.nextInt(numberOfClients) +1;
            executor.submit(new Sender(clientId,serverUrl));
        }
        try {
            int exit = System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Sender.setActive(false);
        executor.shutdown();
        System.out.println("Executer shutdown");
    }

    private static boolean isValidInteger(String s) {
        int res;
        try {
             res = Integer.parseInt(s);
        } catch(NumberFormatException | NullPointerException e) {
            System.out.println("Wrong format or bad number");
            return false;
        }
        if(res <= 0) {
            System.out.println("Please enter number greater than 0");
            return false;
        }

        return true;
    }
}
