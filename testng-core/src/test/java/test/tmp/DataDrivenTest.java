package test.tmp;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataDrivenTest {

  @DataProvider(name = "provider")
  public Object[][] createData() throws IOException {
    Properties p = new Properties();
    List<Object> vResult = new ArrayList<>();
    p.load(new FileInputStream("c:/t/data.properties"));
    for (Enumeration<Object> e = p.keys(); e.hasMoreElements(); ) {
      vResult.add(e.nextElement());
    }

    Object[][] result = new Object[vResult.size()][1];
    for (int i = 0; i < result.length; i++) {
      result[i] = new Object[] {vResult.get(i)};
    }

    return result;
  }

  @Test(dataProvider = "provider")
  public void foo(int n) {
    Assert.assertTrue(n > 0);
  }
}
