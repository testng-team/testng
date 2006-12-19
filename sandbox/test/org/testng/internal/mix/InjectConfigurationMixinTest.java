package org.testng.internal.mix;

import org.testng.annotations.Test;


/**
 * This class/interface 
 */
@RequiresConfiguration(ConfigurationMixin.class)
public class InjectConfigurationMixinTest {
  public InjectConfigurationMixinTest() {
    System.out.println("<clinit>");
  }
  
  @TestConfiguration
  private ConfigurationMixin m_mixin= new ConfigurationMixin();
  
  @Test
  public void dummyTest() {
    System.out.println("dummy test");
  }
}
