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
package org.apache.usergrid.persistence.query.ir.result;


import java.util.List;

import org.apache.usergrid.persistence.EntityManager;
import org.apache.usergrid.persistence.Results;


/** @author tnine */
public class EntityResultsLoader implements ResultsLoader {

    private EntityManager em;


    /**
     *
     */
    public EntityResultsLoader( EntityManager em ) {
        this.em = em;
    }


    /* (non-Javadoc)
     * @see org.apache.usergrid.persistence.query.ir.result.ResultsLoader#getResults(java.util.List)
     */
    @Override
    public Results getResults( List<ScanColumn> entityIds, String type ) throws Exception {
        return em.getEntities( ScanColumnTransformer.getIds( entityIds ), type );
    }
}
