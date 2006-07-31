package test.converter;

import junit.framework.TestCase;


public class ConverterSample1 extends TestCase {
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
