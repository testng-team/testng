package org.testng.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Which grammar validates a suite file is decided from its prologue, before a parser exists, so a
 * misreading here silently sends a document to the wrong schema -- or to none.
 *
 * <p>The case that motivates reading the prologue rather than searching for the text is the
 * commented-out doctype below: a suite carrying one would otherwise be validated against the DTD it
 * deliberately does not declare.
 */
public class XmlPrologueTest {

  @DataProvider
  public static Object[][] prologues() {
    return new Object[][] {
      {
        "an external doctype", "<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.1.dtd\">", true
      },
      {"an internal subset", "<!DOCTYPE suite [ <!ENTITY name \"value\"> ]>", true},
      {"no doctype at all", "", false},
      {"a comment, then a doctype", "<!-- a note --><!DOCTYPE suite SYSTEM \"x.dtd\">", true},
      {"a commented-out doctype", "<!-- <!DOCTYPE suite SYSTEM \"x.dtd\"> -->", false},
      {"a comment mentioning the word", "<!-- add a DOCTYPE here -->", false},
      {"a processing instruction", "<?xml-stylesheet href=\"s.xsl\"?>", false},
      {"a processing instruction, then a doctype", "<?pi ?><!DOCTYPE suite SYSTEM \"x\">", true},
      {"a comment spanning lines", "<!--\n<!DOCTYPE suite>\n-->", false},
    };
  }

  @Test(dataProvider = "prologues")
  public void theDoctypeIsFoundOnlyWhereItIsDeclared(
      String description, String prologue, boolean expected) {
    String document =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + prologue + "\n<suite name=\"s\"/>\n";

    assertThat(XmlPrologue.declaresDoctype(document.getBytes(StandardCharsets.UTF_8)))
        .as("a suite file whose prologue holds %s", description)
        .isEqualTo(expected);
  }

  /**
   * A schema declaration lives on the root element, so it is past the prologue and must not be
   * mistaken for a doctype.
   */
  @Test
  public void aSchemaDeclarationIsNotADoctype() {
    String document =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<suite xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "       xsi:noNamespaceSchemaLocation=\"https://testng.org/testng-1.1.xsd\"\n"
            + "       name=\"s\"/>\n";

    assertThat(XmlPrologue.declaresDoctype(document.getBytes(StandardCharsets.UTF_8))).isFalse();
  }

  /** Truncated input is the parser's problem to report, not this scanner's to throw on. */
  @Test
  public void anUnterminatedPrologueIsNotADoctype() {
    assertThat(XmlPrologue.declaresDoctype("<!-- unterminated".getBytes(StandardCharsets.UTF_8)))
        .isFalse();
    assertThat(XmlPrologue.declaresDoctype(new byte[0])).isFalse();
  }

  /**
   * A suite file is not always UTF-8, and the answer must not depend on the encoding. Both UTF-16
   * byte orders are covered because they differ in exactly the way a byte-level reading of the
   * document would trip over.
   */
  @Test
  public void aDoctypeIsFoundWhateverTheEncoding() {
    String utf16 =
        "<?xml version=\"1.0\" encoding=\"UTF-16\"?><!DOCTYPE suite SYSTEM \"x\">"
            + "<suite name=\"s\"/>";
    // No encoding declared, so the same bytes stay readable with a byte order mark in front.
    String utf8 = "<?xml version=\"1.0\"?><!DOCTYPE suite SYSTEM \"x\"><suite name=\"s\"/>";

    assertThat(XmlPrologue.declaresDoctype(utf16.getBytes(StandardCharsets.UTF_16BE))).isTrue();
    assertThat(XmlPrologue.declaresDoctype(utf16.getBytes(StandardCharsets.UTF_16LE))).isTrue();
    assertThat(XmlPrologue.declaresDoctype(("﻿" + utf8).getBytes(StandardCharsets.UTF_8)))
        .as("a byte order mark must not hide the declaration behind it")
        .isTrue();
  }

  /**
   * The external subset must not be fetched to answer the question: only the declaration matters,
   * the parse that follows has a resolver for it, and reaching out to whatever a suite file names
   * would be both slow and unsafe. The address is one nothing can serve, so a fetch would hang or
   * throw rather than pass.
   */
  @Test
  public void theExternalSubsetIsNotFetched() {
    String document =
        "<?xml version=\"1.0\"?>\n"
            + "<!DOCTYPE suite SYSTEM \"http://127.0.0.1:1/unreachable.dtd\">\n"
            + "<suite name=\"s\"/>\n";

    assertThat(XmlPrologue.declaresDoctype(document.getBytes(StandardCharsets.UTF_8))).isTrue();
  }
}
