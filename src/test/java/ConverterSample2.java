// Not that this file has no package (that's what we are testing) and therefore,
// it is at the wrong location, but it's easier to leave it here.  
// Also, do not change the line numbers since the test will make sure
// that the tags are generated in hardcoded line numbers
import junit.framework.TestCase;
public class ConverterSample2 extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();

        int classId =10;
        String className = "ajay";

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public final void testClassJunit() {
      int classId = 10;
        String className = "nspe";
    }


    public final void testSetClassId() {
        int classId = 12;
    }

    public final void testSetClassName() {
        String className = "account";
    }

}
