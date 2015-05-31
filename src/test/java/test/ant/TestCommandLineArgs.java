package test.ant;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import java.io.File;


/**
 * Tests some of the functionality in {@link TestNGCommandLineArgs}.
 *
 * @author jkuhnert
 */
public class TestCommandLineArgs
{

  @Test
  public void testUnixPathResolution()
  {
    String path = "/wee/wom/flibble.txt";

    String[] segments = path.split("[/\\\\]", -1);

    assertEquals(4, segments.length);
    assertEquals("wee", segments[1]);
  }

  @Test
  public void testDOSPathResolution()
  {
    String path = "c:\\\\com\\pants\\wibble.txt";

    String[] segments = path.split("[/\\\\]", -1);

    assertEquals(5, segments.length);
    assertEquals("com", segments[2]); // because c: is actually \\ which will be split twice
  }

  @Test
  public void testPathResolution()
  {
    File file = new File("pom.xml");

    assert file.exists();

    String path = file.getAbsolutePath();

    assert path.split("[/\\\\]", -1).length > 1;
  }
}
