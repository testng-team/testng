package org.testng.xml.dom;

public class TestNGTagFactory implements ITagFactory {

  @Override
  public Class<?> getClassForTag(String tag) {
    Class<?> result = null;
    String className = "org.testng.xml.Xml"
        + XDom.toCapitalizedCamelCase(tag);
    try {
      result = Class.forName(className);
    } catch (ClassNotFoundException e) {
//      e.printStackTrace();
    }

    return result;
  }

}
