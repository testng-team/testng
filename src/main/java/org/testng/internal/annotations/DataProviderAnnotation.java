package org.testng.internal.annotations;

import org.testng.annotations.IDataProviderAnnotation;

import java.util.List;

/**
 * An implementation of IDataProvider.
 *
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class DataProviderAnnotation extends BaseAnnotation implements IDataProviderAnnotation {
  private String name;
  private boolean parallel;
  private List<Integer> indices;

  @Override
  public boolean isParallel() {
    return parallel;
  }

  @Override
  public void setParallel(boolean parallel) {
    this.parallel = parallel;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
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
