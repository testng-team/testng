package org.testng.xml;

import java.util.Collection;

/**
 * Used by Parser to perform changes on an XML suite after it's been parsed.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public interface IPostProcessor {

  Collection<XmlSuite> process(Collection<XmlSuite> suites);

}
