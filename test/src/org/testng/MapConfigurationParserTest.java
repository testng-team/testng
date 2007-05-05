package org.testng;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import org.testng.internal.config.MapConfigurationParser;

import java.util.Properties;

/**
 * Tests functionality of {@link org.testng.internal.config.MapConfigurationParser}.
 */
@Test
public class MapConfigurationParserTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_Load_Null_Map() {

    new MapConfigurationParser().load(null);
  }

  @Test(expectedExceptions  = IllegalArgumentException.class)
  public void test_Configure_Null_TestNG() {

    new MapConfigurationParser().configure(null);
  }
  
  public void test_Group_Configuration() {

    TestNG test = new TestNG(false);
    MapConfigurationParser parser = new MapConfigurationParser();

    Properties props = new Properties();
    props.put(ITestNGConfiguration.GROUPS, "snuggly,bunnies,die");
    props.put(ITestNGConfiguration.EXCLUDED_GROUPS, "excluded");
    parser.load(props);

    assert test.m_includedGroups == null;
    assert test.m_excludedGroups == null;

    parser.configure(test);

    assertEquals(test.m_includedGroups, new String[] {"snuggly", "bunnies", "die"});
    assertEquals(test.m_excludedGroups, new String[] { "excluded"});
    assertEquals(test.m_useDefaultListeners, false);
    assertEquals(test.m_testListeners.size(), 0);

    // test with only excluded
    
    props.remove(ITestNGConfiguration.GROUPS);

    parser = new MapConfigurationParser();
    parser.load(props);
    parser.configure(test);

    assertEquals(test.m_excludedGroups, new String[] { "excluded"});
    assert test.m_includedGroups != null; // was set on last configure()
    assertEquals(test.m_includedGroups.length, 0);
  }

  public void test_Class_Configuration() {

    TestNG test = new TestNG(false);
    MapConfigurationParser parser = new MapConfigurationParser();

    Properties props = new Properties();
    props.put(ITestNGConfiguration.TEST_CLASSES, "org.testng.MapConfigurationParserTest, test.Exclude");

    parser.load(props);
    parser.configure(test);

    assert test.m_commandLineTestClasses != null;
    assertEquals(test.m_commandLineTestClasses.length, 2);
    assertEquals(test.m_commandLineTestClasses[0], MapConfigurationParserTest.class);
    assertEquals(test.m_commandLineTestClasses[1], test.Exclude.class);
  }

  @Test(expectedExceptions = TestNGException.class)
  public void test_Invalid_Class_Configuration() {

    TestNG test = new TestNG(false);
    MapConfigurationParser parser = new MapConfigurationParser();

    Properties props = new Properties();
    props.put(ITestNGConfiguration.TEST_CLASSES, "org.testng.MapConfigurationParserTest, test.Unknown");

    parser.load(props);
    parser.configure(test);
  }
}
