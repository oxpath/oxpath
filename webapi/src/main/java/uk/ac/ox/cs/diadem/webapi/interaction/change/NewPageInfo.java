package uk.ac.ox.cs.diadem.webapi.interaction.change;

public interface NewPageInfo extends GenericModification {

  String url();

  Boolean isNewWindow();
}
