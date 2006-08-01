package test.expectedexceptions;

import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

/**
 * This class tests @ExpectedExceptions
 * 
 * @author cbeust
 */
public class SampleExceptions {

  @Test
  @ExpectedExceptions({ NumberFormatException.class} )
  public void shouldPass() {
    throw new NumberFormatException();
  }

  @Test
  @ExpectedExceptions({ NumberFormatException.class} )
  public void shouldFail1() {
    throw new RuntimeException();
  }

  @Test
  @ExpectedExceptions({ NumberFormatException.class} )
  public void shouldFail2() {
  }
  
//  @Test
//  @ExpectedExceptions({ FileNotFoundException.class, IOException.class })
//  public void shouldPass2() throws Exception {
//    throw new FileNotFoundException();
//  }
  
}
