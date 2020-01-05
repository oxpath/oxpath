/*
 * Copyright (c)2011, DIADEM Team
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the DIADEM team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL DIADEM Team BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.ox.cs.diadem.webapi.dom;

import uk.ac.ox.cs.diadem.webapi.dom.DOMNode.Type;

/**
 * Interface for a DOM tree visitor. The actual visit follows the algorithm:
 * 
 * <pre>
 * {@code 
 * init();
 * walkRecursively(documentElement);
 * finish();
 * }
 * </pre>
 * 
 * where:
 * 
 * <pre>
 * {@code
 * void walkRecursively(DOMNode node){
 *  startElement(node);
 *  visitNode(node); //per type node
 * for (final DOMNode child : node.getChildNodes())} 
 *   walkRecursively(child);
 * }  
 *  endElement(node);
 * }
 * </pre>
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 * 
 */
public interface DOMVisitor {

  /**
   * Initialize the visitor. This is the first method called before starting walking the DOM tree. It is called only
   * once.
   */
  void init();

  /**
   * Called before visiting the node's children and descendants.
   * 
   * @param node
   *          the node under visit
   */
  void startElement(DOMNode node);

  /**
   * Called after visiting allElements node's descendant
   * 
   * @param node
   *          the node under visit
   */
  void endElement(DOMNode node);

  /**
   * Called at the very end of the visit
   */
  void finish();

  /**
   * Called in case of the current {@link DOMNode} is of type {@link Type#COMMENT}
   * 
   * @param node
   *          the node under visit
   */
  void visitComment(DOMNode node);

  /**
   * Called in case of the current {@link DOMNode} is of type {@link Type#TEXT}
   * 
   * @param node
   *          the node under visit
   */
  void visitText(DOMNode node);

  /**
   * Called in case of the current {@link DOMNode} is of type {@link Type#PROCESSING_INSTRUCTION}
   * 
   * @param node
   *          the node under visit
   */
  void visitProcessingInstruction(DOMNode node);

  /**
   * Returns true is the current node must be skipped during the visit, false otherwise
   * 
   * @param node
   *          the node under visit
   * @return true is the current node must be skipped during the visit, false otherwise
   */
  boolean filterOut(DOMNode node);
}
