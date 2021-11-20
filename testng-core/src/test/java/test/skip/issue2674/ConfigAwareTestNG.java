package test.skip.issue2674;

import org.testng.CommandLineArgs;
import org.testng.TestNG;

public class ConfigAwareTestNG extends TestNG {

  @Override
  public void configure(CommandLineArgs cla) {
    super.configure(cla);
  }
}
