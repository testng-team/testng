package test.dataprovider.issue2565;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SampleTestUsingPredicate {

  @Test(dataProvider = "dp")
  public void testMethod(Predicate<String> data) {
    data.test("IronHide");
  }

  @DataProvider(name = "dp")
  public Iterator<Predicate<String>> data() {
    List<Predicate<String>> obj = new ArrayList<>();
    obj.add(
        text -> {
          Data.INSTANCE.addDatum(text);
          return true;
        });
    return obj.iterator();
  }
}
