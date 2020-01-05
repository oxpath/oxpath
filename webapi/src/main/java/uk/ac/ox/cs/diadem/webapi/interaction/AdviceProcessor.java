/**
 * 
 */
package uk.ac.ox.cs.diadem.webapi.interaction;

import java.util.List;

import uk.ac.ox.cs.diadem.webapi.MiniBrowser.Advice;

/**
 * @author xiag
 *
 */
public interface AdviceProcessor {

	public void beginTrace(String SequenceId);
	
	public void endTrace(String SequenceId);
	
	public List<List<Advice>> getAdvice(String SequenceId);
}
