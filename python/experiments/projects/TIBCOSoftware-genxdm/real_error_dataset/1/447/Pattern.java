/*
 * Portions copyright (c) 1998-1999, James Clark : see copyingjc.txt for
 * license details
 * Portions copyright (c) 2002, Bill Lindsey : see copying.txt for license
 * details
 * 
 * Portions copyright (c) 2009-2011 TIBCO Software Inc.
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
package org.genxdm.processor.xpath.v10.patterns;

import org.genxdm.Model;
import org.genxdm.nodes.TraversingInformer;
import org.genxdm.xpath.v10.TraverserDynamicContext;
import org.genxdm.xpath.v10.ExprContextDynamic;

/**
 * an XPath (XSLT) match pattern
 */
public interface Pattern
{
	/**
	 * return true if the given node matches this pattern when evaluated in the given ExpressionContext
	 * @param model TODO
	 */
	<N> boolean matches(Model<N> model, N node, ExprContextDynamic<N> dynEnv);
	
	boolean matches(TraversingInformer node, TraverserDynamicContext dynEnv);

}
