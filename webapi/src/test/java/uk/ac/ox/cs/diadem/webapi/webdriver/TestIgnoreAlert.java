package uk.ac.ox.cs.diadem.webapi.webdriver;

import static org.junit.Assert.fail;
import static org.openqa.selenium.remote.CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR;

import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

@Ignore
public class TestIgnoreAlert {

  static FirefoxDriver driver2;

  private final DesiredCapabilities desiredCaps = DesiredCapabilities.firefox();

  // @Test
  // public void canAcceptUnhandledAlert() {
  // runScenarioWithUnhandledAlert(UnexpectedAlertBehaviour.ACCEPT, "This is a default value");
  // }

  @Test
  public void canDismissUnhandledAlert() {
    runScenarioWithUnhandledAlert(UnexpectedAlertBehaviour.DISMISS, "null");
  }

  @AfterClass
  public static void shutdown() {
    driver2.quit();
  }

  @Test
  @Ignore
  public void dismissUnhandledAlertsByDefault() {
    runScenarioWithUnhandledAlert(null, "null");
  }

  @Test
  public void canIgnoreUnhandledAlert() {
    try {
      runScenarioWithUnhandledAlert(UnexpectedAlertBehaviour.IGNORE, "Text ignored");
      fail("Exception not thrown");
    } catch (final UnhandledAlertException ex) {
      // this is expected
    }
    driver2.switchTo().alert().dismiss();
  }

  @Test
  @Ignore
  public void canSpecifyUnhandledAlertBehaviourUsingCapabilities() {
    desiredCaps.setCapability(UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);

    driver2 = new FirefoxDriver(desiredCaps);

    runScenarioWithUnhandledAlert("This is a default value");
  }

  private void runScenarioWithUnhandledAlert(final String expectedAlertText) {
    // driver2.get(WebDriverTestCase.class.getResource("alerts.html").toExternalForm());
    driver2.get("http://web.archive.org/web/20090827015310/http://www.bhphotovideo.com:80/");
    // driver2.findElement(By.id("prompt-with-default")).click();
    try {
      driver2.findElement(By.id("text")).getText();
    } catch (final UnhandledAlertException expected) {
      System.out.println(expected);
    }

    final WebElement findElement = driver2.findElement(By.id("text"));
    System.out.println(findElement);
  }

  private void runScenarioWithUnhandledAlert(final UnexpectedAlertBehaviour behaviour, final String expectedAlertText) {
    if (behaviour != null) {
      desiredCaps.setCapability(UNEXPECTED_ALERT_BEHAVIOUR, behaviour);
    }
    driver2 = new FirefoxDriver(desiredCaps);
    runScenarioWithUnhandledAlert(expectedAlertText);
  }
}