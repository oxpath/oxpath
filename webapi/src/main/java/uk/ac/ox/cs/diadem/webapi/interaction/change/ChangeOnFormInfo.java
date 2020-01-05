package uk.ac.ox.cs.diadem.webapi.interaction.change;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.diadem.webapi.dom.mutation.MutationFormSummary;

public interface ChangeOnFormInfo extends GenericModification {

  enum CHANGE {
    NEW_ENABLED_FIELD_IN_DOM, NEW_DISABLED_FIELD_IN_DOM, APPEARED_ENABLED_FIELD, APPEARED_DISABLED_FIELD, DISABLED_FIELD, ENABLED_FIELD, REMOVED_FIELD, DISAPPEARED_FIELD, FIELD_WITH_NEW_OPTIONS, FIELD_WITH_REMOVED_OPTION, FIELD_WITH_CSS_CHANGE, TEXT_APPEARED, UNSPECIFIED;

    public Pair<String, String> asConstant() {
      return Pair.of("type", name().toLowerCase());
    }
  }

  public CHANGE changeType();

  String getWidgetPMLocalId();

  String getPayload();

  MutationFormSummary getSummary();

}
