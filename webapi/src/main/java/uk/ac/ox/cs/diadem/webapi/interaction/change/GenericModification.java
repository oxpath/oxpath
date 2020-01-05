/*
 * COPYRIGHT (C) 2010-2015 DIADEM Team, Department of Computer Science, Oxford University. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of the DIADEM project ("DIADEM"), Department of Computer Science,
 * Oxford University ("Confidential Information").  You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you entered into with DIADEM.
 */

package uk.ac.ox.cs.diadem.webapi.interaction.change;

import org.apache.commons.lang3.tuple.Pair;

/**
 * 
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface GenericModification {

  public enum ModificationType {
    ALERT, NEW_PAGE, CHANGE_ON_FORM, IFRAME_CHANGE /** not used **/
    , MAJOR_PAGE_CHANGE, MINOR_PAGE_CHANGE;

    public Pair<String, String> asConstant() {
      return Pair.of("change_type", name().toLowerCase());
    }
  }

  ModificationType getType();

}
