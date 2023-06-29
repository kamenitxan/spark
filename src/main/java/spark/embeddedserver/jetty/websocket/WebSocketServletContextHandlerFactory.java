/*
 * Copyright 2015 - Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.embeddedserver.jetty.websocket;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates websocket servlet context handlers.
 */
public class WebSocketServletContextHandlerFactory {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServletContextHandlerFactory.class);


    /**
     * Creates a new websocket servlet context handler.
     *
     * @param webSocketHandlers          webSocketHandlers
     * @param webSocketIdleTimeoutMillis webSocketIdleTimeoutMillis
     * @return a new websocket servlet context handler or 'null' if creation failed.
     */
    public static ServletContextHandler create(Map<String, WebSocketHandlerWrapper> webSocketHandlers,
                                        Optional<Long> webSocketIdleTimeoutMillis) {
        return new WebSocketServletContextHandlerFactory().createContextHandler(webSocketHandlers, webSocketIdleTimeoutMillis);
    }

    /**
     * Creates a new websocket servlet context handler.
     * Alias of create(...) but not static, to be able to customize web socket handlers
     * <a href="https://github.com/perwendel/spark/issues/1208">...</a>
     *
     * @param webSocketHandlers             webSocketHandlers
     * @param webSocketIdleTimeoutMillis    webSocketIdleTimeoutMillis
     * @return a new websocket servlet context handler or 'null' if creation failed.
     */
    public ServletContextHandler createContextHandler(Map<String, WebSocketHandlerWrapper> webSocketHandlers,
                                               Optional<Long> webSocketIdleTimeoutMillis) {
        ServletContextHandler webSocketServletContextHandler = null;
        if (webSocketHandlers != null) {
            try {
                webSocketServletContextHandler = new ServletContextHandler(null, "/", true, false);
                JettyWebSocketServletContainerInitializer.configure(webSocketServletContextHandler, (servletContext, wsContainer) ->
                {
                    if (webSocketIdleTimeoutMillis.isPresent()) {
                        // timeout
                        long to = webSocketIdleTimeoutMillis.get();
                        wsContainer.setIdleTimeout(Duration.ofMillis(to));
                    }
                    // Configure default max size
                    //wsContainer.setMaxTextMessageSize(65535);
                    for (String path : webSocketHandlers.keySet()) {
                        JettyWebSocketCreator webSocketCreator = WebSocketCreatorFactory.createWS(webSocketHandlers.get(path));
                        // Add websockets
                        wsContainer.addMapping(path, webSocketCreator);
                    }
                });
            } catch (Exception ex) {
                logger.error("creation of websocket context handler failed.", ex);
                webSocketServletContextHandler = null;
            }
        }
        return webSocketServletContextHandler;
    }

}
