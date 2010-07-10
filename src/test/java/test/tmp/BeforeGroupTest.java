package test.tmp;

import org.testng.ITestContext;
import org.testng.annotations.BeforeGroups;

public class BeforeGroupTest {

//  Logger l = LoggerFactory.getLogger(this.getClass());

  @BeforeGroups(groups = { "NewUser" }, value = { "NewUser" })
  public void preNewUser(ITestContext itc) {
    System.out.println("BEFOREGROUPS perfroming pre groups init");
//    m_inj = Guice.createInjector(new JUnitModule(), new RequestModule(),
//        new GenericModule(), new SecurityModule());
//    itc.setAttribute("injector", m_inj);
  }
}