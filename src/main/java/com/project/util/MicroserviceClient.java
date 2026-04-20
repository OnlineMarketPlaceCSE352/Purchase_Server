package com.project.util;

import com.project.dto.Request;
import java.io.*;
import java.net.Socket;

public class MicroserviceClient {

    public static String sendRequestToService(String host, int port, Request request) throws IOException {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), false);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            RestHandler.sendRequest(out, host, port, request);

            String statusLine = in.readLine();
            if (statusLine == null) {
                throw new IOException("Connection closed by server.");
            }

            String[] statusParts = statusLine.split(" ");
            int statusCode = Integer.parseInt(statusParts[1]);
            if (statusCode < 200 || statusCode >= 300) {
                throw new RuntimeException("Microservice returned error: " + statusCode + " " + statusLine);
            }

            int contentLength = 0;
            String headerLine;
            while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                if (headerLine.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
                }
            }

            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                int read = in.read(bodyChars, 0, contentLength);
                return new String(bodyChars, 0, read);
            } else {
                StringBuilder bodyBuilder = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    bodyBuilder.append(line).append("\n");
                }
                return bodyBuilder.toString().trim();
            }
        }
    }
}