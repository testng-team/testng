package test.dataprovider.issue2565;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SampleTestUsingConsumer {

  @Test(dataProvider = "dp")
  public void testMethod(Consumer<String> data) {
    data.accept("StarScream");
  }

  @DataProvider(name = "dp")
  public Iterator<Consumer<String>> data() {
    List<Consumer<String>> obj = new ArrayList<>();
    obj.add(Data.INSTANCE::addDatum);
    return obj.iterator();
  }
}
