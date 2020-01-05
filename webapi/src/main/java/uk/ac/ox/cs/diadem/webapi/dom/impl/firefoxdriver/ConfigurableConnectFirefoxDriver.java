/* 
 * COPYRIGHT (C) 2010-2015 DIADEM Team, Department of Computer Science, Oxford University. All Rights Reserved. 
 * 
 * This software is the confidential and proprietary information of the DIADEM project ("DIADEM"), Department of Computer Science, 
 * Oxford University ("Confidential Information").  You shall not disclose such Confidential Information and shall use 
 * it only in accordance with the terms of the license agreement you entered into with DIADEM.
 */

package uk.ac.ox.cs.diadem.webapi.dom.impl.firefoxdriver;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.ExtensionConnection;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.NewProfileExtensionConnection;
import org.openqa.selenium.internal.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author timfu
 */
public class ConfigurableConnectFirefoxDriver extends FirefoxDriver {
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(ConfigurableConnectFirefoxDriver.class);
  /*@SuppressWarnings("unused")
  private static final Configuration config = ConfigurationFacility.getConfiguration();*/

  public ConfigurableConnectFirefoxDriver() {
    super();
  }

  public ConfigurableConnectFirefoxDriver(final FirefoxProfile profile) {
    super(profile);
  }

  public ConfigurableConnectFirefoxDriver(final Capabilities desiredCapabilities) {
    super(desiredCapabilities);
  }

  public ConfigurableConnectFirefoxDriver(final Capabilities desiredCapabilities,
      final Capabilities requiredCapabilities) {
    super(desiredCapabilities, requiredCapabilities);
  }

  @Override
  protected ExtensionConnection connectTo(final FirefoxBinary binary, final FirefoxProfile profile, final String host) {
    final Lock lock = obtainLock(profile);
    try {
      final FirefoxBinary bin = binary == null ? new FirefoxBinary() : binary;

      // if (FirefoxDriver.USE_MARIONETTE) {
      // // System.out.println("************************** Using marionette");
      // return new MarionetteConnection(lock, bin, profile, host);
      // } else {
      return new NewProfileExtensionConnection(lock, bin, profile, "127.0.0.1");
      // }
    } catch (final Exception e) {
      throw new WebDriverException(e);
    } finally {
      lock.unlock();
    }
  }

}
