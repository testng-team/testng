package test.dataprovider.issue2565;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SampleTestUsingFunction {

  @Test(dataProvider = "dp")
  public void testMethod(Function<String, String> data) {
    data.apply("Bumble_Bee");
  }

  @DataProvider(name = "dp")
  public Iterator<Function<String, String>> data() {
    List<Function<String, String>> obj = new ArrayList<>();
    obj.add(
        text -> {
          Data.INSTANCE.addDatum(text);
          return text.toUpperCase(Locale.ROOT);
        });
    return obj.iterator();
  }
}
