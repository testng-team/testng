package test.tmp;

import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = { "NewUser" })
public class RegisterCommandTest {

//  Logger l = LoggerFactory.getLogger(this.getClass());

  @BeforeClass
  public void beforeClass(ITestContext itc) {
    System.out.println("BEFORECLASS getting injector from context");
//    m_inj = (Injector) itc.getAttribute("injector");
//    m_inj.injectMembers(this);

  }

  public void testExecute(ITestContext itc) throws Exception {
    System.out.println("TESTEXECUTE do somthing");
  }
}

