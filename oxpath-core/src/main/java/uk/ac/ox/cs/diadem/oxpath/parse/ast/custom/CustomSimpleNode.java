/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package uk.ac.ox.cs.diadem.oxpath.parse.ast.custom;

import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.Node;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.OXPathParserVisitor;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.SimpleNode;

/**
 * This base implementation for AST nodes contains all additional code that cannot be put into SimpleNode because
 * auto-generated. See option NODE_CLASS in JJTree
 * 
 * @author giog
 *
 */
public class CustomSimpleNode extends SimpleNode {

  protected Node parent;
  protected Node[] children;
  protected int id;
  protected Object value;
  protected OXPathParser parser;

  public CustomSimpleNode(final int i) {
    super(i);
  }

  public CustomSimpleNode(final OXPathParser p, final int i) {
    this(i);
    parser = p;
  }

  public void nullifyChildren() {
    children = null;
  }

  /** Accept the visitor. **/
  @Override
  public Object jjtAccept(final OXPathParserVisitor visitor, final Object data)
      throws uk.ac.ox.cs.diadem.oxpath.core.OXPathException {
    return visitor.visit(this, data);
  }

  public void mergeWith(final Node to) {
    final int numChildren = parent.jjtGetNumChildren();
    for (int i = 0; i < numChildren; i++) {
      final Node child = parent.jjtGetChild(i);
      if (child.equals(this)) {
        ((CustomSimpleNode) parent).setChild(i, to);
        to.jjtSetParent(parent);
        return;
      }
    }
  }

  public void removeChild(final Node toRemove) {
    final Node[] newChildren = new Node[children.length - 1];
    for (int i = 0; i < children.length; i++) {
      final Node child = jjtGetChild(i);
      if (child.equals(toRemove)) {
        toRemove.jjtSetParent(null);
        continue;
      }
      newChildren[i] = child;
    }
    children = newChildren;
  }

  private void setChild(final int i, final Node to) {
    children[i] = to;

  }

}
