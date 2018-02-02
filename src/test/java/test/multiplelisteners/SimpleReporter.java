package test.multiplelisteners;

import org.testng.IAttributes;
import org.testng.IReporter2;
import org.testng.ISuite;
import org.testng.SuiteRunner;
import org.testng.TestListenerAdapter;
import org.testng.internal.IConfiguration;
import org.testng.xml.XmlSuite;
import test.listeners.ListenerAssert;

import java.lang.reflect.Field;
import java.util.List;

public class SimpleReporter implements IReporter2
{
  @Override
  public void generateReport(final List<XmlSuite> xmlSuites, final List<ISuite> suites,
      final IAttributes attributes)
  {
    for (final ISuite iSuite : suites)
    {
      try
      {
        final Field field = SuiteRunner.class.getDeclaredField("m_configuration");
        field.setAccessible(true);
        final IConfiguration conf = (IConfiguration) field.get(iSuite);
        ListenerAssert.assertListenerType(conf.getConfigurationListeners(), TestListenerAdapter.class);
      }
      catch (final Exception e)
      {
        throw new RuntimeException(e);
      }
    }
  }
}
