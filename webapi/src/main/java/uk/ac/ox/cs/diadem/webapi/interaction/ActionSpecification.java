/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction;

import java.net.URL;

import com.google.common.base.Optional;

import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.interaction.WebActionExecutor.ActionType;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface ActionSpecification {

  // public ExecutionStatus execute() throws WebAPIInteractionException;

  /**
   * Internal use for easier testing
   *
   * @return
   * @throws ProcessingException
   */
  // public ExecutionStatus executeMock(DOMElement target, Map<String, Object> parameters)
  // throws WebAPIInteractionException;

  // public ExecutionStatus execute(FactSerializer serializer) throws ProcessingException;

  // public WebBrowser getBrowser();

  // public PMAction getPMAction();

  public ObservedInteraction getChangesToWatch();

  /**
   * @return
   */
  public String getPMActionString();

  // private final String idAsString;
  public String actionId();

  // private DOMElement target;
  public Optional<DOMElement> getTargetElement();

  // private String xPathLocator;
  public Optional<String> getTargetXPathLocator();

  // private boolean isFormAction;
  public boolean isFormAction();

  // private String pmActionAsString;
  public String getActionDescription();

  // private boolean isRadioButton;
  public boolean isTargetRadioButton();

  // private ActionType actionType;
  public ActionType getActionType();

  // private int optionIndex;
  public Optional<Integer> getOptionIndex();

  // private String value;
  public Optional<String> getParamenterValue();

  // private URL url;
  public Optional<URL> getParamenterURL();

  // private String errorMessage;
  public Optional<String> getErrorMessage();
}
