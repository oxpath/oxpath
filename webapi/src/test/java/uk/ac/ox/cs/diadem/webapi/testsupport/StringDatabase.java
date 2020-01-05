package uk.ac.ox.cs.diadem.webapi.testsupport;

/**
 * <p>A StringDatabase provides a key-value mapping, where both,
 * keys and values are strings. Such a StringDatabase is used in
 * testing to record test outputs and check them later on. All
 * the effort for generating these outputs is otherwise lost in
 * future testing.</p>
 * 
 * <p>The main use of a StringDatabase db in a JUnit test is a follows:<br/>
 * 
 * <blockquote> assertTrue(db.check(key,value)); </blockquote>
 * 
 * This call will check if value has been saved for key, and if so,
 * return true. Otherwise, the behavior depends on the {@link Mode},
 * obtained with getMode():
 * 
 * <ul><li> {@link Mode#TEST}: returns false and does not update the database.
 *           Use this {@link Mode} to re-run a test that should run 
 *           without errors.</li> 
 *    <li> {@link Mode#LOCKED}: same as {@link Mode#TEST}, but does not allow to switch {@link Mode}.
 *           The framework uses this {@link Mode} to ensure that no 
 *           StringDatabase is accidentally switched to another {@link Mode}.</li> 
 *    <li> {@link Mode#INTERACTIVE}: asks the user what to do -- to either add a new entry, 
 *           or to update a given entry, and to return true in those cases,
 *           or to return false without changing the database.
 *           Use this {@link Mode} for your the tests you are currently
 *           updating to check changes and to record them if they are fine.</li>
 *   <li> {@link Mode#RECORD}: always returns true and silently updates the database.
 *           Use WITH CARE: If you can be sure that all changes you did are fine,
 *           e.g., when regenerating the database to get rid off old entries,
 *           you can use this {@link Mode} to re-record all values.</li>
 *  </ul> 
 *  </p>
 *           
 *  A separate StringDatabase is generated for each test case, typically through
 *  {@link StandardTestcase}. When testcases become large and have a lot of 
 *  test-methods, it is often helpful to organize the keys hierarchically. To this
 *  end, one can set a prefix with setKeyPrefix(prefix). This prefix is prepended
 *  to all keys provided in subsequent calls, until another prefix is set. For
 *  setting the prefix to the current method name, invoke setMethodKeyPrefix.
 *  
 *  Upon update, the entire Database is flushed onto disk to ensure data loss in
 *  face of crashes. This might make it slow.
 *  
 * @author christian
 *
 */
public interface StringDatabase {

	public enum Mode {
		/**
		 * all deviancies from the recorded data are errors
		 */
		TEST,
		/**
		 * same as {@link Mode#TEST}, but is also unchangable
		 */
		LOCKED,
		/**
		 * all deviancies trigger a user interaction to decide whether to record or report an error
		 */
		INTERACTIVE, 
		/**
		 * all deviancies are silently recorded and no error is reported
		 */
		RECORD;
	}
	
	/**
	 * @return current {@link Mode}
	 */
	public Mode getMode();

	/**
	 * @throws TestSupportException, if the current mode is {@link Mode#LOCKED}.
	 */
	public void setMode(Mode state);

	/**
	 * All subsequent calls to {@link StringDatabase#lookup(String)},
	 * {@link StringDatabase#check(String, String)}, and 
	 * {@link StringDatabase#checkSorted(String, String)} 
	 * prepend prefix their keys.
	 * @param prefix 
	 */
	public void setKeyPrefix(String prefix);
	
	/**
	 * Sets the prefix to "<current method name>/".
	 */
	public void setMethodKeyPrefix();
	
	/**
	 * @return the current prefix
	 */
	public String getKeyPrefix();

	/**
	 * resets the key prefix -- equivalent to setKeyPrefix("")
	 */
	public void resetMethodKeyPrefix();
	
	/**
	 * @param key
	 * @return true if a value for key exists.
	 */
	public abstract String lookup(String key);
	
	/**
	 * @param key
	 * @param value
	 * @return true if a value for key exists and equals value (or if the developer wants to record this change)
	 */
	public boolean check(String key, String value);
	
	/**
	 * Same as {@link StringDatabase#check(String, String)}, but it first
	 * drops all duplicate lines and then sorts them before checking.
	 * This comes in handy, if your output is in implementation dependent order.
	 * @param key
	 * @param value
	 * @return true if a value for key exists and equals the sorted value (or if the developer wants to record this change)
	 */
	public boolean checkSorted(String key, String value);
	
	/**
	 * remove all key-value pairs from the database
	 */
	public void clear();
}