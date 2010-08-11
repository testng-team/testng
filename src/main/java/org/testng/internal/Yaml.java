package org.testng.internal;

import javax.xml.parsers.ParserConfigurationException;

import org.testng.IObjectFactory;
import org.testng.xml.Parser;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * YAML support for TestNG.
 * 
 * @author cbeust
 */
public class Yaml {

  private static void addToMap(Map suite, String name, Map target) {
    List<Map<String, String>> parameters = (List<Map<String, String>>) suite.get(name);
    if (parameters != null) {
      for (Map<String, String> parameter : parameters) {
        for (Map.Entry p : parameter.entrySet()) {
          target.put(p.getKey(), p.getValue().toString());
        }
      }
    }
  }

  private static void addToList(Map suite, String name, List target) {
    List<Map<String, String>> parameters = (List<Map<String, String>>) suite.get(name);
    if (parameters != null) {
      for (Map<String, String> parameter : parameters) {
        for (Map.Entry p : parameter.entrySet()) {
          target.add(p.getValue().toString());
        }
      }
    }
  }

  public static XmlSuite parse(String filePath) throws FileNotFoundException {
    Constructor constructor = new Constructor(XmlSuite.class);
    {
      TypeDescription suiteDescription = new TypeDescription(XmlSuite.class);
      suiteDescription.putListPropertyType("packages", XmlPackage.class);
      suiteDescription.putListPropertyType("listeners", String.class);
      suiteDescription.putListPropertyType("tests", XmlTest.class);
      suiteDescription.putListPropertyType("method-selectors", XmlMethodSelector.class);
      constructor.addTypeDescription(suiteDescription);
    }

    {
      TypeDescription testDescription = new TypeDescription(XmlTest.class);
      testDescription.putListPropertyType("classes", XmlClass.class);
      testDescription.putMapPropertyType("metaGroups", String.class, List.class);
      constructor.addTypeDescription(testDescription);
    }

    Loader loader = new Loader(constructor);

    org.yaml.snakeyaml.Yaml y = new org.yaml.snakeyaml.Yaml(loader);
    XmlSuite result = (XmlSuite) y.load(new FileInputStream(new File(filePath)));
    System.out.println(result.toXml());

    // Adjust XmlTest parents
    for (XmlTest t : result.getTests()) {
      t.setSuite(result);
    }
    return result;

//    Map o = (Map) y.load(new FileInputStream(new File(filePath)));
//
//    return parse(o);
  }

