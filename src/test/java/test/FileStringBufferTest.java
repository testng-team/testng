package test;

import org.testng.annotations.Test;
import org.testng.reporters.FileStringBuffer;

public class FileStringBufferTest {

  @Test
  public void basic() {
    {
      FileStringBuffer fsb = new FileStringBuffer(5);
      String s = "0123456789";
      String s3 = s + s + s;
  
      fsb.append(s3);
//      Assert.assertEquals(s3, fsb.toString());
    }

    {
      FileStringBuffer fsb = new FileStringBuffer(5);
      String s = "0123456789";
      String s3 = s + s + s;
  
      fsb.append(s);
      fsb.append(s);
      fsb.append(s);
//      Assert.assertEquals(s3, fsb.toString());
    }
  }
}
