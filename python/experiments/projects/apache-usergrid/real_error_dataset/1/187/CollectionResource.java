/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.usergrid.rest.test.resource2point0.endpoints;


import org.apache.usergrid.rest.test.resource2point0.model.ApiResponse;
import org.apache.usergrid.rest.test.resource2point0.model.Entity;
import org.apache.usergrid.rest.test.resource2point0.state.ClientContext;


/**
 * Holds POST,PUT,GET,DELETE methods for Collections. Models the rest endpoints for the different ways
 * to get an entity out of UG.
 */
public  class CollectionResource extends AbstractCollectionResource<Entity,EntityResource> {


    public CollectionResource(final String name, final ClientContext context, final UrlResource parent) {
        super( name, context, parent );
    }


    @Override
    protected Entity instantiateT(ApiResponse response) {
        Entity entity = new Entity(response);
        return entity;
    }

    @Override
    protected EntityResource instantiateEntityResource(String identifier, ClientContext context, UrlResource parent) {
        return new EntityResource( identifier, context, this );
    }

}
