/*
 * Copyright (c) 2014 The original author or authors.
 * All rights reserved.
 */
package lbaas;

import static lbaas.Constants.QUEUE_ROUTE_ADD;
import static lbaas.Constants.QUEUE_ROUTE_DEL;
import static lbaas.Constants.QUEUE_ROUTE_VERSION;
import static lbaas.Constants.SEPARATOR;
import static lbaas.Constants.NUM_FIELDS;
import static lbaas.Constants.STRING_PATTERN;

import java.util.Iterator;
import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

public class QueueMap {

    private final Verticle verticle;
    private final Vertx vertx;
    private final EventBus eb;
    private final Logger log;
    private final Map<String, Virtualhost> virtualhosts;

    public static String buildMessage(String virtualhostStr,
            String endpointStr,
            String portStr,
            String statusStr,
            String uriStr,
            String properties)
    {
        final String messageFormat = STRING_PATTERN;
        return String.format(messageFormat,
            virtualhostStr,
            SEPARATOR,
            endpointStr,
            SEPARATOR,
            portStr,
            SEPARATOR,
            statusStr,
            SEPARATOR,
            uriStr,
            SEPARATOR,
            properties);
    }

    public QueueMap(final Verticle verticle, final Map<String, Virtualhost> virtualhosts) {
        this.verticle = verticle;
        this.vertx = verticle.getVertx();
        this.eb=(verticle != null) ? verticle.getVertx().eventBus() : null;
        this.log=(verticle != null) ? verticle.getContainer().logger() : null;
        this.virtualhosts=virtualhosts;
    }

    public boolean processAddMessage(String message) {
        boolean isOk = true;
        final String[] route = message.split(SEPARATOR.toString());
        if (route.length == NUM_FIELDS && virtualhosts!=null) {
            String virtualhost = route[0];
            String host = route[1];
            String port = route[2];
            boolean status = !route[3].equals("0");
            String uri = route[4];
            String endpoint = (!"".equals(host) && !"".equals(port)) ?
                    String.format("%s:%s", host, port) : "";
            String uriBase = uri.split("/")[1];
            String properties = route[5];

            if (virtualhosts==null) {
                return false;
            }

            switch (uriBase) {
                case "route":
                case "virtualhost":
                    if (!virtualhosts.containsKey(virtualhost)) {
                        virtualhosts.put(virtualhost, new Virtualhost(virtualhost, vertx));
                        log.info(String.format("[%s] Virtualhost %s added", verticle.toString(), virtualhost));
                        isOk = true;
                    } else {
                        isOk = false;
                    }
                    break;
                case "real":
                    if (!virtualhosts.containsKey(virtualhost)) {
                        log.warn(String.format("[%s] Endpoint %s failed do not created because Virtualhost %s not exist", verticle.toString(), endpoint, virtualhost));
                        isOk = false;
                    } else {
                        final Virtualhost vhost = virtualhosts.get(virtualhost);
                        if (vhost.addClient(endpoint, status)) {
                            log.info(String.format("[%s] Real %s (%s) added", verticle.toString(), endpoint, virtualhost));
                        } else {
                            log.warn(String.format("[%s] Real %s (%s) already exist", verticle.toString(), endpoint, virtualhost));
                            isOk = false;
                        }
                    }
                    break;
                default:
                    log.warn(String.format("[%s] uriBase %s not supported", verticle.toString(), uriBase));
                    isOk = false;
                    break;
            }
        } else {
            isOk = false;
        }
        return isOk;
    }

