package test.duplicate;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Created by EasonYi
 */
public class BarTest {
  @Test
  @Parameters({"ba", "bb"})
  public void bar1(@Optional String ba, @Optional String bb) {
    System.out.println("bar1_" + Id.ID.getAndIncrement() + "_" + ba + "_" + bb);
  }

  @Test
  public void bar2() {
    System.out.println("bar2_" + Id.ID.getAndIncrement());
  }

  @Test
  @Parameters({"ba", "bb"})
  public void bar3(@Optional String ba, @Optional String bb) {
    System.out.println("bar3_" + Id.ID.getAndIncrement() + "_" + ba + "_" + bb);
  }
}
