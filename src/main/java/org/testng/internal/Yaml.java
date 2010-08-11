package org.testng.internal;

import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

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
    org.yaml.snakeyaml.Yaml y = new org.yaml.snakeyaml.Yaml();
    Map o = (Map) y.load(new FileInputStream(new File(filePath)));

    return parse(o);
  }

  private static XmlSuite parse(Map o) {
      XmlSuite result = new XmlSuite();
  
      Map<Object, Object> suite = (Map<Object, Object>) o.get("suite");
      result.setName((String) suite.get("name"));
      result.setVerbose((Integer) suite.get("verbose"));
      result.setParallel(((Boolean) suite.get("parallel")).toString());
      result.setThreadCount((Integer) suite.get("thread-count"));
      result.setDataProviderThreadCount((Integer) suite.get("data-provider-thread-count"));

      addToMap(suite, "parameters", result.getParameters());
      addToList(suite, "listeners", result.getListeners());

      {
        Map<String, Object> tests = (Map<String, Object>) suite.get("tests");
        if (tests != null) {
          XmlTest xmlTest = new XmlTest(result);
          xmlTest.setName(tests.get("name").toString());
          List<Map<String, String>> classes = (List<Map<String, String>>) tests.get("classes");
          for (Map<String, String> c : classes) {
            XmlClass xmlClass = new XmlClass(c.get("name"));
            xmlTest.getXmlClasses().add(xmlClass);
          }
        }
      }
//      List<Map<String, String>> listeners = (List<Map<String, String>>) suite.get("listeners");
//      if (listeners != null) {
//        for (Map<String, String> listener : listeners) {
//          for (Map.Entry p : listener.entrySet()) {
//            result.getListeners().add(p.getValue().toString());
//          }
//        }
//      }
  
      //    Map<Object, String> listeners = (Map<Object, String>) suite.get("listeners");
  //    if (parameters != null) {
  //      for (Map.Entry entry : listeners.entrySet()) {
  //        System.out.println("Listener Entry:" + entry.getKey() + ":" + entry.getValue());
  //      }
  //    }
  
      System.out.println(result.toXml());
      return result;
    }

}
