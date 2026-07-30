package org.testng.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.xml.XsdValidationTest.TESTNG_XSD;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.annotations.Test;
import org.testng.xml.internal.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Keeps {@code testng-1.1.dtd} and {@code testng-1.1.xsd} from drifting apart.
 *
 * <p>They already drifted once, in a way no test could see: the writer advertised {@code
 * testng-1.0.dtd} while the reader always resolved {@code testng-1.1.dtd}. Two files describing the
 * same language, edited independently, will do it again -- an attribute added to one and forgotten
 * in the other is a single-line change nobody notices in review.
 *
 * <p>What is compared is the <em>declarations</em>: which elements exist, which attributes they
 * carry, whether each is required, its default value and its enumerated values. Content models are
 * deliberately left out -- they are expressed too differently in the two languages to compare
 * mechanically without inventing a parser for both. They are covered from the other end instead:
 * {@link XsdValidationTest} validates the whole corpus and every {@code toXml()} output under both
 * schemas, and asserts that the DTD-invalid fixture is rejected by the XSD as well.
 */
public class SchemaConsistencyTest {

  @Test
  public void bothSchemasDeclareTheSameElements() throws Exception {
    assertThat(declarationsFromXsd().keySet())
        .as("the elements declared by %s and by %s", TESTNG_XSD, Parser.TESTNG_DTD)
        .isEqualTo(declarationsFromDtd().keySet());
  }

  @Test
  public void bothSchemasDeclareTheSameAttributes() throws Exception {
    assertThat(declarationsFromXsd())
        .as(
            "the attributes declared by %s and by %s, with their requiredness, default value and"
                + " enumerated values",
            TESTNG_XSD, Parser.TESTNG_DTD)
        .isEqualTo(declarationsFromDtd());
  }

  /**
   * An attribute rendered so that the two schema languages produce the same string, and so that a
   * failure reads as a diff. For instance {@code optional default="skip" (continue|skip)}.
   *
   * <p>Enumerated values are sorted: which values exist is what has to match, the order they are
   * written in is not something a reader can observe.
   */
  private static String render(boolean required, String defaultValue, List<String> enumeration) {
    StringBuilder rendered = new StringBuilder(required ? "required" : "optional");
    if (defaultValue != null) {
      rendered.append(" default=\"").append(defaultValue).append('"');
    }
    rendered.append(' ');
    if (enumeration.isEmpty()) {
      rendered.append("CDATA");
    } else {
      rendered.append('(').append(String.join("|", new TreeSet<>(enumeration))).append(')');
    }
    return rendered.toString();
  }

  // ---------------------------------------------------------------- DTD

  /** {@code <!ELEMENT name model>}; no content model in this DTD contains a {@code >}. */
  private static final Pattern ELEMENT = Pattern.compile("<!ELEMENT\\s+([\\w-]+)[^>]*>");

  private static final Pattern ATTLIST = Pattern.compile("<!ATTLIST\\s+([\\w-]+)([^>]*)>");

  /** One {@code name AttType DefaultDecl} triple of an {@code <!ATTLIST>} body. */
  private static final Pattern ATTRIBUTE =
      Pattern.compile(
          "([\\w-]+)\\s+(CDATA|\\([^)]*\\))\\s+(#REQUIRED|#IMPLIED|\"[^\"]*\")", Pattern.DOTALL);

  private static final Pattern COMMENT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);

  private static Map<String, Map<String, String>> declarationsFromDtd() throws IOException {
    String dtd = COMMENT.matcher(read(Parser.TESTNG_DTD)).replaceAll("");

    Map<String, Map<String, String>> declarations = new TreeMap<>();
    Matcher elements = ELEMENT.matcher(dtd);
    while (elements.find()) {
      declarations.put(elements.group(1), new TreeMap<>());
    }

    Matcher attributeLists = ATTLIST.matcher(dtd);
    while (attributeLists.find()) {
      Map<String, String> attributes =
          Objects.requireNonNull(
              declarations.get(attributeLists.group(1)),
              "<!ATTLIST " + attributeLists.group(1) + "> has no matching <!ELEMENT>");
      Matcher attribute = ATTRIBUTE.matcher(attributeLists.group(2));
      while (attribute.find()) {
        String declaredDefault = attribute.group(3);
        attributes.put(
            attribute.group(1),
            render(
                "#REQUIRED".equals(declaredDefault),
                declaredDefault.startsWith("\"") ? stripDelimiters(declaredDefault) : null,
                enumerationOf(attribute.group(2))));
      }
    }
    return declarations;
  }

  /** Drops the surrounding quotes of a default value, or the parentheses of an enumeration. */
  private static String stripDelimiters(String delimited) {
    return delimited.substring(1, delimited.length() - 1);
  }

  /** {@code CDATA} has no enumeration; {@code (a | b)} has one. */
  private static List<String> enumerationOf(String attributeType) {
    if (!attributeType.startsWith("(")) {
      return new ArrayList<>();
    }
    return Arrays.stream(stripDelimiters(attributeType).split("\\|"))
        .map(String::trim)
        .collect(Collectors.toList());
  }

  // ---------------------------------------------------------------- XSD

  private static Map<String, Map<String, String>> declarationsFromXsd() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    Document schema;
    try (InputStream stream = open(TESTNG_XSD)) {
      schema = factory.newDocumentBuilder().parse(stream);
    }

    Map<String, List<String>> simpleTypes = new LinkedHashMap<>();
    for (Element simpleType : childrenNamed(schema.getDocumentElement(), "simpleType")) {
      simpleTypes.put(simpleType.getAttribute("name"), enumerationValuesOf(simpleType));
    }

    Map<String, Map<String, String>> declarations = new TreeMap<>();
    for (Element element : childrenNamed(schema.getDocumentElement(), "element")) {
      Map<String, String> attributes = new TreeMap<>();
      for (Element complexType : childrenNamed(element, "complexType")) {
        for (Element attribute : childrenNamed(complexType, "attribute")) {
          attributes.put(
              attribute.getAttribute("name"),
              render(
                  "required".equals(attribute.getAttribute("use")),
                  attribute.hasAttribute("default") ? attribute.getAttribute("default") : null,
                  enumerationFor(attribute.getAttribute("type"), simpleTypes)));
        }
      }
      declarations.put(element.getAttribute("name"), attributes);
    }
    return declarations;
  }

  /**
   * The schema has no imports, so a type name is either a built-in or one of its own named simple
   * types; comparing the local part is enough to tell them apart.
   */
  private static List<String> enumerationFor(String type, Map<String, List<String>> simpleTypes) {
    String localName = type.substring(type.indexOf(':') + 1);
    return simpleTypes.getOrDefault(localName, new ArrayList<>());
  }

  private static List<String> enumerationValuesOf(Element simpleType) {
    List<String> values = new ArrayList<>();
    NodeList enumerations = simpleType.getElementsByTagNameNS("*", "enumeration");
    for (int i = 0; i < enumerations.getLength(); i++) {
      values.add(((Element) enumerations.item(i)).getAttribute("value"));
    }
    return values;
  }

  private static List<Element> childrenNamed(Element parent, String localName) {
    List<Element> children = new ArrayList<>();
    NodeList nodes = parent.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE && localName.equals(node.getLocalName())) {
        children.add((Element) node);
      }
    }
    return children;
  }

  // ---------------------------------------------------------------- resources

  private static String read(String resource) throws IOException {
    try (InputStream stream = open(resource)) {
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static InputStream open(String resource) {
    return Objects.requireNonNull(
        SchemaConsistencyTest.class.getClassLoader().getResourceAsStream(resource),
        resource + " is not on the test classpath");
  }
}
