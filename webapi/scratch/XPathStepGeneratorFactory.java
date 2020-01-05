package uk.ac.ox.cs.diadem.webapi.dom.finder;

public class XPathStepGeneratorFactory {

  /**
   * The instance we use -- the interface does not guarantee at all a static instance.
   */
  static private UnionXPathStepGenerator INSTANCE = null;

  public static XPathLocator create() {
    if (INSTANCE == null) {
      INSTANCE = new UnionXPathStepGenerator();
      INSTANCE.add(new AttributeXPathStepGenerator());
      INSTANCE.add(new PlainTagXPathStepGenerator());
    }
    return INSTANCE;
  }
}
