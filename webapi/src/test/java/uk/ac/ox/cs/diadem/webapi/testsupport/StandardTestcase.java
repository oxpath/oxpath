package uk.ac.ox.cs.diadem.webapi.testsupport;

import java.io.StringWriter;

//import org.junit.After;
//import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author christian
 * 
 */
abstract public class StandardTestcase {
  @Deprecated
  protected final Logger logger;
  protected final Logger LOGGER;
  protected final StringDatabase database;

//  @Before
  final public void standardTestcaseTearUp() {
    database.resetMethodKeyPrefix();
    
    
    //ConfigurationFacility.resetApplicationConfiguration();
    
    
    logger.info("Entering Testcase... ");
    // TODO identify the current test method for logging message?!
  }

//  @After
  final public void standardTestcaseTearDown() {
    logger.info("... leaving Testcase.");
  }

  protected StandardTestcase() {
    LOGGER = LoggerFactory.getLogger(this.getClass());
    logger = LOGGER;
    database = TestFacilities.getStringDatabase(this.getClass());
  }

  protected static <E> String toString(E[] array) {
    final StringWriter writer = new StringWriter();
    writer.append("[ '");
    boolean first = true;
    for (final E e : array) {
      if (first) {
        first = false;
      } else {
        writer.append("', '");
      }
      writer.append(e.toString());
    }
    writer.append("' ]");
    return writer.toString();
  }

}
