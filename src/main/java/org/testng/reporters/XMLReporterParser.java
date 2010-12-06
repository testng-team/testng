package org.testng.reporters;

/**
 * Parses a testng-result.xml file and notifies a listener.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class XMLReporterParser {

  private XMLReporterListener m_listener;

  public XMLReporterParser(XMLReporterListener listener) {
    m_listener = listener;
    parse();
  }

  private void parse() {
  }
}
