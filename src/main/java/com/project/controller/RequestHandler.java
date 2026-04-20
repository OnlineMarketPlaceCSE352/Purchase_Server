package com.project.controller;

import com.project.dto.Request;
import com.project.dto.Response;

@FunctionalInterface
public interface RequestHandler {
    Response handle(Request request) throws Exception;
}
