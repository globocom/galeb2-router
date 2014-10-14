/*
 * Copyright (c) 2014 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY
 * KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
 * PARTICULAR PURPOSE.
 */
package com.globo.galeb.handlers.rest;

import java.io.UnsupportedEncodingException;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpHeaders;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import com.globo.galeb.core.Farm;
import com.globo.galeb.core.HttpCode;
import com.globo.galeb.core.ManagerService;
import com.globo.galeb.core.Server;
import com.globo.galeb.core.ServerResponse;

public class GetMatcherHandler implements Handler<HttpServerRequest> {

    private final Logger log;
    private String httpServerName = null;
    private final Farm farm;
    private final String classId;

    public GetMatcherHandler(String id, final Logger log, final Farm farm) {
        this.log = log;
        this.classId = id;
        this.farm = farm;
    }

    @Override
    public void handle(HttpServerRequest req) {
        final ServerResponse serverResponse = new ServerResponse(req, log, null, false);
        ManagerService managerService = new ManagerService(classId, log).setRequest(req).setResponse(serverResponse);

        if (httpServerName==null) {
            httpServerName = req.headers().contains(HttpHeaders.HOST) ? req.headers().get(HttpHeaders.HOST) : "SERVER";
            Server.setHttpServerName(httpServerName);
        }

        if (!managerService.checkUriOk()) {
            return;
        }

        String uriBase = "";
        String id = "";
        String message = "";

        if (req.params()!=null) {
            uriBase = req.params().contains("param0") ? req.params().get("param0") : "";
            id = req.params().contains("param1") ? req.params().get("param1") : "";
        }

        try {
            uriBase = java.net.URLDecoder.decode(uriBase, "UTF-8");
            id = java.net.URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            serverResponse.setStatusCode(HttpCode.BAD_REQUEST)
                                .setId(id)
                                .end();
            log.error("Unsupported Encoding");
            return;
        }

        switch (uriBase) {
            case "version":
                message = new JsonObject().putNumber("version", farm.getVersion()).encodePrettily();
                break;
            case "farm":
                message = farm.getFarmJson();
                break;
            case "virtualhost":
                message = farm.getVirtualhostJson(id);
                break;
            case "backend":
                message = farm.getBackendJson(id);
                break;
            default:
                message = "";
                break;
        }

        int statusCode = HttpCode.OK;
        if ("".equals(message)||"{}".equals(message)||"[ ]".equals(message)) {
            statusCode = HttpCode.NOT_FOUND;
            message = HttpCode.getMessage(statusCode, true);
        }

        serverResponse.setStatusCode(statusCode)
            .setMessage(message)
            .setId(id)
            .end();
        log.info(String.format("GET /%s", uriBase));
    }

}