/**
 * 
 */
package uk.ac.ox.cs.diadem.webapi.dom.mutation;

import uk.ac.ox.cs.diadem.webapi.dom.DOMCSS2Properties.CssProperty;

/**
 * @author giog
 * 
 */
public interface CSSMutationRecord extends MutationRecord {

  CssProperty getProperty();

  String getOldValue();

  String getNewValue();

}
