package test.duplicate;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Created by EasonYi
 */
public class HelloTest {
  @Test
  @Parameters({"request_hello1", "response_hello1"})
  public void hello1(@Optional String ba, @Optional String bb) {
    System.out.println("hello1_" + Id.ID.getAndIncrement() + "_" + ba + "_" + bb);
  }

  @Test
  public void hello2() {
    System.out.println("hello2_" + Id.ID.getAndIncrement());
  }

  @Test
  @Parameters({"request_hello3", "response_hello3"})
  public void hello3(@Optional String ba, @Optional String bb) {
    System.out.println("hello3_" + Id.ID.getAndIncrement() + "_" + ba + "_" + bb);
  }

  @Test
  public void hello4() {
    int id = Id.ID.getAndIncrement();
    System.out.println("begin hello4_" + id);
    TestUtil.sleepSomeMilliSeconds(1000);
    System.out.println("end hello4_" + id);
  }
}
