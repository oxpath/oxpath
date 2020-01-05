package uk.ac.ox.cs.diadem.oxpath.testsupport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author christian
 * 
 *         TESTING: all methods tested, except for checkSorted. Needs more border cases, such as an empty database.
 * 
 */
class StringDatabaseImplementation implements StringDatabase {

  final static Logger logger = LoggerFactory.getLogger(StringDatabaseImplementation.class);

  private static String databasePath = new String("data") + File.separator + "test" + File.separator;

  protected final static Map<Class<?>, StringDatabaseImplementation> instances = new HashMap<Class<?>, StringDatabaseImplementation>();
  private Map<String, String> database = null;
  final private Class<?> classObject;
  private Mode mode = defaultTestMode();
  private boolean accessed = false;

  static private final String emptyPrefix = "";

  private String prefix = emptyPrefix;

  private final static String testingModeProperty = "testing.mode";

  static private Mode defaultTestMode() {
	//ConfigurationObject.getConfiguration("StringDatabaseTestConfiguration.xml");
	  
    //return ConfigurationFacility.getConfiguration().getEnumNoDefault(testingModeProperty, Mode.TEST);
    return null;
  }

  static public StringDatabase getInstance(final Class<?> classObject) {
    assert classObject != null;
    StringDatabaseImplementation result = instances.get(classObject);
    if (result == null) {
      instances.put(classObject, result = new StringDatabaseImplementation(classObject));
    }
    return result;
  }

  static public StringDatabase getInstance(final Object object) {
    assert object != null;
    StringDatabaseImplementation result = instances.get(object.getClass());
    if (result == null) {
      instances.put(object.getClass(), result = new StringDatabaseImplementation(object.getClass()));
    }
    return result;
  }

  private String getDatabasePath() {
    return databasePath + classObject.getCanonicalName() + ".dat";
  }

  protected StringDatabaseImplementation(final Class<?> classObject) throws TestSupportException {
    logger.debug("Loading StringDatabase for class '{}'...", classObject.getCanonicalName());

    // TODO set the state according to config
    this.classObject = classObject;

    ObjectInputStream is;
    try {
      is = new ObjectInputStream(new FileInputStream(getDatabasePath()));
    } catch (final FileNotFoundException e) {
      database = new HashMap<String, String>();
      logger.debug("... initialized StringDatabase for class '{}'.", classObject.getCanonicalName());
      return;
    } catch (final IOException e) {
      throw new TestSupportException("Failed loading StringDatabase for class '" + classObject.getCanonicalName()
          + "'.", e, logger);
    }
    try {
      try {
        Object readObject = is.readObject();
        if (!(readObject instanceof Class<?>))
          throw new TestSupportException("Failed loading StringDatabase for class '" + classObject.getCanonicalName()
              + "': Got '" + readObject.getClass().getCanonicalName() + "' instead of 'Class<?>'.", logger);
        final Class<?> readClassObject = (Class<?>) readObject;
        if (!readClassObject.equals(classObject)) {
          final String question = "StringDatabase for class '" + classObject.getCanonicalName()
              + "'\n has been generated with '" + readClassObject.getCanonicalName() + "'\nProceed?\n";
          if ((mode != Mode.INTERACTIVE) || !askUserBoolean(question))
            throw new TestSupportException("Failed loading StringDatabase for class '" + classObject.getCanonicalName()
                + "': Found data is associated with '" + readClassObject.getCanonicalName() + "'", logger);
          else {
            accessed = true;
          }
        }

        readObject = is.readObject();
        if (!(readObject instanceof Map<?, ?>))
          throw new TestSupportException("Failed loading StringDatabase for class '" + classObject.getCanonicalName()
              + "': Got '" + readObject.getClass().getCanonicalName() + "' instead of 'Map<?,?>'.", logger);
        @SuppressWarnings("unchecked")
        final Map<String, String> readDabase = (Map<String, String>) readObject;
        database = readDabase;
      } finally {
        is.close();
      }
    } catch (final IOException e) {
      throw new TestSupportException("Failed loading StringDatabase for class '" + classObject.getCanonicalName()
          + "'.", e, logger);
    } catch (final ClassNotFoundException e) {
      throw new TestSupportException("Failed loading StringDatabase for class '" + classObject.getCanonicalName()
          + "'.", e, logger);
    } finally {
      if (database != null) {
        logger.debug("... loaded StringDatabase for class '{}'.", classObject.getCanonicalName());
      } else {
        logger.error("... FAILED loading StringDatabase for class '{}'.", classObject.getCanonicalName());
      }
    }
    if (accessed) {
      flush();
    }
  }

