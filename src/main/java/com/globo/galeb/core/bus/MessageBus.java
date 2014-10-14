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
package com.globo.galeb.core.bus;

import com.globo.galeb.core.IJsonable;
import com.globo.galeb.core.SafeJsonObject;

public class MessageBus {

    public static final String ENTITY_FIELDNAME    = "entity";
    public static final String PARENT_ID_FIELDNAME = "parentId";
    public static final String URI_FIELDNAME       = "uri";

    private String entityStr  = "{}";
    private String parentId   = "";
    private String uriStr     = "";
    private String messageBus = "{}";

    public MessageBus() {
        this("{}");
    }

    public MessageBus(String message) {
        SafeJsonObject json = new SafeJsonObject(message);
        setEntity(json.getString(ENTITY_FIELDNAME,"{}"));
        setParentId(json.getString(PARENT_ID_FIELDNAME, ""));
        setUri(json.getString(URI_FIELDNAME, ""));
        make();
    }

    public String getParentId() {
        return parentId;
    }

    public MessageBus setParentId(String parentId) {
        if (parentId!=null) {
            this.parentId = parentId;
        }
        return this;
    }

    public SafeJsonObject getEntity() {
        return new SafeJsonObject(entityStr);
    }

    public String getEntityId() {
        return getEntity().getString(IJsonable.ID_FIELDNAME, "");
    }

    public MessageBus setEntity(String entityStr) {
        this.entityStr = new SafeJsonObject(entityStr).encode();
        return this;
    }

    public MessageBus setEntity(SafeJsonObject entityJson) {
        if (entityJson!=null) {
            this.entityStr = entityJson.encode();
        } else {
            this.entityStr = "{}";
        }
        return this;
    }

    public String getUri() {
        return uriStr;
    }

    public MessageBus setUri(String uriStr) {
        if (uriStr!=null) {
            this.uriStr = uriStr;
        }
        return this;
    }

    public String getUriBase() {
        String[] uriStrArray = uriStr.split("/");
        return uriStrArray.length > 1 ? uriStrArray[1] : "";
    }

    public MessageBus make() {

        messageBus = new SafeJsonObject()
                            .putString(URI_FIELDNAME, uriStr)
                            .putString(PARENT_ID_FIELDNAME, parentId)
                            .putString(ENTITY_FIELDNAME, getEntity().encode())
                            .encode();
        return this;
    }

    @Override
    public String toString() {
        return messageBus;
    }

    public SafeJsonObject toJson() {
        return new SafeJsonObject(messageBus);
    }

}