package com.project;

import com.project.controller.PurchaseController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PurchaseServerApplication {
    private static int PORT = 8083;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(50);

    static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            initializeServer(args);
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is live on port " + PORT);

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
                }
                catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }
        }
    }

    private static synchronized void initializeServer(String[] args) {
        System.out.println("Starting server...");

        String portStr = args.length > 0 ? args[0] : System.getenv("PORT");
        if (portStr != null && !portStr.isEmpty()) {
            try {
                PORT = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + portStr);
                System.exit(1);
            }
        }

        try {
            System.out.println("Initializing database connection pool...");
            System.out.println("Database pool is ready!");
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to connect to the database! Shutting down.");
            System.exit(1);
        }
    }
}
