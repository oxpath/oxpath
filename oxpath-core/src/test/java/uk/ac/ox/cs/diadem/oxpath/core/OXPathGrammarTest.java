package uk.ac.ox.cs.diadem.oxpath.core;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.Node;
import uk.ac.ox.cs.diadem.oxpath.testsupport.StandardTestcase;
import uk.ac.ox.cs.diadem.oxpath.testsupport.StringDatabase.Mode;
import uk.ac.ox.cs.diadem.oxpath.utils.DumpTreeVisitor;
import uk.ac.ox.cs.diadem.oxpath.utils.PrintVisitor;


public class OXPathGrammarTest extends StandardTestcase {

  @Test
  public void testValidExpressions() throws IOException, OXPathException {
    database.setMode(Mode.INTERACTIVE);
    @SuppressWarnings("unchecked")
    final List<String> tests = FileUtils.readLines(FileUtils.toFile(OXPathGrammarTest.class
        .getResource("oxpath-expressions-valid.txt")));

    for (final String test : tests) {// asserts are built into OXPathStringDBOuputHandler
      final Node root = OXPathParser.getJJTreeFromString(test);
      new PrePAATVisitor().accept(root, null);
      // ((SimpleNode) root).dump("");
      assertTrue(database.check(test, new PrintVisitor().accept(root, null)));
      // System.out.println(new PrintVisitor().accept(root, null));
    }
  }

  @Test
  public void testParsing() throws IOException, OXPathException {
    database.setMode(Mode.INTERACTIVE);
    final URL resource = OXPathGrammarTest.class.getResource("test-parser.txt");
    final File file = FileUtils.toFile(resource);
    @SuppressWarnings("unchecked")
    final List<String> tests = FileUtils.readLines(file);

    for (final String test : tests) {// asserts are built into OXPathStringDBOuputHandler
      if (test.startsWith("#")) {
        continue;
      }
//      System.out.println("+PROCESSING ");
//      System.out.println(test);
      final Node root = OXPathParser.getJJTreeFromString(test);
//      System.out.println("+ORIGINAL EXPRESSION+");
      final String original = new PrintVisitor().accept(root, null);
//      System.out.println(original);

      // System.out.println("+ORIGINAL TREE+");

      final StringBuilder b = new StringBuilder();
      new DumpTreeVisitor(b).accept(root, null);
//      System.out.println(b);
      //
      // System.out.println("+AGGREGATED TREE+");
      // // new SimpleXPathAggregatorVisitor().accept(root, Lists.<String> newArrayList());
      // new StepCollapseVisitor().accept(root, null);
      // new DumpTreeVisitor(b).accept(root, null);
      // final String aggregated = b.toString();
      // assertTrue(database.check(test + "-aggegated", aggregated));
      // System.out.println(aggregated);
      // // System.out.println("+AFTER PREEPAT+");
      // // new PrePAATVisitor().accept(root, null);
      // // new DumpTreeVisitor().accept(root, null);
      // // new SimpleXPathAggregatorVisitor().accept(root, Lists.<String> newArrayList());
      // // // new AggragationVisitor().accept(root, null);
      // // System.out.println("+AGGREGATED TREE+");
      // // new DumpTreeVisitor().accept(root, null);
      // // // NEW DUMPTREEVISITOR().ACCEPT(ROOT, NULL);
      // final String newExpr = new PrintVisitor().accept(root, null);
      // assertTrue(database.check(test, newExpr));
      // System.out.println("+NEW EXPRESSION+");
      // System.out.println(newExpr);
      // assertEquals("Equals", original, newExpr);
    }

  }

  @Test
  public void testScottFraser() throws IOException, OXPathException {
    database.setMode(Mode.INTERACTIVE);
    @SuppressWarnings("unchecked")
    final Node root = OXPathParser.getJJTree(FileUtils.toFile(OXPathGrammarTest.class.getResource("scottfraser.oxp"))
        .getAbsolutePath());
    new PrePAATVisitor().accept(root, null);
    // ((SimpleNode) root).dump("");
    final String accept = new PrintVisitor().accept(root, null);
    assertTrue(database.check("scottfraser", accept));
//    System.out.println(accept);
  }

  @Test
  public void testInvalidExpressions() throws IOException, OXPathException {

    database.setMode(Mode.INTERACTIVE);
    @SuppressWarnings("unchecked")
    final List<String> tests = FileUtils.readLines(FileUtils.toFile(OXPathGrammarTest.class
        .getResource("oxpath-expressions-invalid.txt")));

    for (final String test : tests) {
      boolean exception = false;
      try {
        final Node root = OXPathParser.getJJTreeFromString(test);
        new PrePAATVisitor().accept(root, null);
      } catch (final OXPathException e) {
        exception = true;
        logger.debug("Expression '{}' provoked exception '{}'.", test, e);
      }
      assertTrue("Expression '" + test + "' did pass the parser.", exception);
    }
  }
}
