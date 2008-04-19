package test.tmp;

import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Arrays;

class AA {
  @Test
  public void f() {}
}

public class A extends AA {

  public static void main(String[] args) {
    XmlSuite suite = new XmlSuite();
    XmlTest test = new XmlTest(suite);
    XmlClass xClass = new XmlClass(AA.class);
    test.getXmlClasses().add(xClass);
//    testng.setXmlSuites(Arrays.asList(new XmlSuite[] {suite}));
    test.getExcludedGroups().add("fast");
    test.setVerbose(5);
    
    System.out.println(suite.toXml());
  }
  
  @Override
  public void f() {}
  
}
