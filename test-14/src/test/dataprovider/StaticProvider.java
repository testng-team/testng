package test.dataprovider;

public class StaticProvider {

  /**
   * @testng.data-provider name="static"
   */
  public static Object[][] create() {
    return new Object[][] {
        new Object[] { "Cedric" },
    };
  }
}
