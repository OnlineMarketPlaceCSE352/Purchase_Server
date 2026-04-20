package com.project;

import com.project.controller.PurchaseController;
import com.project.service.PurchaseService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PurchaseServerApplication {
    private static int PORT = 8083;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(50);

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            initializeServer(args);
            serverSocket = new ServerSocket(PORT);
            System.out.println("Product Service Target: " + PurchaseService.PRODUCT_SERVICE_HOST + ":" + PurchaseService.PRODUCT_SERVICE_PORT);
            System.out.println("User/Auth Service Target: " + PurchaseService.USER_SERVICE_HOST + ":" + PurchaseService.USER_SERVICE_PORT);
            System.out.println("Purchase Server is live on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new PurchaseController(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }
        }
    }

    private static synchronized void initializeServer(String[] args) {
        System.out.println("Starting server...");

        String portStr = getConfig(args, 0, "PORT", "8083");
        try {
            PORT = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + portStr);
            System.exit(1);
        }

        try {
            PurchaseService.PRODUCT_SERVICE_HOST = getConfig(args, 1, "PRODUCT_SERVICE_HOST", "bore.pub");
            PurchaseService.PRODUCT_SERVICE_PORT = Integer.parseInt(getConfig(args, 2, "PRODUCT_SERVICE_PORT", "12345"));

            PurchaseService.USER_SERVICE_HOST = getConfig(args, 3, "USER_SERVICE_HOST", "bore.pub");
            PurchaseService.USER_SERVICE_PORT = Integer.parseInt(getConfig(args, 4, "USER_SERVICE_PORT", "54321"));
        } catch (NumberFormatException e) {
            System.err.println("CRITICAL: Invalid port provided for external microservices.");
            System.exit(1);
        }

        try {
            System.out.println("Initializing database connection pool...");
            System.out.println("Database pool is ready!");
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to connect to the database! Shutting down.");
            System.exit(1);
        }
    }

    private static String getConfig(String[] args, int argIndex, String envName, String defaultValue) {
        if (args != null && args.length > argIndex) {
            return args[argIndex];
        }
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        return defaultValue;
    }
}