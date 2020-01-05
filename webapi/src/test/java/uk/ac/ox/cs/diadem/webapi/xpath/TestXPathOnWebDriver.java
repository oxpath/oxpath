/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.xpath;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import uk.ac.ox.cs.diadem.webapi.BrowserInitialiseTestHelper;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathEvaluator;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathResult;
import uk.ac.ox.cs.diadem.webapi.utils.XPathUtil;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public class TestXPathOnWebDriver {

  static WebBrowser browser;

  @BeforeClass
  public static void oneTimesetUp() throws Exception {
	  browser = BrowserInitialiseTestHelper.init();
  }

  @Before
  public void resetPage() throws Exception {
    browser.navigate("http://diadem.cs.ox.ac.uk/test/re/fast/wwagency/");
  }

  @AfterClass
  public static void shutdown() {
    browser.shutdown();
  }

//  @Test
//  public void testXX() throws InterruptedException, IOException {
//    browser.navigate("http://scholar.google.co.uk/");
//    final DOMElement element = browser.getContentDOMWindow().getDocument().getDocumentElement();
//    final DOMNode node = element.getPreviousSibling().getPreviousSibling();
//    assertEquals(node.getNodeValue(), "364");
//  }

  @Test
  public void test_elements() throws InterruptedException, IOException {
    final DOMNode context = browser.getContentDOMWindow().getDocument();
    final DOMXPathEvaluator xpathEvaluator = context.getXPathEvaluator();

    final DOMXPathResult iResult = xpathEvaluator.evaluate("//a",
        context, xpathEvaluator.createNSResolver(context), DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);
    final List<DOMNode> nodes = Lists.newArrayList();
    for (long i = 0; i < iResult.getSnapshotLength(); i++) {
      nodes.add(iResult.snapshotItem((int) i));
    }
    assertEquals(11, nodes.size());
    final List<DOMNode> list = XPathUtil.getNodes("//a", browser);
    assertEquals(11, list.size());
  }

  @Test
  public void testAttributes() throws InterruptedException, IOException {

    browser.navigate(TestXPathOnWebDriver.class.getResource("testxpath.html").toExternalForm());
    final DOMDocument context = browser.getContentDOMWindow().getDocument();
    final DOMXPathEvaluator xpathEvaluator = context.getXPathEvaluator();
    // final short resultType = (returnsNodes) ? DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE : DOMXPathResult.ANY_TYPE;
    // since we are passing in XPath, no extraction is encountered, so parent and current are the same
    final DOMXPathResult iResult = xpathEvaluator.evaluate("//td/@id", context,
        xpathEvaluator.createNSResolver(context), DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);
    final List<DOMNode> nodes = Lists.newArrayList();
    for (long i = 0; i < iResult.getSnapshotLength(); i++) {
      nodes.add(iResult.snapshotItem((int) i));
    }

    assertEquals(1, nodes.size());
  }

  @Test
  @Ignore
  public void testBulkXPath() throws InterruptedException, IOException {

    //browser.navigate("file:///home/giog/workspace/oxpath.misc/target/classes/uk/ac/ox/cs/diadem/oxpath/misc/large_page.html");
	  
	browser.navigate("https://www.yandex.ru/");
	
    final DOMNode context = browser.getContentDOMWindow().getDocument();
    final DOMXPathEvaluator xpathEvaluator = context.getXPathEvaluator();
    // final short resultType = (returnsNodes) ? DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE : DOMXPathResult.ANY_TYPE;
    // since we are passing in XPath, no extraction is encountered, so parent and current are the same
    // /body/div[position()<=1]:<record>[? ./..:<locality=substring(.,1,100)> ]
    final Stopwatch s = Stopwatch.createStarted();
    final String records = "1000";
    final String[] attreibutes = getAttributes(10);
    final Table<DOMNode, String, List<DOMNode>> table = xpathEvaluator.evaluateBulk(context,
        "descendant-or-self::node()/child::body/child::div[position()<=" + records + "]", Sets.newHashSet(attreibutes));
    s.stop();
//    System.out.println(MessageFormat.format("{0} nodes --> {1} ms", 2 * unfoldTable(table),
//        s.elapsed(TimeUnit.MILLISECONDS)));

  }

  private String[] getAttributes(final int size) {
    final String[] attr = new String[size];
    for (int i = 0; i < attr.length; i++) {
      final String pos = i + 1 + "";
      attr[i] = "./following-sibling::div[" + pos + "]";

    }
    return attr;
  }

  private int unfoldTable(final Table<DOMNode, String, List<DOMNode>> table) {
//    System.out.println(table.toString());
    return table.size();
  }

  @Test
  public void testUnorderedList() throws InterruptedException, IOException {
    browser.navigate(TestXPathOnWebDriver.class.getResource("testxpath.html").toExternalForm());
    final DOMDocument context = browser.getContentDOMWindow().getDocument();
    final DOMXPathEvaluator xpathEvaluator = context.getXPathEvaluator();
    // final short resultType = (returnsNodes) ? DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE : DOMXPathResult.ANY_TYPE;
    // since we are passing in XPath, no extraction is encountered, so parent and current are the same
    final DOMXPathResult iResult = xpathEvaluator.evaluate("descendant-or-self::node()", context,
        xpathEvaluator.createNSResolver(context), DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);
    final List<DOMNode> nodes = Lists.newArrayList();
    for (long i = 0; i < iResult.getSnapshotLength(); i++) {
      nodes.add(iResult.snapshotItem((int) i));
    }

    assertEquals(52, nodes.size());
  }

  @Test
  @Ignore
  public void testOrderedSet() throws InterruptedException, IOException {
    final DOMDocument context = browser.getContentDOMWindow().getDocument();
    final DOMXPathEvaluator xpathEvaluator = context.getXPathEvaluator();
    // final short resultType = (returnsNodes) ? DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE : DOMXPathResult.ANY_TYPE;
    // since we are passing in XPath, no extraction is encountered, so parent and current are the same
    final DOMXPathResult iResult = xpathEvaluator.evaluate("descendant-or-self::node()", context,
        xpathEvaluator.createNSResolver(context), DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);
    final TreeSet<DOMNode> nodes = Sets.newTreeSet(new Comparator<DOMNode>() {

      @Override
      public int compare(final DOMNode o1, final DOMNode o2) {

        return o2.compareDocumentPosition(o1);
      }
    });
    for (long i = 0; i < iResult.getSnapshotLength(); i++) {
      nodes.add(iResult.snapshotItem((int) i));
    }

//    System.out.println(nodes.size());
  }

  @Test
  public void testLinkedSet() throws InterruptedException, IOException {
    final DOMDocument context = browser.getContentDOMWindow().getDocument();
    final DOMXPathEvaluator xpathEvaluator = context.getXPathEvaluator();
    // final short resultType = (returnsNodes) ? DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE : DOMXPathResult.ANY_TYPE;
    // since we are passing in XPath, no extraction is encountered, so parent and current are the same
    final DOMXPathResult iResult = xpathEvaluator.evaluate("descendant-or-self::node()", context,
        xpathEvaluator.createNSResolver(context), DOMXPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
    final LinkedHashSet<DOMNode> nodes = Sets.newLinkedHashSet();
    final long snapshotLength = iResult.getSnapshotLength();
    for (long i = 0; i < snapshotLength; i++) {
      nodes.add(iResult.snapshotItem((int) i));
    }

//    System.out.println(nodes.size());
  }

}
