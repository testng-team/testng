package test.tmp;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;


public class DataDrivenTest {

  @DataProvider(name = "provider")
  public Object[][] createData() throws FileNotFoundException, IOException {
    Properties p = new Properties();
    List<Object> vResult = new ArrayList<>();
    p.load(new FileInputStream(new File("c:/t/data.properties")));
    for (Enumeration e = p.keys(); e.hasMoreElements(); ) {
      vResult.add(e.nextElement());
    }

    Object[][] result = new Object[vResult.size()][1];
    for (int i = 0; i < result.length; i++) {
      result[i] = new Object[] { vResult.get(i) };
    }

    return result;
  }

  @Test(dataProvider = "provider")
  public void foo(int n) {
    Assert.assertTrue(n > 0);
  }

}
