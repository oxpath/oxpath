/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.MutationFormSummary;
import uk.ac.ox.cs.diadem.webapi.interaction.change.AlertInfo;
import uk.ac.ox.cs.diadem.webapi.interaction.change.ChangeOnFormInfo;
import uk.ac.ox.cs.diadem.webapi.interaction.change.ChangeOnFormInfo.CHANGE;
import uk.ac.ox.cs.diadem.webapi.interaction.change.GenericModification;
import uk.ac.ox.cs.diadem.webapi.interaction.change.GenericModification.ModificationType;
import uk.ac.ox.cs.diadem.webapi.interaction.change.IFrameChange;
import uk.ac.ox.cs.diadem.webapi.interaction.change.MajorPageChangeInfo;
import uk.ac.ox.cs.diadem.webapi.interaction.change.MinorPageChanges;
import uk.ac.ox.cs.diadem.webapi.interaction.change.NewPageInfo;
import uk.ac.ox.cs.diadem.webapi.pagestate.MajorChangeType;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public final class Reports {

  private static final Logger LOGGER = LoggerFactory.getLogger(Reports.class);

  private Reports() {
    // prevents instantiate
  }

  /**
   * Create a new {@link ModificationReportBuilder}
   *
   * @return
   */
  public static ModificationReportBuilder newModificationReportBuilder() {
    return new ModificationReportBuilder();
  }

  public static ActionExecutionReportBuilder newActionModificationReportBuilder() {
    return new ActionExecutionReportBuilder();
  }

  /**
   * Builder for reporting changes
   *
   * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
   */
  public static class ModificationReportBuilder {

    private String alertMessage;
    private boolean alertDetected;
    private String newURL;
    private String newTitle;
    private boolean newWindow;
    private boolean newLocation;
    private Boolean pageDifferences = false;
    private MajorChangeType majorChangeType;
    private final Boolean iFramechanges = false;
    private final Boolean minorPageChanges = false;
    private final Set<ChangeOnFormInfo> changeOnForms = Sets.newHashSet();

    private ModificationReportBuilder() {
    }

    public ModificationReport build() {
      return new ModificationReportImpl(this);
    }

    public void alertDetected(final String alertMessage) {
      alertDetected = true;
      this.alertMessage = alertMessage;

    }

    public void newWindowOpened(final String newURL, final String newTitle) {
      newWindow = true;
      this.newURL = newURL;
      this.newTitle = newTitle;

    }

    public void pageModification(final String newURL, final String newTitle) {
      newLocation = true;
      this.newURL = newURL;

    }

    public void newFormChange(final ChangeOnFormInfo change) {
      changeOnForms.add(change);

    }

    public void majorPageModification(final MajorChangeType type) {
      majorChangeType = type;
      pageDifferences = true;

    }

    public class BuilderForChangeOnFormInfo_OLD {

      private CHANGE changeType = ChangeOnFormInfo.CHANGE.UNSPECIFIED;
      private String addedOptionValue;
      private String newFieldName;
      private String removedFieldName;
      private String removedOptionValue;

      private String newTextValue;

      private final String formNodeLocalId;

      public BuilderForChangeOnFormInfo_OLD(final String formNodeLocalId) {
        this.formNodeLocalId = formNodeLocalId;

      }

      public void reportOptionAddition() {
        changeType = ChangeOnFormInfo.CHANGE.FIELD_WITH_NEW_OPTIONS;

      }

      public void reportOptionDeletion() {
        changeType = ChangeOnFormInfo.CHANGE.FIELD_WITH_REMOVED_OPTION;

      }

      public boolean reportAttributeChange(final String attributeName, final String oldValue, final String newValue) {
        final HashSet<String> set = Sets.newHashSet("disabled", "selected", "style");
        // TODO check if selected or not
        if (set.contains(attributeName.toLowerCase())) {
          changeType = ChangeOnFormInfo.CHANGE.UNSPECIFIED;
          return true;
        }
        return false;

      }

      public void reportTextChange(final String oldValue, final String textContent) {
        newTextValue = textContent;
        changeType = ChangeOnFormInfo.CHANGE.TEXT_APPEARED;
      }

      public ChangeOnFormInfo build() {

        return new ChangeOnFormInfo() {
          @Override
          public MutationFormSummary getSummary() {
            return null;
          }

          @Override
          public ModificationType getType() {

            return ModificationType.CHANGE_ON_FORM;
          }

          @Override
          public CHANGE changeType() {
            return changeType;
          }

          @Override
          public String getPayload() {
            switch (changeType) {
            case NEW_ENABLED_FIELD_IN_DOM:
              return newFieldName;
            case REMOVED_FIELD:
              return removedFieldName;
            case TEXT_APPEARED:
              return newTextValue;
            case FIELD_WITH_NEW_OPTIONS:
            case FIELD_WITH_REMOVED_OPTION:
              return null;
            case UNSPECIFIED:
              return "unspecified";
            default:
              LOGGER.error("unexpected change type <{}>", changeType);
              throw new RuntimeException("unexpected change type " + changeType);
            }
          }

          @Override
          public String getWidgetPMLocalId() {
            return formNodeLocalId;
          }
        };
      }

      public void reportNewField(final String newFiledName) {
        newFieldName = newFiledName;
        changeType = ChangeOnFormInfo.CHANGE.NEW_ENABLED_FIELD_IN_DOM;

      }

      public void reportRemovedField(final String removedFieldName) {
        this.removedFieldName = removedFieldName;
        changeType = ChangeOnFormInfo.CHANGE.REMOVED_FIELD;

      }

    }

    /**
     * NEw one
     *
     * @author giog
     * @param <E>
     *
     */
    public class BuilderForChangeOnFormInfo {

      Set<Pair<ChangeOnFormInfo.CHANGE, Set<DOMElement>>> changes = new HashSet<Pair<ChangeOnFormInfo.CHANGE, Set<DOMElement>>>();
      private Set<String> textChanges;
      private MutationFormSummary formSummary;
      private final String formNodeLocalId;

      public BuilderForChangeOnFormInfo(final String formNodeLocalId) {
        this.formNodeLocalId = formNodeLocalId;

      }

      public void reportAppearedText(final Set<String> textContent) {
        textChanges = textContent;
      }

      public void reportNewFields(final Set<DOMElement> newFields) {
        changes.add(Pair.of(ChangeOnFormInfo.CHANGE.NEW_ENABLED_FIELD_IN_DOM, newFields));
      }

      public void reportRemovedFields(final Set<DOMElement> removedFields) {
        changes.add(Pair.of(ChangeOnFormInfo.CHANGE.REMOVED_FIELD, removedFields));
      }

      public void reportFieldsHavingNewOptions(final Set<DOMElement> fieldsHavingNewOptions) {
        changes.add(Pair.of(ChangeOnFormInfo.CHANGE.FIELD_WITH_NEW_OPTIONS, fieldsHavingNewOptions));
      }

      public ChangeOnFormInfo build() {
        return new ChangeOnFormInfo() {
          @Override
          public MutationFormSummary getSummary() {
            return formSummary;
          }

          @Override
          public String getWidgetPMLocalId() {

            return formNodeLocalId;
          }

          @Override
          public ModificationType getType() {
            throw new RuntimeException("Shoudn't be invoked");
          }

          @Override
          public String getPayload() {
            throw new RuntimeException("Shoudn't be invoked");
          }

          @Override
          public CHANGE changeType() {
            throw new RuntimeException("Shoudn't be invoked");
          }

        };
      }

      public void summary(final MutationFormSummary formSummary) {
        this.formSummary = formSummary;

      }

    }

  }

  public static class ActionExecutionReportBuilder {
    private uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus status;
    private ModificationReportBuilder changeReportBuilder;

    // private ActionExecutionReportBuilder(final PMAction pmAction) {
    // this.pmAction = pmAction;
    // }

    public ActionExecutionReport build() {

      return new ActionExecutionReportImpl(this, changeReportBuilder.build());
    }

    public void actionStatus(final uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus status) {
      this.status = status;
    }

    public ExecutionStatus getCurrentExecutionStatus() {
      return status;
    }

    public ModificationReportBuilder createOrGetModificationReportBuilder() {
      if (changeReportBuilder == null) {
        changeReportBuilder = Reports.newModificationReportBuilder();
      }
      return changeReportBuilder;
    }

  }

  private static class ModificationReportImpl implements ModificationReport {

    private final Boolean isAlert;
    private final Boolean newLocationPage;
    private final Boolean pageDifferences;
    private final Boolean newWindow;
    private final Boolean formchanges;
    private final Boolean iFramechanges;
    private final Boolean minorPageChanges;
    private final String newURL;
    private final String alertMessage;
    private final Set<ModificationType> detectedChanges;
    private final Set<ChangeOnFormInfo> changeOnForms;
    private final MajorChangeType majorChangeType;

    private ModificationReportImpl(final ModificationReportBuilder builder) {
      isAlert = builder.alertDetected;
      alertMessage = builder.alertMessage;
      newLocationPage = builder.newLocation;
      pageDifferences = builder.pageDifferences;
      majorChangeType = builder.majorChangeType;
      newWindow = builder.newWindow;
      newURL = builder.newURL;
      iFramechanges = builder.iFramechanges;
      formchanges = !builder.changeOnForms.isEmpty();
      changeOnForms = builder.changeOnForms;
      minorPageChanges = builder.minorPageChanges;
      ;
      final ModificationType[] values = GenericModification.ModificationType.values();
      detectedChanges = Sets.newHashSet();
      for (final ModificationType type : values) {
        if (isDetected(type)) {
          detectedChanges.add(type);
        }
      }
      // formchanges=builder.
      // minorPageChanges=builder.
    }

    private Boolean isDetected(final ModificationType modificationType) {
      switch (modificationType) {
      case ALERT:
        return isAlert;
      case NEW_PAGE:
        return newLocationPage || newWindow;
      case MAJOR_PAGE_CHANGE:
        return pageDifferences;
      case CHANGE_ON_FORM:
        return formchanges;
      case IFRAME_CHANGE:
        return iFramechanges;
      case MINOR_PAGE_CHANGE:
        return minorPageChanges;
      default:
        LOGGER.error("modification type not considered: <{}>", modificationType);
        return false;
      }
    }

    @Override
    public int hashCode() {
      return toString().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj instanceof ModificationReport)
        return toString().equals(obj.toString());
      return false;
    }

    @Override
    public String toString() {
      final ToStringHelper toStringBuilder = Objects.toStringHelper(getClass().getSimpleName());

      final ModificationType[] values = GenericModification.ModificationType.values();
      for (final ModificationType type : values) {
        toStringBuilder.add(type.toString(), isDetected(type));
      }

      return toStringBuilder.toString();
    }

    @Override
    public NewPageInfo getNewPageInfo() {
      return new NewPageInfo() {

        @Override
        public ModificationType getType() {
          return ModificationType.NEW_PAGE;
        }

        @Override
        public String url() {
          return newURL;
        }

        @Override
        public Boolean isNewWindow() {
          return newWindow;
        }
      };
    }

    @Override
    public MajorPageChangeInfo getMajorPageContentInfo() {

      return new MajorPageChangeInfo() {

        @Override
        public ModificationType getType() {
          return ModificationType.MAJOR_PAGE_CHANGE;
        }

        @Override
        public MajorChangeType changeType() {
          return majorChangeType;
        }
      };
    }

    @Override
    public Set<ModificationType> detectedChanges() {
      return detectedChanges;
    }

    @Override
    public AlertInfo getAlertPayload() {
      return new AlertInfo() {

        @Override
        public ModificationType getType() {
          return ModificationType.ALERT;
        }

        @Override
        public String getMessage() {
          return alertMessage;
        }
      };
    }

    @Override
    public Set<ChangeOnFormInfo> getChangeOnFormInfo() {
      return changeOnForms;
    }

    @Override
    public IFrameChange getIFrameChanges() {
      unsupported("IFrameChange");
      return null;
    }

    @Override
    public MinorPageChanges getMinorPageChanges() {
      unsupported("MinorPageChanges");
      return null;
    }

    private void unsupported(final String message) {
      throw new RuntimeException("Unsupported yet " + message);

    }
  }

  private static class ActionExecutionReportImpl implements ActionExecutionReport {

    private final ModificationReport modificationReport;
    private final ExecutionStatus status;

    private ActionExecutionReportImpl(final ActionExecutionReportBuilder builder,
        final ModificationReport modificationReport) {
      this.modificationReport = modificationReport;
      status = builder.status;
    }

    @Override
    public ExecutionStatus getActionExecutionStatus() {
      return status;
    }

    @Override
    public ModificationReport getModificationReport() {
      return modificationReport;
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(this.getClass().getSimpleName()).add("status", status)
          .add("modificationReport", modificationReport).toString();
    }

    // @Override
    // public PMAction getPMDomAction() {
    // return pmAction;
    // }
  }

}
