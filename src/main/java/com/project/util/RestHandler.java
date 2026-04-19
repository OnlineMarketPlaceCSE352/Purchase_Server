package com.project.util;

import com.project.dto.Request;
import com.project.dto.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

public class RestHandler {
    public static void sendResponse(OutputStream output, Response response) throws IOException {
        output.write(response.toString().getBytes("UTF-8"));
        output.flush();
    }

    public static Request parseRequest(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) return null;

        String[] parts = requestLine.split(" ");
        Method method = Method.valueOf(parts[0]);
        String path = parts[1];

        Request request = new Request(method, path);

        // TODO Parse headers and body

        return request;
    }


}
