package test.issue1430;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.internal.ClassHelper;

public class TestFileToClass {

  @Test
  public void testFileToClass() {
    Class c1 = this.getClass();
    String p = c1.getResource("TestFileToClass.class").getPath();
    Class c2 = ClassHelper.fileToClass(p);
    Assert.assertNotNull(c2);
    Assert.assertEquals(c1.getName(), c2.getName());
  }
}