  @Override
  public Mode getMode() {
    return mode;
  }

  @Override
  public void setMode(final Mode newMode) {
    if (mode == Mode.LOCKED)
      throw new TestSupportException("Cannot switch to State '" + newMode.name() + "' -- state is LOCKED.", logger);
    mode = newMode;
  }

  @Override
  public String lookup(final String key) {
    assert key != null;
    accessed = true;
    return database.get(prefix + key);
  }

  @Override
  public boolean checkSorted(final String key, final String value) {
    assert key != null;
    assert value != null;
    final SortedSet<String> set = new TreeSet<String>();
    final BufferedReader reader = new BufferedReader(new StringReader(value));
    try {
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        set.add(line);
      }
    } catch (final IOException e) {
      e.printStackTrace();
      assert false : "Violated Invariance.";
    }
    final StringWriter writer = new StringWriter();
    for (final String line : set) {
      writer.write(line);
      writer.append('\n');
    }
    return check(key, writer.toString());
  }

  @Override
  public boolean check(final String unprefixedKey, final String value) {
    assert value != null;
    assert unprefixedKey != null;
    accessed = true;
    final String key = prefix + unprefixedKey;
    final String expectedValue = database.get(key);
    if (expectedValue == null) {
      switch (mode) {
      case LOCKED:
      case TEST: {
        logger.debug("Missing key/value pair in StringDatabase for class '{}': Key '" + key
            + "' and Value '{}' are missing.", classObject.getCanonicalName(), value);
        return false;
      }
      case RECORD:
        logger.info("Inserting key/value pair in StringDatabase for class '{}': Key '" + key + "' and Value '{}'.",
            classObject.getCanonicalName(), value);
        database.put(key, value);
        flush();
        return true;
      case INTERACTIVE:
        String question = "StringDatabase has no value for key '" + key;
        if (value.indexOf('\n') == -1) {
          question += "'.\nAdd value '" + value + "'?\n";
        } else {
          question += "'.\nAdd value--------------------------------------------\n" + value
              + "\n----------------------------------------------------?\n";
        }

        if (!askUserBoolean(question))
          return false;

        logger.info("Inserting key/value pair in StringDatabase for class '{}': Key '" + key + "' and Value '{}'.",
            classObject.getCanonicalName(), value);
        database.put(key, value);
        flush();
        return true;
      default:
        assert false : "Violated Invariance.";
        return false;
      }
    }

    if (expectedValue.equals(value))
      return true;

    switch (mode) {
    case LOCKED:
    case TEST: {
      logger.debug("Mismatch for key '" + key + "' in StringDatabase for class '" + classObject.getCanonicalName()
          + "': Expected value '{}' Received value '{}'", expectedValue, value);
      return false;
    }
    case RECORD:
      logger.info("Updating value fo key '" + key + "' in StringDatabase for class '" + classObject.getCanonicalName()
          + "': Old value '{}' new value '{}'", expectedValue, value);
      database.put(key, value);
      flush();
      return true;
    case INTERACTIVE:
      String question = "StringDatabase has deviating value for key '" + key;
      if ((value.indexOf('\n') == -1) && (expectedValue.indexOf('\n') == -1)) {
        question += "':\nold value: '" + expectedValue + "'\nnew value: '" + value + "'?\n";
      } else {
        question += "':\nold value:---------------------------------------------------------\n" + expectedValue
            + "\nnew value:---------------------------------------------------------\n" + value
            + "\n------------------------------------------------------------------?\n";
      }

      if (!askUserBoolean(question))
        return false;

      logger.info("Updating value fo key '" + key + "' in StringDatabase for class '" + classObject.getCanonicalName()
          + "': Old value '{}' new value '{}'", expectedValue, value);
      database.put(key, value);
      flush();
      return true;
    default:
      assert false : "Violated Invariance.";
      return false;
    }
  }

  protected void flush() {
    logger.debug("Storing StringDatabase for class '{}'...", classObject.getCanonicalName());
    if (accessed) {
      try {
        final File dataDir = new File("data");
        if (!dataDir.exists()) {
          dataDir.mkdir();
        }
        final File dataTestDir = new File("data/test");
        if (!dataTestDir.exists()) {
          dataTestDir.mkdir();
        }

        final ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("data/test/"
            + classObject.getCanonicalName() + ".dat"));
        os.writeObject(classObject);
        os.writeObject(database);
        os.close();
        accessed = false;
      } catch (final FileNotFoundException e) {
        throw new TestSupportException("Failed saving StringDatabase for class '" + classObject.getCanonicalName()
            + "': " + e.getMessage(), logger);
      } catch (final IOException e) {
        throw new TestSupportException("Failed saving StringDatabase for class '" + classObject.getCanonicalName()
            + "': " + e.getMessage(), logger);
      } finally {
        if (accessed) {
          logger.error("FAILED storing StringDatabase for class '{}'...", classObject.getCanonicalName());
        } else {
          logger.debug("... stored StringDatabase for class '{}'.", classObject.getCanonicalName());
        }
      }
    } else {
      logger.debug("... StringDatabase for class '{}' unaltered and unsaved.", classObject.getCanonicalName());
    }
  }

  @Override
  public void clear() {
    database.clear();
    flush();
  }

  protected char askUser(final String questionText) {
    assert questionText != null;
    BufferedReader reader = null;
    if (reader == null) {
      reader = new BufferedReader(new InputStreamReader(System.in));
    }
    System.out.print(questionText);
    final char userResponse;
    try {
      while (true) {
        final String response = reader.readLine();
        assert response != null : "Violated Invariance";
        if (response.equals("")) {
          continue;
        }
        userResponse = response.charAt(0);
        break;
      }
      // String response;
      // while((response=reader.readLine()).equals(""));
      // userResponse=response.charAt(0);
    } catch (final IOException e) {
      throw new TestSupportException("FAILDED reading user feedback for StringDatabase'"
          + classObject.getCanonicalName() + "': " + e.getMessage(), logger);
    }
    return userResponse;
  }

  protected boolean askUserBoolean(final String question) {
    int userResponse;
    while (true) {
      userResponse = askUser(question);
      if ((userResponse == 'y') || (userResponse == 'Y'))
        return true;
      if ((userResponse == 'n') || (userResponse == 'N'))
        return false;
    }
  }

  @Override
  public void setKeyPrefix(final String prefix) {
    assert prefix != null;
    this.prefix = prefix;
  }

  @Override
  public String getKeyPrefix() {
    return prefix;
  }

  @Override
  public void setMethodKeyPrefix() {
    setKeyPrefix(Thread.currentThread().getStackTrace()[2].getMethodName() + "/");
  }

  @Override
  public void resetMethodKeyPrefix() {
    prefix = emptyPrefix;
  }

  @Override
  public String toString() {
    final StringBuilder bs = new StringBuilder();
    bs.append("Prefix: ");
    bs.append(prefix);
    bs.append("\n");
    bs.append("Mode: ");
    bs.append(mode.name());
    bs.append("\n");
    for (final Entry<String, String> entry : database.entrySet()) {
      bs.append(entry.getKey());
      bs.append(":\n");
      bs.append(entry.getValue());
      bs.append("\n-------------------------------------------\n");
    }
    return bs.toString();
  }
}
