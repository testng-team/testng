package test.factory.sample;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * Test that setUp methods are correctly interleaved even when we use similar instances of a same
 * test class.
 */
public class Factory2Sample {

  private static List<String> methods = new ArrayList<>();

  @BeforeSuite
  public void init() {
    methods = new ArrayList<>();
  }

  @BeforeMethod
  public void setUp() {
    methods.add("setUp");
  }

  @AfterMethod
  public void tearDown() {
    methods.add("tearDown");
  }

  private static final List<String> EXPECTED_METHODS =
      Arrays.asList(
          "setUp",
          "testInputImages",
          "tearDown",
          "setUp",
          "testInputImages",
          "tearDown",
          "setUp",
          "testImages",
          "tearDown",
          "setUp",
          "testImages",
          "tearDown");

  @AfterSuite
  public void afterSuite() {
    assertEquals(methods, EXPECTED_METHODS);
  }

  @Test
  public void testInputImages() {
    methods.add("testInputImages");
  }

  @Test(dependsOnMethods = {"testInputImages"})
  public void testImages() {
    methods.add("testImages");
  }
}
