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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.openmessaging.service;

import io.openmessaging.KeyValue;
import io.openmessaging.ServiceLifecycle;

/**
 * @author vintagewang@apache.org
 * @version OMS 1.0
 * @since OMS 1.0
 */
public interface ServiceEndPoint extends ServiceLifecycle {
    /**
     * Register/re-register a service in a serviceEndPoint object
     * if service has been registered in serviceEndPoint object, it will be failed when registering delicately
     *
     * @param service the service to publish in serviceEndPoint
     */
    void publish(Object service);

    /**
     * Like {@link #publish(Object)} but specifying {@code attributes}
     * that can be used to configure the service published
     *
     * @param service the service to publish in serviceEndPoint
     * @param properties the service published attributes
     */

    void publish(Object service, KeyValue properties);

    /**
     * Bind a service object to serviceEndPoint, which can directly call services provided by service object
     *
     * @param type service type to bind in serviceEndPoint
     * @return service proxy object to bind
     */
    <T> T bind(Class<T> type);

    /**
     * Like {@link #bind(Class)} but specifying {@code attributes} that can be used to configure the service band
     *
     * @param type service type to bind in serviceEndPoint
     * @param properties the service bind attributes
     * @param <T> service proxy object to bind
     * @return service proxy object to bind
     */
    <T> T bind(Class<T> type, KeyValue properties);

    /**
     * Like {@link #bind(Class, KeyValue)} but specifying {@code serviceLoadBalance} that can be used to select
     * endPoint target
     *
     * @param type service type to bind in serviceConsumer
     * @param properties the service band attributes
     * @param serviceLoadBalance select endPoint target algorithm
     * @param <T> service proxy object to bind
     * @return service proxy object to bind
     */
    <T> T bind(Class<T> type, KeyValue properties, ServiceLoadBalanceStrategy serviceLoadBalance);

    InvokeContext invokeContext();
}