  private static void setField(Object xml, Map<?, ?> map, String key, String methodName,
      Class<?> parameter) {
    Object o = map.get(key);
    if (o != null) {
      Method m;
      try {
        m = xml.getClass().getMethod(methodName, parameter);
        m.invoke(xml, o);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static XmlSuite parse(Map o) {
      XmlSuite result = new XmlSuite();
  
      //
      // <suite>
      //
      Map<Object, Object> suite = (Map<Object, Object>) o.get("suite");
      setField(result, suite, "name", "setName", String.class);
      setField(result, suite, "verbose", "setVerbose", Integer.class);
      result.setParallel(((Boolean) suite.get("parallel")).toString());
      result.setThreadCount((Integer) suite.get("thread-count"));
      result.setDataProviderThreadCount((Integer) suite.get("data-provider-thread-count"));
      result.setJUnit((Boolean) suite.get("junit"));
      setField(result, suite, "configfailurepolicy", "setConfigFailurePolicy", String.class);
      result.setThreadCount((Integer) suite.get("thread-count"));
      result.setTimeOut((String) suite.get("time-out"));
      setField(result, suite, "skipfailedinvocationcounts", "setSkipFailedInvocationCounts",
          Boolean.class);
      result.setObjectFactory((IObjectFactory) suite.get("object-factory"));


      //
      // <method-selectors>
      //
      {
        List<Map<String, Object>> selectors =
            (List<Map<String, Object>>) suite.get("method-selectors");
        if (selectors != null) {
          for (Map<String, Object> s : selectors) {
            String cls = (String) s.get("selector-class");
            if (cls != null) {
              org.testng.xml.XmlMethodSelector ms = new org.testng.xml.XmlMethodSelector();
              ms.setPriority((Integer) s.get("priority"));
              ms.setName(cls);
              result.getMethodSelectors().add(ms);
            }
            String script = (String) s.get("script");
            if (script != null) {
              org.testng.xml.XmlMethodSelector ms = new org.testng.xml.XmlMethodSelector();
              ms.setExpression(script);
              ms.setLanguage((String) s.get("language"));
              result.getMethodSelectors().add(ms);
            }
          }
        }
      }

      //
      // <listeners>
      //
      addToList(suite, "listeners", result.getListeners());

      //
      // <packages>
      //
      {
        List<Map<String, String>> packages = (List<Map<String, String>>) suite.get("packages");
        for (Map<String, String> p : packages) {
          XmlPackage xp = new XmlPackage(p.get("name"));
          result.getXmlPackages().add(xp);
        }
      }

      //
      // <parameters>
      //

      addToMap(suite, "parameters:", result.getParameters());


      //
      // <test>
      //
      {
        List<Map<String, Object>> tests = (List<Map<String, Object>>) suite.get("tests");
        for (Map<String, Object> test : tests) {
          XmlTest xmlTest = new XmlTest(result);
          xmlTest.setName(test.get("name").toString());
          setField(xmlTest, test, "junit", "setJUnit", Boolean.class);
          setField(xmlTest, test, "verbose", "setVerbose", Integer.class);
          xmlTest.setParallel((String) test.get("parallel"));
          setField(xmlTest, test, "thread-count", "setThreadCount", Integer.class);
          setField(xmlTest, test, "time-out", "setTimeOut", Long.class);
          setField(xmlTest, test, "skipfailedinvocationcounts", "setSkipFailedInvocationCounts",
              Boolean.class);
          setField(xmlTest, test, "preserve-order", "setPreserveOrder", Boolean.class);

          //
          // <classes>
          //
          List<Map<String, String>> classes = (List<Map<String, String>>) test.get("classes");
          for (Map<String, String> c : classes) {
            XmlClass xmlClass = new XmlClass(c.get("name"));
            xmlTest.getXmlClasses().add(xmlClass);
          }

          //
          // <parameter>
          //

          //
          // <groups>
          //

          //
          // <packages>
          //
        }
      }

      //
      // <method-selectors>
      //

      //
      // <suite-files>
      //

      System.out.println(result.toXml());
      return result;
    }

  private static void maybeAdd(StringBuilder sb, String key, Object value) {
    maybeAdd(sb, "", key, value);
  }

  private static void maybeAdd(StringBuilder sb, String sp, String key, Object value) {
    if (value != null) sb.append(sp).append(key).append(": ").append(value.toString())
        .append("\n");
  }

  public static StringBuilder toYaml(XmlSuite suite) {
    StringBuilder result = new StringBuilder();

    maybeAdd(result, "name", suite.getName());
    maybeAdd(result, "junit", suite.isJUnit());
    maybeAdd(result, "verbose", suite.getVerbose());
    maybeAdd(result, "threadCount", suite.getThreadCount());
    maybeAdd(result, "timeOut", suite.getTimeOut());
    maybeAdd(result, "skipFailedInvocationCounts", suite.skipFailedInvocationCounts());

    toYaml(result, "parameters:", suite.getParameters());
    if (suite.getPackages().size() > 0) {
      result.append("packages:\n");
      toYaml(result, suite.getPackages());
    }
    toYaml(result, "listeners", suite.getListeners());
    if (suite.getTests().size() > 0) {
      result.append("  tests:\n");
      for (XmlTest t : suite.getTests()) {
        toYaml(result, "    ", t);
      }
    }
//    (test|method-selectors|suite-files)* >
    return result;
  }

  private static void toYaml(StringBuilder result, String sp, XmlTest t) {
    String sp2 = sp + "  ";
    result.append(sp).append("- name: ").append(t.getName()).append("\n");
    maybeAdd(result, sp2, "junit", t.isJUnit());
    maybeAdd(result, sp2, "verbose", t.getVerbose());
    maybeAdd(result, sp2, "parallel", t.getParallel());
    maybeAdd(result, sp2, "timeOut", t.getTimeOut());
    maybeAdd(result, sp2, "skipFailedInvocationCounts", t.skipFailedInvocationCounts());
    maybeAdd(result, sp2, "preserveOrder", t.getPreserveOrder());

    if (t.getXmlClasses().size() > 0) {
      result.append(sp2).append("classes:\n");
      for (XmlClass xc : t.getXmlClasses())  {
        toYaml(result, sp2 + "  ", xc);
      }
    }

    if (t.getXmlPackages().size() > 0) {
      result.append(sp2).append("xmlPackages:\n");
      for (XmlPackage xp : t.getXmlPackages())  {
        toYaml(result, sp2 + "  ", xp);
      }
    }

    if (t.getMetaGroups().size() > 0) {
      result.append(sp2).append("groups:\n");
      for (String group : t.getMetaGroups().keySet()) {
        result.append(sp2 + "  -").append(group).append("\n");
      }
    }

    //    <!ELEMENT test (method-selectors?,) >

  }

  private static void toYaml(StringBuilder result, String sp2, XmlClass xc) {
    result.append(sp2).append("- name: ").append(xc.getName()).append("\n");
    if (xc.getIncludedMethods().size() > 0) {
      result.append(sp2 + "  includedMethods:\n");
      for (XmlInclude xi : xc.getIncludedMethods()) {
        toYaml(result, sp2 + "    ", xi);
      }
    }

    if (xc.getExcludedMethods().size() > 0) {
      result.append(sp2 + "  excludedMethods:\n");
      toYaml(result, sp2 + "    ", xc.getExcludedMethods());
    }
  }

  private static void toYaml(StringBuilder result, String sp2, XmlInclude xi) {
    result.append(sp2 + "- " + xi.getName()).append("\n");
  }

  private static void toYaml(StringBuilder result, String sp, List<String> strings) {
    for (String l : strings) {
      result.append(sp).append("- ").append(l).append("\n");
    }
  }

  private static final String SP = "  ";

  private static void toYaml(StringBuilder sb, List<XmlPackage> packages) {
    for (XmlPackage p : packages) {
      toYaml(sb, "  ", p);
    }
  }

  private static void toYaml(StringBuilder sb, String sp, XmlPackage p) {
    sb.append(sp).append("name: ").append(p.getName()).append("\n");

    generateIncludeExclude(sb, sp, "includes", p.getInclude());
    generateIncludeExclude(sb, sp, "excludes", p.getExclude());
  }

  private static void generateIncludeExclude(StringBuilder sb, String sp,
      String key, List<String> includes) {
    if (includes.size() > 0) {
      sb.append(sp).append("  ").append(key).append("\n");
      for (String inc : includes) {
        sb.append(sp).append("    ").append(inc);
      }
    }
  }

  private static void toYaml(StringBuilder sb, String key, Map<String, String> parameters) {
    sb.append(key).append("\n");
    for (Map.Entry<String, String> p : parameters.entrySet()) {
      sb.append(SP).append(p.getKey()).append(": ").append(p.getValue()).append("\n");
    }
  }

  public static void main(String[] args)
      throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
    Collection<XmlSuite> s =
        new Parser("/Users/cbeust/java/testng/src/test/resources/testng.xml").parse();
    System.out.println(Yaml.toYaml(s.iterator().next()));
  }
}
