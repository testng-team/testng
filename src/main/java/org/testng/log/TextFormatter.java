package org.testng.log;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;


/**
 * This class implements a simple TextFormatter because the brainded
 * default formatter of java.util.logging outputs everything on two
 * lines and it's ugly as butt.
 *
 * @author Cedric Beust, May 2, 2004
 *
 */
public class TextFormatter extends SimpleFormatter {
  @Override
  public synchronized String format(LogRecord record) {
    StringBuffer result = new StringBuffer();

    result.append(record.getMessage()).append("\n");

    return result.toString();
  }

}
