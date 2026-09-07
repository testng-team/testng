package test.issue1430;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;
import org.testng.internal.ClassHelper;

public class TestFileToClass {

  @Test
  public void testFileToClass() {
    Class c1 = this.getClass();
    String p = c1.getResource("TestFileToClass.class").getPath();
    Class c2 = ClassHelper.fileToClass(p);
    assertThat(c2).isNotNull();
    assertThat(c1.getName()).isEqualTo(c2.getName());
  }
}
