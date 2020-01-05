/**
 * 
 */
package uk.ac.ox.cs.diadem.webapi.interaction.impl;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.diadem.webapi.MiniBrowser.Advice;
import uk.ac.ox.cs.diadem.webapi.interaction.AdviceProcessor;

/**
 * @author xiag
 *
 */
public class WebAdviceProcessor implements AdviceProcessor{

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.diadem.webapi.interaction.AdviceProcessor#beginTrace(java.lang.String)
	 */
	@Override
	public void beginTrace(String SequenceId) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.diadem.webapi.interaction.AdviceProcessor#endTrace(java.lang.String)
	 */
	@Override
	public void endTrace(String SequenceId) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.diadem.webapi.interaction.AdviceProcessor#getAdvice(java.lang.String)
	 */
	@Override
	public List<List<Advice>> getAdvice(String SequenceId) {
		// TODO Auto-generated method stub
		return new ArrayList<List<Advice>>();
	}

}
