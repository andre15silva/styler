/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.transport.rest.vertx;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.foundation.ssl.SSLOptionFactory;
import org.apache.servicecomb.foundation.vertx.ClientEvent;
import org.apache.servicecomb.foundation.vertx.EventType;
import org.apache.servicecomb.foundation.vertx.ServerType;
import org.apache.servicecomb.foundation.vertx.VertxTLSBuilder;
import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogConfiguration;
import org.apache.servicecomb.transport.rest.vertx.accesslog.impl.AccessLogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;

public class RestServerVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestServerVerticle.class);

  private static final String SSL_KEY = "rest.provider";

  private Endpoint endpoint;

  private URIEndpointObject endpointObject;

  private final AtomicInteger connectedCounter;

  public RestServerVerticle() {
    this(((VertxRestTransport) BeanUtils.getBean("vertxRestTransport")).getConnectedCounter());
  }

  public RestServerVerticle(AtomicInteger connectedCounter) {
    this.connectedCounter = connectedCounter;
  }

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    this.endpoint = (Endpoint) context.config().getValue(AbstractTransport.ENDPOINT_KEY);
    this.endpointObject = (URIEndpointObject) endpoint.getAddress();
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    try {
      super.start();
      // 如果本地未配置地址，则表示不必监听，只需要作为客户端使用即可
      if (endpointObject == null) {
        LOGGER.warn("rest listen address is not configured, will not start.");
        startFuture.complete();
        return;
      }
      Router mainRouter = Router.router(vertx);
      mountAccessLogHandler(mainRouter);
      mountCorsHandler(mainRouter);
      initDispatcher(mainRouter);
      HttpServer httpServer = createHttpServer();
      httpServer.requestHandler(mainRouter::accept);
      httpServer.connectionHandler(connection -> {
        int connectedCount = connectedCounter.incrementAndGet();
        int connectionLimit = DynamicPropertyFactory.getInstance()
            .getIntProperty("servicecomb.rest.server.connection-limit", Integer.MAX_VALUE).get();
        if (connectedCount > connectionLimit) {
          connectedCounter.decrementAndGet();
          connection.close();
        } else {
          EventManager.post(new ClientEvent(connection.remoteAddress().toString(),
              EventType.Connected, ServerType.Rest, connectedCount));
          connection.closeHandler(event -> EventManager.post(new ClientEvent(connection.remoteAddress().toString(),
              EventType.Closed, ServerType.Rest, connectedCounter.decrementAndGet())));
        }
      });

      startListen(httpServer, startFuture);
    } catch (Throwable e) {
      // vert.x got some states that not print error and execute call back in VertexUtils.blockDeploy, we add a log our self.
      LOGGER.error("", e);
      throw e;
    }
  }

  private void mountAccessLogHandler(Router mainRouter) {
    if (AccessLogConfiguration.INSTANCE.getAccessLogEnabled()) {
      String pattern = AccessLogConfiguration.INSTANCE.getAccesslogPattern();
      LOGGER.info("access log enabled, pattern = {}", pattern);
      mainRouter.route()
          .handler(new AccessLogHandler(
              pattern
          ));
    }
  }

  /**
   * Support CORS
   */
  void mountCorsHandler(Router mainRouter) {
    if (!TransportConfig.isCorsEnabled()) {
      return;
    }

    CorsHandler corsHandler = getCorsHandler(TransportConfig.getCorsAllowedOrigin());
    // Access-Control-Allow-Credentials
    corsHandler.allowCredentials(TransportConfig.isCorsAllowCredentials());
    // Access-Control-Allow-Headers
    corsHandler.allowedHeaders(TransportConfig.getCorsAllowedHeaders());
    // Access-Control-Allow-Methods
    Set<String> allowedMethods = TransportConfig.getCorsAllowedMethods();
    for (String method : allowedMethods) {
      corsHandler.allowedMethod(HttpMethod.valueOf(method));
    }
    // Access-Control-Expose-Headers
    corsHandler.exposedHeaders(TransportConfig.getCorsExposedHeaders());
    // Access-Control-Max-Age
    int maxAge = TransportConfig.getCorsMaxAge();
    if (maxAge >= 0) {
      corsHandler.maxAgeSeconds(maxAge);
    }

    LOGGER.info("mount CorsHandler");
    mainRouter.route().handler(corsHandler);
  }

  private CorsHandler getCorsHandler(String corsAllowedOrigin) {
    return CorsHandler.create(corsAllowedOrigin);
  }

  private void initDispatcher(Router mainRouter) {
    List<VertxHttpDispatcher> dispatchers = SPIServiceUtils.getSortedService(VertxHttpDispatcher.class);
    for (VertxHttpDispatcher dispatcher : dispatchers) {
      if (dispatcher.enabled()) {
        dispatcher.init(mainRouter);
      }
    }
  }

  private void startListen(HttpServer server, Future<Void> startFuture) {
    server.listen(endpointObject.getPort(), endpointObject.getHostOrIp(), ar -> {
      if (ar.succeeded()) {
        LOGGER.info("rest listen success. address={}:{}",
            endpointObject.getHostOrIp(),
            ar.result().actualPort());
        startFuture.complete();
        return;
      }

      String msg = String.format("rest listen failed, address=%s:%d",
          endpointObject.getHostOrIp(),
          endpointObject.getPort());
      LOGGER.error(msg, ar.cause());
      startFuture.fail(ar.cause());
    });
  }

  private HttpServer createHttpServer() {
    HttpServerOptions serverOptions = createDefaultHttpServerOptions();
    return vertx.createHttpServer(serverOptions);
  }

  private HttpServerOptions createDefaultHttpServerOptions() {
    HttpServerOptions serverOptions = new HttpServerOptions();
    serverOptions.setUsePooledBuffers(true);
    serverOptions.setIdleTimeout(TransportConfig.getConnectionIdleTimeoutInSeconds());
    serverOptions.setCompressionSupported(TransportConfig.getCompressed());
    serverOptions.setMaxHeaderSize(TransportConfig.getMaxHeaderSize());
    if (endpointObject.isHttp2Enabled()) {
      serverOptions.setUseAlpn(true);
    }
    if (endpointObject.isSslEnabled()) {
      SSLOptionFactory factory =
          SSLOptionFactory.createSSLOptionFactory(SSL_KEY, null);
      SSLOption sslOption;
      if (factory == null) {
        sslOption = SSLOption.buildFromYaml(SSL_KEY);
      } else {
        sslOption = factory.createSSLOption();
      }
      SSLCustom sslCustom = SSLCustom.createSSLCustom(sslOption.getSslCustomClass());
      VertxTLSBuilder.buildNetServerOptions(sslOption, sslCustom, serverOptions);
    }

    return serverOptions;
  }
}
