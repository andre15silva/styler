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
package org.genxdm.processor.xpath.v10.expressions;

import org.genxdm.Model;
import org.genxdm.NodeKind;
import org.genxdm.processor.xpath.v10.iterators.NodeIteratorOnIterator;
import org.genxdm.xpath.v10.ExprContextDynamic;
import org.genxdm.xpath.v10.NodeIterator;

public final class AncestorOrSelfAxisExpr 
    extends ReverseAxisExpr
{

	public <N> NodeIterator<N> nodeIterator(Model<N> model, final N contextNode, final ExprContextDynamic<N> dynEnv)
	{
		return new NodeIteratorOnIterator<N>(model.getAncestorOrSelfAxis(contextNode).iterator());
	}

	public NodeKind getPrincipalNodeKind()
	{
		return NodeKind.ELEMENT;
	}
}
