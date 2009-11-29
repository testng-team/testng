package test.tmp;

import org.testng.ITest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

public class A implements ITest {
  
  public static void main(String[] args) {
    String s = new A().toString();
    System.out.println(Pattern.matches(".*@[0-9a-f]+$", new A().toString()));
  }

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
        new Object[] { "10.10.10" },
        new Object[] { "10.10" },
    };
  }

//  @Test(dataProvider = "dp")
//  public void verifyIPAddress(String ip) {
//    System.out.println("IP:" + ip);
//  }

  @BeforeClass
  public void beforeClassA() {
  }

  @AfterClass
  public void afterClassA() {
  }

  @Test
  public void g1() {
  }


  public String getTestName() {
    return "This is test A";
  }

//  @Test(groups = "mytest", dependsOnMethods = "g")
//  public void f() {
//  }
//
//
//  @AfterMethod
//  public void after() {
//  }

}
