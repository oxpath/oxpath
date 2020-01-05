package uk.ac.ox.cs.diadem.oxpath.core;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;
import uk.ac.ox.cs.diadem.oxpath.parse.ParseException;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.Node;
import uk.ac.ox.cs.diadem.oxpath.testsupport.StringDatabase.Mode;
import uk.ac.ox.cs.diadem.oxpath.utils.DumpTreeVisitor;
import uk.ac.ox.cs.diadem.oxpath.utils.PrintVisitor;

/**
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 *
 */
@RunWith(value = Parameterized.class)
public class ASTParseTest extends AbstractOXPathTestCase {

  private final String expression;

  public ASTParseTest(final String expression) {
    this.expression = expression;
  }

  @Parameters
  public static Collection<Object[]> data() throws IOException, ParseException {
    final List<String> readLines = Resources.readLines(ASTParseTest.class.getResource("asttest.txt"), Charsets.UTF_8);
    final Collection<Object[]> data = new ArrayList<Object[]>(readLines.size());
    for (final String in : readLines) {

      if (in.startsWith("#") || in.isEmpty()) {
        continue;
      }

      data.add(new Object[] { in });

    }
    return data;
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  // @Ignore
  public void testString() throws IOException, OXPathException {
    database.setKeyPrefix(expression);

    database.setMode(Mode.INTERACTIVE);

    final Node root = OXPathParser.getJJTreeFromString(expression);
    final String original = new PrintVisitor(true, true).accept(root, null);
    final String key = "toString";
    if ((database.getMode() == Mode.RECORD) || (database.getMode() == Mode.INTERACTIVE)) {
      database.check(key, original);
    }
    assertEquals("To string version", database.lookup(key), original);

  }

  @Test
  public void testAst() throws IOException, OXPathException {
    database.setKeyPrefix(expression);

    database.setMode(Mode.INTERACTIVE);

    final Node root = OXPathParser.getJJTreeFromString(expression);

    final StringBuilder b = new StringBuilder();
    b.append(expression);
    b.append("\n");
    new DumpTreeVisitor(b).accept(root, null);
    final String toAst = b.toString();

    final String key = "toAst";
    if ((database.getMode() == Mode.RECORD) || (database.getMode() == Mode.INTERACTIVE)) {
      database.check(key, toAst);
    }
    assertEquals("To ast version", database.lookup(key), toAst);
  }

  @Test
  public void testPrePAA() throws IOException, OXPathException {
    database.setKeyPrefix(expression);

    database.setMode(Mode.INTERACTIVE);

    final Node root = OXPathParser.getJJTreeFromString(expression);
    new PrePAATVisitor().accept(root, null);
    final StringBuilder b = new StringBuilder();
    b.append(expression);
    b.append("\n");
    new DumpTreeVisitor(b).accept(root, null);
    final String toAst = b.toString();
    final String key = "PreePaat";
    if ((database.getMode() == Mode.RECORD) || (database.getMode() == Mode.INTERACTIVE)) {
      database.check(key, toAst);
    }
    assertEquals("To ast version", database.lookup(key), toAst);
  }

}
