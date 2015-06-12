package org.testng.xml;

import org.testng.collections.ListMultiMap;
import org.testng.collections.Maps;

import java.util.List;

/**
 * A holder class for parameters defined just in this tag and all parameters
 * including the ones inherited from enclosing tags.
 */
public class Parameters {

  private ListMultiMap<String, String> m_localParameters = Maps.newListMultiMap();
  private ListMultiMap<String, String> m_allParameters = Maps.newListMultiMap();

  public List<String> getLocalParameter(String name) {
    return m_localParameters.get(name);
  }

  public List<String> getAllValues(String name) {
    return m_allParameters.get(name);
  }

  public List<String> getValue(String name) {
    return m_localParameters.get(name);
  }

  public void addLocalParameter(String name, String value) {
    m_localParameters.put(name, value);
    m_allParameters.put(name, value);
  }

  public void addAllParameter(String name, String value) {
    m_allParameters.put(name, value);
  }
}
