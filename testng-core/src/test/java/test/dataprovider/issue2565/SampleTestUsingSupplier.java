package test.dataprovider.issue2565;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SampleTestUsingSupplier {

  @Test(dataProvider = "dp")
  public void testMethod(Supplier<String> data) {
    Data.INSTANCE.addDatum(data.get());
  }

  @DataProvider(name = "dp")
  public Iterator<Supplier<String>> data() {
    List<Supplier<String>> obj = new ArrayList<>();
    obj.add(() -> "Optimus_Prime");
    obj.add(() -> "Megatron");
    return obj.iterator();
  }
}
