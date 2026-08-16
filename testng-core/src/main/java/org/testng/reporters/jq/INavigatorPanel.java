package org.testng.reporters.jq;

import org.jspecify.annotations.Nullable;
import org.testng.ISuite;

/** Panels that are accessible from the navigator. */
public interface INavigatorPanel extends IPanel {
  String getPanelName(ISuite suite);

  String getNavigatorLink(ISuite suite);

  /** @return the CSS class to style the navigator link with, or {@code null} for none. */
  @Nullable
  String getClassName();

  String getPrefix();
}
