package uk.ac.ox.cs.diadem.webapi.dom.mutation;

import java.util.Set;

import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;

public interface MutationFormSummary {

  Set<DOMElement> getFieldsHavingNewOptions();

  Set<DOMElement> getFieldsHavingRemovedOptions();

  Set<DOMElement> getNewEnabledFields();

  Set<DOMElement> getNewDisabledFields();

  // Set<DOMElement> getAppearedFields();

  Set<DOMElement> getAppearedEnabledFields();

  Set<DOMElement> getAppearedDisabledFields();

  Set<DOMElement> getEnabledFields();

  Set<DOMElement> getDisabledFields();

  Set<String> getAppearedText();

  boolean isFormChanged();

  Set<DOMElement> getFieldswithCSSChange();

  Set<DOMElement> getRemovedFields();

  Set<DOMElement> getDisappearedFields();

  Long countElementsWithVisibilityChange();
  // countNodesWithVisibilityChange: 0
  // fieldsWithNewOptions : [],
  // fieldsWithRemovedOptions : [],
  // newEnabledFields : [],
  // newDisabledFields : [],
  // appearedEnabledFields : [],
  // appearedDisabledFields : [],
  // enabledFields : [],
  // disabledFields : [],
  // // strings
  // textVaried : [],//strings
  // //removedElements : [],
  // removedFields : [],
  // disappearedFields : [],
  // fieldsWithCSSChange : []
}