    public boolean processDelMessage(String message) {
        boolean isOk = true;
        final String[] route = message.split(SEPARATOR.toString());
        if (route.length == NUM_FIELDS && virtualhosts!=null) {
            String virtualhost = route[0];
            String host = route[1];
            String port = route[2];
            boolean status = !route[3].equals("0");
            String uri = route[4];
            String endpoint = (!"".equals(host) && !"".equals(port)) ?
                    String.format("%s:%s", host, port) : "";
            String uriBase = uri.split("/")[1];
            String properties = route[5];

            if (virtualhosts==null) {
                return false;
            }

            switch (uriBase) {
                case "route":
                    Iterator<Virtualhost> iterVirtualhost = virtualhosts.values().iterator();
                    while (iterVirtualhost.hasNext()) {
                        Virtualhost aVirtualhost = iterVirtualhost.next();
                        if (aVirtualhost!=null) {
                            aVirtualhost.clear(true);
                            aVirtualhost.clear(false);
                        }
                    }
                    virtualhosts.clear();
                    log.info(String.format("[%s] All routes were cleaned", verticle.toString()));
                    break;
                case "virtualhost":
                    if (virtualhosts.containsKey(virtualhost)) {
                        virtualhosts.get(virtualhost).clear(status);
                        virtualhosts.remove(virtualhost);
                        log.info(String.format("[%s] Virtualhost %s removed", verticle.toString(), virtualhost));
                    } else {
                        log.warn(String.format("[%s] Virtualhost not removed. Virtualhost %s not exist", verticle.toString(), virtualhost));
                        isOk = false;
                    }
                    break;
                case "real":
                    if ("".equals(endpoint)) {
                        log.warn(String.format("[%s] Real UNDEF", verticle.toString(), endpoint));
                        isOk = false;
                    } else if (!virtualhosts.containsKey(virtualhost)) {
                        log.warn(String.format("[%s] Real not removed. Virtualhost %s not exist", verticle.toString(), virtualhost));
                        isOk = false;
                    } else {
                        final Virtualhost virtualhostObj = virtualhosts.get(virtualhost);
                        if (virtualhostObj!=null && virtualhostObj.removeClient(endpoint, status)) {
                            log.info(String.format("[%s] Real %s (%s) removed", verticle.toString(), endpoint, virtualhost));
                        } else {
                            log.warn(String.format("[%s] Real not removed. Real %s (%s) not exist", verticle.toString(), endpoint, virtualhost));
                            isOk = false;
                        }
                    }
                    break;
                default:
                    log.warn(String.format("[%s] uriBase %s not supported", verticle.toString(), uriBase));
                    isOk = false;
                    break;
            }
        } else {
            isOk = false;
        }
        return isOk;
    }

    public void processVersionMessage(String message) {
        try {
            setVersion(Long.parseLong(message));
        } catch (java.lang.NumberFormatException e) {}
    }

    public void registerQueueAdd() {
        Handler<Message<String>> queueAddHandler = new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                processAddMessage(message.body());
                postAddEvent(message.body());
            }
        };
        if (eb != null) {
            eb.registerHandler(QUEUE_ROUTE_ADD, queueAddHandler);
        }
    }

    public void registerQueueDel() {
        Handler<Message<String>> queueDelHandler =  new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                processDelMessage(message.body());
                postDelEvent(message.body());
            }
        };
        if (eb!=null) {
            eb.registerHandler(QUEUE_ROUTE_DEL,queueDelHandler);
        }
    }

    public void registerQueueVersion() {
        Handler<Message<String>> queueVersionHandler = new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                processVersionMessage(message.body());
            }
        };
        if (eb!=null) {
            eb.registerHandler(QUEUE_ROUTE_VERSION, queueVersionHandler);
        }
    }

    private void setVersion(Long version) {
        if (verticle instanceof IEventObserver) {
            ((IEventObserver)verticle).setVersion(version);
            log.info(String.format("[%s] POST /version: %d", verticle.toString(), version));
        }
    }

    private void postDelEvent(String message) {
        if (verticle instanceof IEventObserver) {
            ((IEventObserver)verticle).postDelEvent(message);
        }
    }

    private void postAddEvent(String message) {
        if (verticle instanceof IEventObserver) {
            ((IEventObserver)verticle).postAddEvent(message);
        }
    }
}
