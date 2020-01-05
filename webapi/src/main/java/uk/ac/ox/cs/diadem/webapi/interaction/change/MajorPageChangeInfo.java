/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction.change;

import uk.ac.ox.cs.diadem.webapi.pagestate.MajorChangeType;

/**
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface MajorPageChangeInfo extends GenericModification {

  public MajorChangeType changeType();
}
