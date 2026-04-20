package com.project.controller;

import com.project.dto.Request;
import com.project.dto.Response;
import com.project.service.PurchaseService;
import com.project.util.Method;
import com.project.util.RestHandler;
import com.project.util.RouteKey;
import com.project.util.SecurityUtils;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class PurchaseController implements Runnable {
    private final Socket socket;
    private final PurchaseService purchaseService;
    private final Map<RouteKey, RequestHandler> routes = new HashMap<>();

    public PurchaseController(Socket socket) {
        this.socket = socket;
        this.purchaseService = new PurchaseService();
        initializeRoutes();
    }

    private void initializeRoutes() {
        routes.put(new RouteKey(Method.GET, "/api/purchases"), this::handleGetAllPurchases);
        routes.put(new RouteKey(Method.POST, "/api/purchases"), this::handleCreatePurchase);
        routes.put(new RouteKey(Method.DELETE, "/api/purchases/:id"), this::handleDeletePurchase);
        routes.put(new RouteKey(Method.GET, "/api/purchases/bought"), this::handleGetBoughtPurchases);
        routes.put(new RouteKey(Method.GET, "/api/purchases/sold"), this::handleGetSoldPurchases);
    }

    @Override
    public void run() {
        BufferedReader input = null;
        PrintWriter output = null;
        try {
            output = new PrintWriter(socket.getOutputStream(), false);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Request request = RestHandler.parseRequest(input);
            if (request == null) return;

            Response response = handleRoute(request);
            RestHandler.sendResponse(output, response);

        } catch (IOException e) {
            System.err.println("Error handling request: " + e.getMessage());
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) {
                    input.close();
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private Response handleRoute(Request request) {
        RouteKey key = new RouteKey(request.getMethod(), request.getPath());
        RequestHandler handler = routes.get(key);

        // Check for dynamic routes
        if (handler == null) {
            for (Map.Entry<RouteKey, RequestHandler> entry : routes.entrySet()) {
                RouteKey registeredKey = entry.getKey();
                if (registeredKey.method() == request.getMethod() && matchPath(registeredKey.path(), request.getPath())) {
                    handler = entry.getValue();
                    break;
                }
            }
        }

        if (handler == null) {
            return errorResponse(404, "Not Found", "Endpoint not found");
        }

        try {
            return handler.handle(request);
        } catch (Exception e) {
            return errorResponse(400, "Bad Request", e.getMessage());
        }
    }

    private boolean matchPath(String registeredPath, String actualPath) {
        String[] registeredParts = registeredPath.split("/");
        String[] actualParts = actualPath.split("/");

        if (registeredParts.length != actualParts.length) {
            return false;
        }

        for (int i = 0; i < registeredParts.length; i++) {
            if (!registeredParts[i].equals(actualParts[i]) && !registeredParts[i].startsWith(":")) {
                return false;
            }
        }

        return true;
    }

    // Admin-only endpoints
    private Response handleGetAllPurchases(Request request) {
        String token = request.getHeader("Authorization");
        if (token == null) return errorResponse(401, "Unauthorized", "Missing authentication token");

        String role = SecurityUtils.getRoleFromToken(token);
        if (!"ADMIN".equals(role)) return errorResponse(403, "Forbidden", "Admin rights required");

        try {
            Response response = new Response();
            response.setStatusCode(200);
            response.setStatusText("OK");
            response.setBody(purchaseService.getAllPurchases());
            return response;
        } catch (Exception e) {
            return errorResponse(500, "Internal Server Error", "Failed to retrieve purchases");
        }
    }

    private Response handleDeletePurchase(Request request) {
        String token = request.getHeader("Authorization");
        if (token == null) return errorResponse(401, "Unauthorized", "Missing authentication token");

        String role = SecurityUtils.getRoleFromToken(token);
        if (!"ADMIN".equals(role)) return errorResponse(403, "Forbidden", "Admin rights required");

        String purchaseID = request.getPath().split("/")[3];
        try {
            purchaseService.deletePurchase(purchaseID);
            Response response = new Response();
            response.setStatusCode(200);
            response.setStatusText("OK");
            response.setBody("{\"message\": \"Purchase deleted successfully\"}");
            return response;
        } catch (Exception e) {
            return errorResponse(400, "Bad Request", e.getMessage());
        }
    }

    // Authenticated user endpoints
    private Response handleCreatePurchase(Request request) {
        String token = request.getHeader("Authorization");
        if (token == null) return errorResponse(401, "Unauthorized", "Missing authentication token");

        String buyerID = SecurityUtils.getUserIdFromToken(token);

        try {
            purchaseService.createPurchase(request.getBody(), buyerID, token);

            Response response = new Response();
            response.setStatusCode(201);
            response.setStatusText("Created");
            response.setBody("{\"message\": \"Purchase created successfully\"}");
            return response;
        } catch (Exception e) {
            return errorResponse(400, "Bad Request", e.getMessage());
        }
    }

    private Response handleGetBoughtPurchases(Request request) {
        String token = request.getHeader("Authorization");
        if (token == null) return errorResponse(401, "Unauthorized", "Missing authentication token");

        String buyerID = SecurityUtils.getUserIdFromToken(token);

        try {
            Response response = new Response();
            response.setStatusCode(200);
            response.setStatusText("OK");
            response.setBody(purchaseService.getPurchasesByBuyerID(buyerID));
            return response;
        } catch (Exception e) {
            return errorResponse(500, "Internal Server Error", "Failed to retrieve purchase history");
        }
    }

    private Response handleGetSoldPurchases(Request request) {
        String token = request.getHeader("Authorization");
        if (token == null) return errorResponse(401, "Unauthorized", "Missing authentication token");

        String sellerID = SecurityUtils.getUserIdFromToken(token);

        try {
            Response response = new Response();
            response.setStatusCode(200);
            response.setStatusText("OK");
            response.setBody(purchaseService.getPurchasesBySellerID(sellerID));
            return response;
        } catch (Exception e) {
            return errorResponse(500, "Internal Server Error", "Failed to retrieve sales history");
        }
    }

    private Response errorResponse(int code, String status, String message) {
        Response res = new Response();
        res.setStatusCode(code);
        res.setStatusText(status);
        res.setBody("{\"error\": \"" + message + "\"}");
        return res;
    }
}