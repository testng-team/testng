package org.testng.reporters.jq;

import org.testng.ISuite;

/**
 * Panels that are accessible from the navigator.
 */
public interface INavigatorPanel extends IPanel {
  String getPanelName(ISuite suite);
  String getNavigatorLink(ISuite suite);
  String getClassName();
  String getPrefix();
}
