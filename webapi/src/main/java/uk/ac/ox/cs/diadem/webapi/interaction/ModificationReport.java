package uk.ac.ox.cs.diadem.webapi.interaction;

import java.util.Set;

import uk.ac.ox.cs.diadem.webapi.interaction.change.AlertInfo;
import uk.ac.ox.cs.diadem.webapi.interaction.change.ChangeOnFormInfo;
import uk.ac.ox.cs.diadem.webapi.interaction.change.GenericModification.ModificationType;
import uk.ac.ox.cs.diadem.webapi.interaction.change.IFrameChange;
import uk.ac.ox.cs.diadem.webapi.interaction.change.MajorPageChangeInfo;
import uk.ac.ox.cs.diadem.webapi.interaction.change.MinorPageChanges;
import uk.ac.ox.cs.diadem.webapi.interaction.change.NewPageInfo;

/**
 *
 *
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface ModificationReport {

  Set<ModificationType> detectedChanges();

  //
  AlertInfo getAlertPayload();

  //
  NewPageInfo getNewPageInfo();

  //
  Set<ChangeOnFormInfo> getChangeOnFormInfo();

  //
  MajorPageChangeInfo getMajorPageContentInfo();

  //
  // TextualInfo getTextualMessages();

  IFrameChange getIFrameChanges();

  MinorPageChanges getMinorPageChanges();

}
