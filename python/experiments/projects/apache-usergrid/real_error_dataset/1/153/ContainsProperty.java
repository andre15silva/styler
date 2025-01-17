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
package org.apache.usergrid.persistence.index.query.tree;


import org.antlr.runtime.ClassicToken;
import org.antlr.runtime.Token;


/**
 * A property for full text searching that requires special renaming
 *
 * @author tnine
 */
public class ContainsProperty extends Property {

    private String indexedName = null;


    public ContainsProperty( Token t ) {
        super( t );
        this.indexedName = String.format( "%s.keywords", super.getValue() );
    }


    public ContainsProperty( String property ) {
        this( new ClassicToken( 0, property ) );
    }


    /* (non-Javadoc)
     * @see org.apache.usergrid.persistence.query.tree.Property#getIndexedValue()
     */
    @Override
    public String getIndexedValue() {
        return this.indexedName;
    }


    /** @return the property */
    public ContainsProperty getProperty() {
        return ( ContainsProperty ) this.children.get( 0 );
    }
}
