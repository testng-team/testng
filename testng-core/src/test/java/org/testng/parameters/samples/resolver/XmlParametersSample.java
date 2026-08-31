package test.inject.parameterresolver;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * A resolver owned parameter beside one supplied by {@code <parameter>} in the suite file. The
 * {@code @Parameters} names cover only what testng.xml supplies, so there is one of them for two
 * declared parameters.
 */
public class XmlParametersSample {

  @Test
  @Parameters({"greeting"})
  public void test(@FromResolver CustomObject custom, String greeting) {
    ParameterRecorder.record("test", custom, greeting);
  }

  @Test
  @Parameters({"greeting"})
  public void reversed(String greeting, @FromResolver CustomObject custom) {
    ParameterRecorder.record("reversed", greeting, custom);
  }
}
