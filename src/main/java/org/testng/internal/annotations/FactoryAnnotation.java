package org.testng.internal.annotations;

import org.testng.annotations.IFactoryAnnotation;

import java.util.List;

/**
 * An implementation of IFactory
 *
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class FactoryAnnotation extends BaseAnnotation implements IFactoryAnnotation {
  private String[] parameters = {};
  private String dataProvider = null;
  private Class<?> dataProviderClass;
  private boolean enabled = true;
  private List<Integer> indices;

  @Override
  public String getDataProvider() {
    return dataProvider;
  }

  @Override
  public void setDataProvider(String dataProvider) {
    this.dataProvider = dataProvider;
  }

  @Override
  public String[] getParameters() {
    return parameters;
  }

  public void setParameters(String[] parameters) {
    this.parameters = parameters;
  }

  @Override
  public void setDataProviderClass(Class<?> dataProviderClass) {
    this.dataProviderClass = dataProviderClass;
  }

  @Override
  public Class<?> getDataProviderClass() {
    return dataProviderClass;
  }

  @Override
  public boolean getEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public List<Integer> getIndices() {
    return indices;
  }

  @Override
  public void setIndices(List<Integer> indices) {
    this.indices = indices;
  }
}
