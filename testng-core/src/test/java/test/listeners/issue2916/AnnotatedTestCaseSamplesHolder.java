package test.listeners.issue2916;

import org.testng.annotations.Listeners;

public class AnnotatedTestCaseSamplesHolder {

  @Listeners(ExecutionListenerHolder.MasterOogway.class)
  public static class ExecutionListenerSampleTestCase extends NormalSampleTestCase {}

  @Listeners(SuiteListenerHolder.MasterOogway.class)
  public static class SuiteListenerSampleTestCase extends NormalSampleTestCase {}

  @Listeners(TestListenerHolder.MasterOogway.class)
  public static class TestListenerSampleTestCase extends ElaborateSampleTestCase {}

  @Listeners(InvokedMethodListenerHolder.MasterOogway.class)
  public static class InvokedMethodListenerSampleTestCase extends NormalSampleTestCase {}

  @Listeners(ConfigurationListenerHolder.MasterOogway.class)
  public static class ConfigurationListenerSampleTestCase extends SimpleConfigTestCase {}

  @Listeners(ClassListenerHolder.MasterOogway.class)
  public static class ClassListenerSampleTestCase extends NormalSampleTestCase {}

  @Listeners(DataProviderListenerHolder.MasterOogway.class)
  public static class DataProviderListenerSampleTestCase extends DataProviderSampleTestCase {}

  @Listeners(DataProviderInterceptorHolder.MasterOogway.class)
  public static class DataProviderInterceptorSampleTestCase extends DataProviderSampleTestCase {}

  @Listeners(ExecutionVisualiserHolder.MasterOogway.class)
  public static class ExecutionVisualiserSampleTestCase extends NormalSampleTestCase {}

  @Listeners(MethodInterceptorHolder.MasterOogway.class)
  public static class MethodInterceptorSampleTestCase extends NormalSampleTestCase {}
}
