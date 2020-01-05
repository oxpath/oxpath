/**
 *
 */
package uk.ac.ox.cs.diadem.webapi;

import java.net.URI;
import java.net.URL;

import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPINoSuchElementException;
import uk.ac.ox.cs.diadem.webapi.interaction.AdviceProcessor;
import uk.ac.ox.cs.diadem.webapi.interaction.WebActionExecutor;

/**
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public interface MiniBrowser {

  /**
   * Returns a WebActionExecutor if available for this implementation, or null otherwise
   * @return
   */
  WebActionExecutor getActionExecutor();
  
  AdviceProcessor getAdviceProcessor();

  /**
   * It returns the DOMElement identified by the given xpath.
   * @param xPathLocator
   * @return the DOMElement
   * @throws WebAPINoSuchElementException if the element is not found, or the found node is not an element node
   */
  DOMElement getDOMElementByLocator(String xPathLocator) throws WebAPINoSuchElementException;

  /**
   * Navigate to the given URL and waits until the new page is fully loaded (listen to "load" JavaScript event)
   * @throws a WebAPITimeoutException if the set timeout is exceeded
   * @param uRI
   */
  void navigate(String uRI);

  void navigate(URI uri);

  /**
   * Forces back button on the browser
   */
  void back();

  /**
   * Returns the URL of the resource that the web browser is currently displaying, or null if no navigation to any url
   * has been performed yet.
   */
  String getLocationURL();

  /**
   * Returns the a URL object for {@link #getLocationURL()}.
   */
  URL getURL();

  /**
   * Shuts down the browser.
   */
  void shutdown();
    
  /**
 * TODO Describe.
 */
public class Advice {

		private String field;
		private String value;
		
		public Advice() {}
		
		public Advice(String field, String value) {
			this.field = field;
			this.value = value;
		}

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return "Advice [field=\"" + field + "\", value=\"" + value + "\"]";
		}
		
		
	}

}
