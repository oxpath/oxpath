/**
 *
 */
package uk.ac.ox.cs.diadem.oxpath.core;

/**
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public class StandardOXPathTest extends AbstractOXPathTestCase {

//  /**
//   * standard error for number tests in the {@code primitiveDataTest} method.
//   */
//  private static final double ASSERTDELTA = 0.005;
//  /**
//   *
//   */
//  private static final String TESTDELIMITER = "%%%";
//
//  // private static final WebBrowser browser = BrowserFactory.newWebBrowser(Engine.SWT_MOZILLA, true);
//  //
//  // // private ObjectOutputStream os;
//  // @AfterClass
//  // public static void init() {
//  // browser.shutdown();
//  // }
//
//  @Test
//  @Ignore
//  public void primitiveDataTest() throws DOMException, ParserConfigurationException, OXPathException, Exception {
//
//    @SuppressWarnings("unchecked")
//    final List<String> readLines = FileUtils.readLines(FileUtils.toFile(StandardOXPathTest.class
//        .getResource("queries.txt")));
//
//    final ArrayList<Tester> tests = new ArrayList<Tester>();
//
//    String tempExpr;
//
//    for (final String line : readLines) {
//      final Scanner s = new Scanner(line).useDelimiter("\\s*" + TESTDELIMITER + "\\s*");
//      tempExpr = s.next();
//      tests.add((s.hasNext()) ? new Tester(tempExpr, s.next()) : new Tester(tempExpr));
//      s.close();
//    }
//
//    OXPathType result;
//    for (final Tester tester : tests) {
//      final IOutputHandler handler = new StringDBOutputHandler(database);
//      logger.info("Starting evaluation on expression '{}'", tester.getExpression());
//
//      result = OXPath.ENGINE.evaluate(tester.getExpression(), browser, handler);
//
//      logger.info("Expression '{}' done with result '{}'", tester.getExpression(), result);
//      Thread.sleep(1000);
//      if (tester.hasExpected())
//        if (tester.getExpected().startsWith("true") || tester.getExpected().startsWith("false")) {
//          assertEquals(Boolean.parseBoolean(tester.getExpected()), result.booleanValue());
//        } else {// check for doubles and String
//          boolean isNumber = true;
//          try {
//            final double expectedAsDouble = Double.parseDouble(tester.getExpected());
//            assertEquals(expectedAsDouble, result.number().doubleValue(), ASSERTDELTA);
//          } catch (final NumberFormatException e) {
//            isNumber = false;
//          }// catch this; means not a number and we do a String test
//          if (!isNumber) {
//            assertEquals(tester.getExpected(), result.string());
//          }
//        }
//    }
//  }
//
//  @Test
//  @Ignore
//  public void amazonTest() throws DOMException, ParserConfigurationException, OXPathException, Exception {
//    database.setMethodKeyPrefix();
//    database.setMode(Mode.TEST);
//    executeExtractionTest("amazon.txt");
//  }
//
//  /**
//   * Use this as a basis for other tests that compare expected vs. actual extraction nodes via the database; takes a
//   * single parameter, {@code filename}, which is the filename containing the expression to test. So the StringDB works
//   * correctly, <b>HIGHLY RECOMMEND</b> that only one expression per class is tested (as keys are appended with method
//   * names).
//   *
//   * @param filename
//   *          file name containing the expression to test
//   * @throws Exception
//   * @throws OXPathException
//   * @throws ParserConfigurationException
//   * @throws DOMException
//   */
//  public void executeExtractionTest(final String filename) throws DOMException, ParserConfigurationException,
//  OXPathException, Exception {
//    @SuppressWarnings("unchecked")
//    final List<String> tests = FileUtils.readLines(FileUtils.toFile(StandardOXPathTest.class.getResource(filename)));
//
//    OXPathType result;
//    for (final String test : tests) {
//      final IOutputHandler handler = new StringDBOutputHandler(database);
//      logger.info("Starting evaluation on expression '{}'", test);
//      result = OXPath.ENGINE.evaluate(test, browser, handler);
//      logger.info("Expression '{}' done with result '{}'", test, result);
//      Thread.sleep(1000);
//    }
//  }
//
//  private class Tester {
//
//    public Tester(final String expr, final String expect) {
//      expression = expr;
//      expected = expect;
//      hasExpected = true;
//    }
//
//    public Tester(final String expr) {
//      expression = expr;
//      expected = "";
//      hasExpected = false;
//    }
//
//    public String getExpression() {
//      return expression;
//    }
//
//    public String getExpected() {
//      return expected;
//    }
//
//    public boolean hasExpected() {
//      return hasExpected;
//    }
//
//    private final String expression;
//    private final String expected;
//    private final boolean hasExpected;
//  }

}
