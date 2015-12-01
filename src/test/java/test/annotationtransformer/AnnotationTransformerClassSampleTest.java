package test.annotationtransformer;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * This test will fail unless a time out transformer
 * is applied to it.
 *
 * @author cbeust
 *
 */
@Test(timeOut = 1000)
@Listeners(MySuiteListener.class)
public class AnnotationTransformerClassSampleTest {

  public void one() {
    try {
      Thread.sleep(2000);
//      ppp("FINISHED SLEEPING");
    }
    catch (InterruptedException handled) {
      Thread.currentThread().interrupt();
//      ppp("WAS INTERRUPTED");
      // ignore
    }
  }

  private void ppp(String string) {
    System.out.println("[Transformer] " + string);
  }

}
