package test.mannotation;

/**
 * @testng.test enabled = "true" groups = "group1 group2"
 *    alwaysRun = "true" parameters = "param1 param2"
 *    dependsOnGroups = "dg1 dg2" dependsOnMethods = "dm1 dm2"
 *    timeOut = "42" invocationCount = "43" successPercentage = "44"
 *    dataProvider = "dp" description = "Class level description"
 */
public class MTest1 {
  
  /**
   * @testng.test enabled = "true" groups = "group5 group6"
   *    alwaysRun = "true" parameters = "param5 param6"
   *    dependsOnGroups = "dg5 dg6" dependsOnMethods = "dm5 dm6"
   *    timeOut = "242" invocationCount = "243" successPercentage = "62"
   *    dataProvider = "dp3" description = "Constructor description"
   */
  public MTest1() {}

  /**
   * @testng.test enabled = "true" groups = "group3 group4"
   *    alwaysRun = "true" parameters = "param3 param4"
   *    dependsOnGroups = "dg3 dg4" dependsOnMethods = "dm3 dm4"
   *    timeOut = "142" invocationCount = "143" successPercentage = "61"
   *    dataProvider = "dp2" threadPoolSize = "3"
   *    description = "Method description"
   */
  public void f() {}
  
  /**
   * @testng.configuration
   *   beforeSuite = "true" beforeTestMethod = "true"
   *   beforeTest = "true" beforeTestClass = "true"
   *   beforeGroups = "b1 b2"
   */
  public void before() {}
  
  /**
   * @testng.configuration
   *   afterSuite = "true" afterTestMethod = "true"
   *   afterTest = "true" afterTestClass = "true"
   *   afterGroups = "a1 a2"
   */
  public void after() {}
  
  /**
   * @testng.configuration parameters = "oparam1 oparam2"
   * enabled = "false" groups = "ogroup1 ogroup2"
   * dependsOnGroups = "odg1 odg2"
   * dependsOnMethods = "odm1 odm2" alwaysRun = "true"
   * inheritGroups = "false" description = "beforeSuite description"
   * 
   * @testng.data-provider name="dp4"
   * 
   * @testng.expected-exceptions value = "test.mannotation.MTest1 test.mannotation.MTest2"
   */
  public void otherConfigurations() {}
  
  /**
   * @testng.factory parameters = "pf1 pf2"
   */
  public void factory() {}
  
  /**
   * @testng.parameters value = "pp1 pp2 pp3"
   */
  public void parameters() {}
}
