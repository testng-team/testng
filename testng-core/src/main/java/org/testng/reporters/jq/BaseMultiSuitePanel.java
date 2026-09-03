package org.testng.reporters.jq;

import org.jspecify.annotations.Nullable;
import org.testng.ISuite;
import org.testng.reporters.XMLStringBuffer;

public abstract class BaseMultiSuitePanel extends BasePanel implements INavigatorPanel {

  abstract @Nullable String getHeader(ISuite suite);

  /**
   * Writes the content of this panel for one suite into {@code content}, and may leave a tag open
   * on its stack: nothing downstream closes one.
   *
   * @param content a buffer of its own, already started at the indentation of the report
   */
  abstract void writeContent(ISuite suite, XMLStringBuffer content);

  /**
   * @return the content of this panel for one suite, as a String.
   *     <p>The report itself does not go through here: this materialises the whole panel, which is
   *     the {@link XMLStringBuffer#toXML()} of a buffer backed by a temporary file, and what
   *     GITHUB-2334 ran out of heap doing. {@link #generate} streams the buffer instead. The seven
   *     panels declared this method public before they had one to stream, so it stays.
   */
  public String getContent(ISuite suite, XMLStringBuffer xsb) {
    return contentOf(suite, xsb).toXML();
  }

  /** Builds this panel's own buffer, at the indentation the report has reached. */
  private XMLStringBuffer contentOf(ISuite suite, XMLStringBuffer main) {
    XMLStringBuffer content = new XMLStringBuffer(main.getCurrentIndent());
    writeContent(suite, content);
    return content;
  }

  public BaseMultiSuitePanel(Model model) {
    super(model);
  }

  @Override
  public void generate(XMLStringBuffer xsb) {
    for (ISuite s : getSuites()) {
      xsb.push(D, C, "panel", "panel-name", getPanelName(s));
      xsb.push(D, C, "main-panel-header rounded-window-top");
      xsb.addOptional(S, getHeader(s), C, "header-content");
      xsb.pop(D);

      xsb.push(D, C, "main-panel-content rounded-window-bottom");
      xsb.addBuffer(contentOf(s, xsb));
      xsb.pop(D);

      xsb.pop(D);
    }
  }

  @Override
  public @Nullable String getClassName() {
    return null;
  }

  @Override
  public String getPanelName(ISuite suite) {
    return getPrefix() + suiteToTag(suite);
  }
}
