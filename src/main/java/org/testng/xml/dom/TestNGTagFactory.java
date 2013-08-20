package org.testng.xml.dom;

import java.util.Map;

import org.testng.collections.Maps;

public class TestNGTagFactory implements ITagFactory {

  private Map<String, Class<?>> m_map = Maps.newHashMap();

  public TestNGTagFactory() {
//    m_map.put("suite-files", XDom.ChildSuite.class);
  }

  @Override
  public Class<?> getClassForTag(String tag) {
    Class<?> result = m_map.get(tag);
    if (result != null) {
      return result;
    } else {
      String className = "org.testng.xml.Xml" + Reflect.toCapitalizedCamelCase(tag);
      try {
        result = Class.forName(className);
      } catch (ClassNotFoundException e) {
        System.out.println("Couldn't find class " + className);
      }
    }

    return result;
  }

}
