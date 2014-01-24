package test.multiplelisteners;

import org.testng.Assert;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.SuiteRunner;
import org.testng.internal.IConfiguration;
import org.testng.xml.XmlSuite;

import java.lang.reflect.Field;
import java.util.List;

public class SimpleReporter implements IReporter
{
  @Override
  public void generateReport(final List<XmlSuite> xmlSuites, final List<ISuite> suites,
      final String outputDirectory)
  {
    for (final ISuite iSuite : suites)
    {
      try
      {
        final Field field = SuiteRunner.class.getDeclaredField("m_configuration");
        field.setAccessible(true);
        final IConfiguration conf = (IConfiguration) field.get(iSuite);
//        Reporter.log(iSuite.getName() + ": " + conf.getConfigurationListeners().size(), true);
        Assert.assertEquals(conf.getConfigurationListeners().size(), 1);
      }
      catch (final Exception e)
      {
        throw new RuntimeException(e);
      }
    }
  }
}
