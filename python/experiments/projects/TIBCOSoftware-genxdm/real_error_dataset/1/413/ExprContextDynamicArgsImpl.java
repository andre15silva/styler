/*
 * Copyright (c) 2009-2011 TIBCO Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.genxdm.processor.xpath.v10.expressions;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.genxdm.xpath.v10.ExprContextDynamic;
import org.genxdm.xpath.v10.ExprContextDynamicArgs;
import org.genxdm.xpath.v10.Variant;

public final class ExprContextDynamicArgsImpl<N> extends DynamicContextBuilderBaseImpl
    implements ExprContextDynamicArgs<N>
{
	private final Map<QName, Variant<N>> variables = new HashMap<QName, Variant<N>>();
	
	@Override
	public void bindVariableValue(final QName name, final Variant<N> value)
	{
		variables.put(name, value);
	}

    @Override
	public ExprContextDynamic<N> build()
	{
		return new ExprContextDynamicImpl<N>(position, size, variables, m_inheritAttributes, m_inheritNamespaces);
	}

    @Override
	public void reset()
	{
		variables.clear();
	}
}
