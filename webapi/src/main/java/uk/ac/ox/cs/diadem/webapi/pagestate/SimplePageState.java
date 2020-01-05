package uk.ac.ox.cs.diadem.webapi.pagestate;

import uk.ac.ox.cs.diadem.webapi.dom.mutation.MutationFormObserver;
import uk.ac.ox.cs.diadem.webapi.interaction.change.MajorPageChangeInfo;

public interface SimplePageState {

  /**
   * Returns true if the other {@link PageState} is identical to this one.
   */
  boolean identicalTo(SimplePageState other);

  /**
   * Returns true if the other {@link PageState} is similar to this one. The specific meaning of similar is determine by
   * the implementation, e.g., through a threshold on the edit distance.
   */
  boolean similarTo(SimplePageState other);

  /**
   * This will have a meaningful result only when either {@link #identicalTo(SimplePageState)} or
   * {@link #atSameLocation(SimplePageState)} have been called, and return false, otherwise it will return
   * {@link MajorPageChangeInfo.MajorChangeType#UNSPECIFIED};
   *
   * @return
   */
  MajorChangeType differenceTypeIfAny(SimplePageState other);

  // /**
  // * Returns true if the pagestate represents a different page (different location or content).
  // */
  // boolean isDifferentPage(SimplePageState other);

  /**
   * Returns true if the pagestate represents a page with the same location URL.
   */
  boolean atSameLocation(final SimplePageState other);

  String getLocationURLOfCurrentPage();

  MutationFormObserver getMutationObserver();

  /**
   * @return
   */
  boolean isErroneousState();

}
