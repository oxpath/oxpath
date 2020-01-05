package uk.ac.ox.cs.diadem.webapi.testsupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to the testsupport framework. Right now, encompasses only the {@link StringDatabase}.
 * @author christian
 * 
 * Предоставляет доступ к структуре/фрэймворку testupport. Cейчас включает только {@link StringDatabase}
 * 
 * TESTING: uses all methods
 *
 */
public class TestFacilities {

	final static Logger logger=LoggerFactory.getLogger(TestFacilities.class);
	
	/** 
	 * Returns the {@link StringDatabase} for given class -- typically the test case class. 
	 * As {@link StringDatabase}s are used for testing, access to them is denied if not run as part of a test case.
	 * 
	 * @param classObject
	 * @return the {@link StringDatabase} for the given classObject
	 * @throws TestSupportException if the current execution is triggered by JUnit
	 */
	static public StringDatabase getStringDatabase(final Class<?> classObject) {
		/*if(!ConfigurationFacility.isTestRun())
			throw new TestSupportException("Attempt to access the StringDatabase for '" + classObject.getName() + "' while not running a test.", logger);*/
		assert classObject!=null;
		return StringDatabaseImplementation.getInstance(classObject);
	}
}
