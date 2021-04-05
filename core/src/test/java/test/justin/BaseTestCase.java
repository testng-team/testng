package test.justin;

import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jlee@antwerkz.com">Justin Lee</a> Date: Aug 15, 2004
 */
@Test
public abstract class BaseTestCase {
    protected static final String TEST_PASSWORD = "testPassword";

    public BaseTestCase() {
        init();
    }

    public BaseTestCase(String name) {
        this();
    }

    private void init() {
        setSessionUser(null);
    }

    protected void commit() {
    }

    protected void tearDown() throws Exception {
        commit();
    }

    protected Object createCustomer() throws Exception {
      return null;
    }

    protected Object createProject() throws Exception {
      return null;
    }

    protected Object createTimeEntry() throws Exception {
        return null;
    }

    protected Object createUser(String name) throws Exception {
      return null;
    }

    protected Object createUserGroup() throws Exception {
      return null;
    }

    protected void setSessionUser(Object user) {
    }
}
