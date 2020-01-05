package uk.ac.ox.cs.diadem.webapi.interaction.impl;

import java.text.MessageFormat;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus.Cause;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus.Status;

public enum StatusExecution {

  FACTORY;

  public ExecutionStatus failedSelectAction(final String valueToFill, final String actualValue) {
    return new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return Status.FAIL;
      }

      @Override
      public String getReadableInfo() {

        return MessageFormat.format("Selected option: {0} but the actual option set is {1}", valueToFill, actualValue);
      }

      @Override
      public Cause getCause() {
        return Cause.INCONSISTENT_SELECTION;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper("ExecutionStatus").add("status", getStatus()).add("cause", getCause())
            .add("info", getReadableInfo()).toString();
      }

      @Override
      public Object forTest() {

        return null;
      }
    };
  }

  public ExecutionStatus failedBackAction(final String urlTarget) {

    return new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return Status.FAIL;
      }

      @Override
      public String getReadableInfo() {

        return MessageFormat.format("Error going back to {0}, landed at an error page", urlTarget);
      }

      @Override
      public Cause getCause() {
        return Cause.UNKNOWN;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper("ExecutionStatus").add("status", getStatus()).add("cause", getCause())
            .add("info", getReadableInfo()).toString();
      }

      @Override
      public Object forTest() {

        return null;
      }
    };
  }

  public ExecutionStatus failedTypeInAction(final String valueToFill, final String actualValue) {
    return new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return Status.FAIL;
      }

      @Override
      public String getReadableInfo() {

        return MessageFormat.format("Typed value: {0} but the actual value set is {1}", valueToFill, actualValue);
      }

      @Override
      public Cause getCause() {
        return Cause.INCONSISTENT_TYPING;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper("ExecutionStatus").add("status", getStatus()).add("cause", getCause())
            .add("info", getReadableInfo()).toString();
      }

      @Override
      public Object forTest() {

        return null;
      }
    };
  }

  public ExecutionStatus failedActionTimeout(final Status status, final Cause cause) {
    return new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return status;
      }

      @Override
      public String getReadableInfo() {
        return "Action failed to timeout";
      }

      @Override
      public Cause getCause() {
        return cause;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper("ExecutionStatus").add("status", getStatus()).add("cause", getCause())
            .add("info", getReadableInfo()).toString();
      }

      @Override
      public Object forTest() {
        return null;
      }
    };
  }

  public ExecutionStatus failedNavigateAction(final Status status, final String url, final String landingURL,
      final int httpstatus, final Cause cause) {
    return new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return status;
      }

      @Override
      public String getReadableInfo() {
        return MessageFormat.format("Http error code {3}, target URL [{0}] but landed at [{1}]. ", url, landingURL,
            httpstatus);
      }

      @Override
      public Cause getCause() {
        return cause;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper("ExecutionStatus").add("status", getStatus()).add("cause", getCause())
            .add("info", getReadableInfo()).toString();
      }

      @Override
      public Object forTest() {

        return null;
      }
    };
  }

  public ExecutionStatus invisibleTarget() {
    return new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return Status.NON_EXECUTABLE;
      }

      @Override
      public String getReadableInfo() {

        return "Target element is not visible or not enabled";
      }

      @Override
      public Cause getCause() {
        return Cause.INVISIBLE_TARGET;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper("ExecutionStatus").add("status", getStatus()).add("cause", getCause())
            .add("info", getReadableInfo()).toString();
      }

      @Override
      public Object forTest() {

        return null;
      }
    };
  }

  public ExecutionStatus disabledElement() {
    return new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return Status.NON_EXECUTABLE;
      }

      @Override
      public String getReadableInfo() {

        return "Target element is enabled";
      }

      @Override
      public Cause getCause() {
        return Cause.DISABLED;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper("ExecutionStatus").add("status", getStatus()).add("cause", getCause())
            .add("info", getReadableInfo()).toString();
      }

      @Override
      public Object forTest() {

        return null;
      }
    };
  }

  public ExecutionStatus successAction(final String value) {
    return new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return Status.SUCCESS;
      }

      @Override
      public String getReadableInfo() {
        return "successful action per value " + value;
      }

      @Override
      public Cause getCause() {
        return Cause.SUCCESS;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper("ExecutionStatus").add("status", getStatus()).add("cause", getCause())
            .add("info", getReadableInfo()).toString();
      }

      @Override
      public Object forTest() {

        return value;
      }

    };
  }

  public ExecutionStatus unspecifiedAfterAction(final String value) {
    return new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return Status.UNSPECIFIED;
      }

      @Override
      public String getReadableInfo() {
        return "unspecified status: " + value;
      }

      @Override
      public Cause getCause() {

        return Cause.UNKNOWN;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper("ExecutionStatus").add("status", getStatus()).add("cause", getCause())
            .add("info", getReadableInfo()).toString();
      }

      @Override
      public Object forTest() {

        return null;
      }
    };
  }

  public ExecutionStatus failedAction(final String value) {
    return new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return Status.FAIL;
      }

      @Override
      public String getReadableInfo() {
        return "failed: " + value;
      }

      @Override
      public Cause getCause() {

        return Cause.UNKNOWN;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper("ExecutionStatus").add("status", getStatus()).add("cause", getCause())
            .add("info", getReadableInfo()).toString();
      }

      @Override
      public Object forTest() {

        return null;
      }
    };
  }

  public ExecutionStatus failedActionOnStaleElement(final String value) {
    return new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return Status.FAIL;
      }

      @Override
      public String getReadableInfo() {
        return "failed: " + value;
      }

      @Override
      public Cause getCause() {

        return Cause.STALE_DOM_NODE;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper("ExecutionStatus").add("status", getStatus()).add("cause", getCause())
            .add("info", getReadableInfo()).toString();
      }

      @Override
      public Object forTest() {

        return null;
      }
    };
  }

  public ExecutionStatus failedClickAction(final String... msg) {
    return new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return Status.FAIL;
      }

      @Override
      public String getReadableInfo() {
        return "click seems to be not performed due to:" + Joiner.on(",").join(msg);
      }

      @Override
      public Cause getCause() {

        return Cause.UNKNOWN;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper("ExecutionStatus").add("status", getStatus()).add("cause", getCause())
            .add("info", getReadableInfo()).toString();
      }

      @Override
      public Object forTest() {

        return null;
      }
    };
  }

  public ExecutionStatus failedMoveToFrameAction() {
    return new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return Status.FAIL;
      }

      @Override
      public String getReadableInfo() {
        return "Target not available for move to frame. ";
      }

      @Override
      public Cause getCause() {

        return Cause.UNKNOWN;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper("ExecutionStatus").add("status", getStatus()).add("cause", getCause())
            .add("info", getReadableInfo()).toString();
      }

      @Override
      public Object forTest() {

        return null;
      }

    };
  }

  public ExecutionStatus autocompleteSuccess(final String clicked) {
    return new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return Status.SUCCESS;
      }

      @Override
      public String getReadableInfo() {
        return "Autocomplete detected and clicked on element: " + clicked;
      }

      @Override
      public Cause getCause() {

        return Cause.SUCCESS;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper("ExecutionStatus").add("status", getStatus()).add("cause", getCause())
            .add("info", getReadableInfo()).toString();
      }

      @Override
      public Object forTest() {

        return clicked.toString();
      }

    };
  }

  // private static final Configuration configuration = ConfigurationFacility.getConfiguration();
  // private static final String INCONSISTENT_FILLING = configuration
  // .getString("facts.actions.feedbacks.inconsistent_filling");
  // private static final String SUCCESS_FEEDBACK = configuration.getString("facts.actions.feedbacks.success");
  // private static final String FAIL_FEEDBACK = configuration.getString("facts.actions.feedbacks.fail");
  // private static final String UNSPECIFIED_FEEDBACK = configuration.getString("facts.actions.feedbacks.unspecified");
  // private static final String NON_EXECUTABLE_FEEDBACK = configuration
  // .getString("facts.actions.feedbacks.nonexecutable");

}
