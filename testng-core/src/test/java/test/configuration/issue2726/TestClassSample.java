package test.configuration.issue2726;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestClassSample {

  public static final List<String> beforeLogs = new CopyOnWriteArrayList<>();
  public static final List<String> afterLogs = new CopyOnWriteArrayList<>();

  @BeforeClass
  public void setup() {
    beforeLogs.add("BEFORE_" + Thread.currentThread().getName());
  }

  @AfterClass
  public void tearDown() {
    afterLogs.add("AFTER_" + Thread.currentThread().getName());
  }

  @Test
  public void testA() {}

  @Test
  public void testB() {}
}
