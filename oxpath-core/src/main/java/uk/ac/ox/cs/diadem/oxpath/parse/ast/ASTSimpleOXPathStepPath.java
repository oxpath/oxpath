/* Generated By:JJTree: Do not edit this line. ASTSimpleOXPathStepPath.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package uk.ac.ox.cs.diadem.oxpath.parse.ast;

import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;

public class ASTSimpleOXPathStepPath extends uk.ac.ox.cs.diadem.oxpath.parse.ast.custom.ASTSimpleOXPathStepPathCustom {
  public ASTSimpleOXPathStepPath(final int id) {
    super(id);
  }

  public ASTSimpleOXPathStepPath(final OXPathParser p, final int id) {
    super(p, id);
  }

  /** Accept the visitor. **/
  @Override
  public Object jjtAccept(final OXPathParserVisitor visitor, final Object data)
      throws uk.ac.ox.cs.diadem.oxpath.core.OXPathException {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=5eaea15a2a3070a9d48d38706625619a (do not edit this line) */
