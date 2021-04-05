package test.factory.nested;

public class FactoryWithAnonymousTestsSample extends BaseFactorySample {

  @Override
  public AbstractBaseSample buildTest() {
    return new AbstractBaseSample() {
      @Override
      protected String someMethod(String param) {
        return param + " world";
      }
    };
  }
}
