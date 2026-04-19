package com.project.controller;

import com.project.dto.Request;
import com.project.dto.Response;
import com.project.service.PurchaseService;
import com.project.util.Method;
import com.project.util.RestHandler;

import java.io.*;
import java.net.Socket;

public class PurchaseController implements Runnable {
    private final Socket socket;
    private final PurchaseService purchaseService;

    public PurchaseController(Socket socket) {
        this.socket = socket;
        this.purchaseService = new PurchaseService();
    }

    @Override
    public void run() {
        BufferedReader input = null;
        OutputStream output = null;
        try {
            output = socket.getOutputStream();
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Request request = RestHandler.parseRequest(input);
            if (request == null) return;
            Response response = handleRoute(request);

            RestHandler.sendResponse(output, response);
        } catch (IOException e) {
            System.err.println("Error handling request: " + e.getMessage());
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private Response handleRoute(Request request) throws IOException {
        Response response = new Response();

        if (request.getMethod() == Method.GET && request.getPath().equals("/api/purchases")) {
            response.setBody(purchaseService.getAllPurchases());
        } else if (request.getMethod() == Method.POST && request.getPath().equals("/api/purchases")) {
            response.setStatusCode(201);
            response.setStatusText("Created");
            response.setBody("{\"message\": \"Purchase created successfully\"}");
        } else {
            response.setStatusCode(404);
            response.setStatusText("Not Found");
            response.setBody("{\"error\": \"Endpoint not found\"}");
        }
        return response;
    }
}
