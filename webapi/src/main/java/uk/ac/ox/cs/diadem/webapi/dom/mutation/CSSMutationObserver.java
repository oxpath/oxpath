package uk.ac.ox.cs.diadem.webapi.dom.mutation;

import java.util.Set;

public interface CSSMutationObserver extends MutationObserver {

  /**
   * Returns a {@link CSSMutationRecord} for each property that changed. It also releases the observation, therefore a
   * new observer need to be installed for further observations
   * 
   * @return
   */
  Set<CSSMutationRecord> takeRecords();
}
