package org.testng.compatibility;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.testng.ConversionUtils;
import org.testng.Converter;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IParameterizable;
import org.testng.annotations.ITestOrConfiguration;

/** Calls the API supplied by TestNG 7.12.0. */
@SuppressWarnings("deprecation")
public class LegacyApiConsumer {

  public static Object[] wrapDataProvider(Class<?> type, Collection<Object[]> rows) {
    return ConversionUtils.wrapDataProvider(type, rows);
  }

  public static Object newConverter() {
    return new Converter();
  }

  public static void convert(String[] args) throws IOException {
    Converter.main(args);
  }

  public static ITestOrConfiguration configuration() {
    return new LegacyConfiguration();
  }

  public static IFactoryAnnotation factory() {
    return new LegacyFactory();
  }

  private static UnsupportedOperationException unused() {
    return new UnsupportedOperationException("Not used by the compatibility consumer");
  }

  private abstract static class LegacyParameterizable implements IParameterizable {
    @Override
    public boolean getEnabled() {
      throw unused();
    }

    @Override
    public void setEnabled(boolean enabled) {
      throw unused();
    }
  }

  private static class LegacyConfiguration extends LegacyParameterizable
      implements ITestOrConfiguration {
    @Override
    public long getTimeOut() {
      throw unused();
    }

    @Override
    public void setTimeOut(long timeOut) {
      throw unused();
    }

    @Override
    public String[] getGroups() {
      throw unused();
    }

    @Override
    public void setGroups(String[] groups) {
      throw unused();
    }

    @Override
    public String[] getDependsOnGroups() {
      throw unused();
    }

    @Override
    public void setDependsOnGroups(String[] groups) {
      throw unused();
    }

    @Override
    public String[] getDependsOnMethods() {
      throw unused();
    }

    @Override
    public void setDependsOnMethods(String[] methods) {
      throw unused();
    }

    @Override
    public String getDescription() {
      throw unused();
    }

    @Override
    public void setDescription(String description) {
      throw unused();
    }
  }

  private static class LegacyFactory extends LegacyParameterizable implements IFactoryAnnotation {
    @Override
    public List<Integer> getIndices() {
      throw unused();
    }

    @Override
    public void setIndices(List<Integer> indices) {
      throw unused();
    }

    @Override
    public String getDataProvider() {
      throw unused();
    }

    @Override
    public void setDataProvider(String dataProvider) {
      throw unused();
    }

    @Override
    public Class<?> getDataProviderClass() {
      throw unused();
    }

    @Override
    public void setDataProviderClass(Class<?> dataProviderClass) {
      throw unused();
    }

    @Override
    public String getDataProviderDynamicClass() {
      throw unused();
    }

    @Override
    public void setDataProviderDynamicClass(String dataProviderClass) {
      throw unused();
    }
  }
}
