/*
 * Copyright (c) 2016, OXPath Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the OXPath team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL OXPath Team BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package uk.ac.ox.cs.diadem.oxpath.output.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import uk.ac.ox.cs.diadem.oxpath.model.OXPathExtractionNode;
import uk.ac.ox.cs.diadem.oxpath.output.ACustomOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.output.IStringSerializable;
import uk.ac.ox.cs.diadem.oxpath.output.OutputHandlerRuntimeException;

/**
 * Used to handle output from OXPath expressions and return the output as XML. This class has the disadvantage of
 * handling all output in memory in order to build the document.
 * 
 * Returns output of query. Builds the document from the nodes received on the output stream
 * 
 * @author Ruslan Fayzrakhmanov
 * 26 Nov 2016
 */
public class XMLOutputHandler
	extends ACustomOutputHandler
	implements IStringSerializable<Document> {
  private static final Logger log = LoggerFactory.getLogger(XMLOutputHandler.class);
  
  public static final String ROOT_ELEMENT_NAME = "results";
  
  private final boolean useCDATASection;
	  public boolean isUseCDATASection() {
		return useCDATASection;
	}

	private final boolean prettyPrint;
	public boolean isPrettyPrint() {
		return prettyPrint;
	}
	
	private final String encoding;
	public String getEncoding() {
		return encoding;
	}
  
  /**
   * Instance field holding the output of the nodes received from the output stream
   */
  private List<OXPathExtractionNode> nodes = new Vector<OXPathExtractionNode>();
  /**
   * Instance field holding the output of the OXPath expression
   */
  private Document outDoc = null;

	  /**
	 * Only one value is allowed per attribute. Pretty-print is disabled. A default character encoding is UTF-8.
	 */
	public XMLOutputHandler() {
	  this.useCDATASection = false;
	  this.prettyPrint = false;
	  this.encoding = StandardCharsets.UTF_8.name();
  }
  
	/**
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param useCDATASection true: use CDATA sections for attribute values.
	 * @param prettyPrint true: use indentation in the serialized XML
	 * A default character encoding is UTF-8.
	 */
	public XMLOutputHandler(final boolean allowMultipleValuesPerAttribute,
		final boolean useCDATASection,
		boolean prettyPrint) {
			this(allowMultipleValuesPerAttribute, useCDATASection
					, prettyPrint, StandardCharsets.UTF_8.name());
	}
	
	/**
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param useCDATASection true: use CDATA sections for attribute values.
	 * @param prettyPrint true: use indentation in the serialized XML
	 * @param encoding
	 * 
	 * The character encoding must be a
     * string acceptable for an XML encoding declaration ([<a href='http://www.w3.org/TR/2004/REC-xml-20040204'>XML 1.0</a>] section
     * 4.3.3 "Character Encoding in Entities"), it is recommended that
     * character encodings registered (as charsets) with the Internet
     * Assigned Numbers Authority [<a href='ftp://ftp.isi.edu/in-notes/iana/assignments/character-sets'>IANA-CHARSETS</a>]
     *  should be referred to using their registered names.
	 */
	public XMLOutputHandler(final boolean allowMultipleValuesPerAttribute,
			final boolean useCDATASection,
			boolean prettyPrint,
			String encoding) {
				super(allowMultipleValuesPerAttribute);
				this.useCDATASection = useCDATASection;
				this.prettyPrint = prettyPrint;
				this.encoding = encoding;
		}
  
	/**
	 * @param wrapperId The ID of the execution.
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param useCDATASection true: use CDATA sections for attribute values.
	 * @param prettyPrint true: use indentation in the serialized XML
	 * A default character encoding is UTF-8.
	 */
	public XMLOutputHandler(String wrapperId,
			boolean allowMultipleValuesPerAttribute,
			boolean useCDATASection,
			boolean prettyPrint) {
	  this(wrapperId, allowMultipleValuesPerAttribute, useCDATASection
			  , prettyPrint, StandardCharsets.UTF_8.name());
  }
	
	/**
	 * @param wrapperId The ID of the execution.
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param useCDATASection true: use CDATA sections for attribute values.
	 * @param prettyPrint true: use indentation in the serialized XML
	 * @param encoding 
	 * 
	 * The character encoding must be a
     * string acceptable for an XML encoding declaration ([<a href='http://www.w3.org/TR/2004/REC-xml-20040204'>XML 1.0</a>] section
     * 4.3.3 "Character Encoding in Entities"), it is recommended that
     * character encodings registered (as charsets) with the Internet
     * Assigned Numbers Authority [<a href='ftp://ftp.isi.edu/in-notes/iana/assignments/character-sets'>IANA-CHARSETS</a>]
     *  should be referred to using their registered names.
	 */
	public XMLOutputHandler(String wrapperId,
		boolean allowMultipleValuesPerAttribute,
		boolean useCDATASection,
		boolean prettyPrint,
		String encoding) {
		super(wrapperId, allowMultipleValuesPerAttribute);
		this.useCDATASection = useCDATASection;
		this.prettyPrint = prettyPrint;
		this.encoding = encoding;
	}

  	@Override
	protected void processNodeImpl(OXPathExtractionNode node) {
  		if (!node.isEndNode()) {
  			assert nodes.size()+1 == node.getId();
  			nodes.add(node);
  	    }
	}

  @Override
  public Document getAccumulativeOutput() {
    try {
    	if (outDoc==null) {
    		outDoc=convertToDOMTree(nodes);
    		nodes.clear(); nodes = null;
    	}
    	return outDoc;
      } catch (ParserConfigurationException | SAXException | IOException e) {
        log.error("Error building the DOM tree. Cause:\n" + e.getMessage());
      }
    return null;
  }

  @Override
  public String asString() {
	  return getStringFromDocument(getAccumulativeOutput(), prettyPrint, encoding);
  }
  
	  /**
	 * @param nodes
	 * @return XML Document encoding the output of the OXPath expression evaluation
	 * @throws ParserConfigurationException in case of XML parse error
	 * @throws SAXException in case of XML parse error
	 * @throws IOException in case of XML parse error
	 */
	private Document convertToDOMTree(List<OXPathExtractionNode> nodes)
		  throws ParserConfigurationException, SAXException, IOException {
    final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    final Document doc = db.newDocument();
    final ArrayList<Element> elements = new ArrayList<Element>();
    // elements are identified by the position rather than by the name
    elements.add(doc.createElement(ROOT_ELEMENT_NAME));
    doc.appendChild(elements.get(0));
    for (final OXPathExtractionNode o : nodes) {
    	boolean isRoot = o.getParentId() == 0;
    	OXPathExtractionNode parentExtrNode = isRoot?null:nodes.get(o.getParentId()-1);
    	assert isRoot || parentExtrNode.getId() == o.getParentId();
    	if (!isRoot && parentExtrNode.isAttribute())
    		throw new OutputHandlerRuntimeException(
					"An attribute node cannot have children", log);
      elements.add(o.getId(), doc.createElement(o.getLabel()));
      elements.get(o.getParentId()).appendChild(elements.get(o.getId()));
      if (o.isAttribute()) {
        final String val = o.getValue();
        if (!useCDATASection) {
          final Text textNode = doc.createTextNode(val);
          elements.get(o.getId()).appendChild(textNode);
        } else {
          final CDATASection section = doc.createCDATASection(val);
          elements.get(o.getId()).appendChild(section);
        }
      }
    }
    return doc;
  }
  
	  /**
	   * <a>https://www.w3.org/TR/DOM-Level-3-LS/load-save.html</a>
	 * @param doc input document
	 * @param prettyPrint true: pretty-print is enabled
	 * @param encoding
	 * @return String representation of document
	 */
	private String getStringFromDocument(final Document doc, final boolean prettyPrint, String encoding) {
	  doc.setXmlVersion("1.1");
	  DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
      LSSerializer lsSerializer = domImplementation.createLSSerializer();
      DOMConfiguration domConf = lsSerializer.getDomConfig();
      domConf.setParameter("format-pretty-print", prettyPrint);
      
      LSOutput output = domImplementation.createLSOutput();
      output.setEncoding(encoding);
      Writer writer = new StringWriter();
      output.setCharacterStream(writer);
      lsSerializer.write(doc, output);
      return writer.toString();
  }

}
