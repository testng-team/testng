package test.skipex;

import java.util.Calendar;

import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 * This class/interface 
 */
public class ConfigurationSkippedExceptionTest {
  @BeforeMethod
  public void configurationLevelSkipException() {
    throw new SkipException("some skip message");
  }
  
  @Test
  public void dummyTest() {
  }
}
