package test.configurationfailurepolicy;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ClassWithFailedBeforeMethodAndMultipleInvocations {

  @BeforeMethod
  public void setupShouldFail() {
    throw new RuntimeException("Failing in setUp");
  }

  @DataProvider( name = "data.provider" )
  public Object[][] dataProvider() {
    return new Object[][] {
        new Object[] { "data1" },
        new Object[] { "data2" }
    };
  }
  
  @Test( dataProvider = "data.provider" )
  public void test1( String s ) {

  }
  
  @Test( invocationCount = 2 )
  public void test2() {
    
  }
}
