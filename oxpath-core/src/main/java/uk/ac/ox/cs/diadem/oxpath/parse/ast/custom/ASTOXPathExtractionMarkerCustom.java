package uk.ac.ox.cs.diadem.oxpath.parse.ast.custom;

import java.util.Iterator;

import uk.ac.ox.cs.diadem.oxpath.core.PositionFuncEnum;
import uk.ac.ox.cs.diadem.oxpath.model.language.OXPathExtractionMarker;
import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;

/**
 * @author AndrewJSel
 *
 */
public class ASTOXPathExtractionMarkerCustom extends CustomSimpleNode {

  public ASTOXPathExtractionMarkerCustom(final int id) {
    super(id);
  }

  public ASTOXPathExtractionMarkerCustom(final OXPathParser p, final int id) {
    super(p, id);
  }

  public void setExtractionMarker(final OXPathExtractionMarker iMarker) {
    marker = iMarker;
  }

  public OXPathExtractionMarker getExtractionMarker() {
    return marker;
  }

  public void setHasList(final boolean list) {
    hasList = list;
  }

  public boolean hasList() {
    return hasList;
  }

  private OXPathExtractionMarker marker;
  private boolean hasList = false;

  public void setSetBasedEval(final PositionFuncEnum set) {
    setEval = set;
  }

  public PositionFuncEnum getSetBasedEval() {
    return setEval;
  }

  private PositionFuncEnum setEval = PositionFuncEnum.NEITHER;

  @Override
  public String toString() {
    String rdfTypes = null;
    final Iterator<String> iter = getExtractionMarker().getRdfTypes();
    if (iter.hasNext()) {
      rdfTypes = iter.next();
      while (iter.hasNext()) {
        rdfTypes += ", " + iter.next();
      }
    }

    return this.getClass().getSimpleName() + "[label=" + getExtractionMarker().getLabel() + ",isAttribute="
    + getExtractionMarker().isAttribute() + (rdfTypes != null ? (", rdfTypes=(" + rdfTypes + ")") : "")
    + ",setEval=" + setEval + ",hasList=" + hasList + "]";
  }
}