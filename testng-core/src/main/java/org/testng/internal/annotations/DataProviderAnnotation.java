package org.testng.internal.annotations;

import java.util.List;
import org.testng.annotations.IDataProviderAnnotation;

/** An implementation of IDataProvider. */
public class DataProviderAnnotation extends BaseAnnotation implements IDataProviderAnnotation {

  private String m_name;
  private boolean m_parallel;
  private List<Integer> m_indices;

  @Override
  public boolean isParallel() {
    return m_parallel;
  }

  @Override
  public void setParallel(boolean parallel) {
    m_parallel = parallel;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public void setName(String name) {
    m_name = name;
  }

  @Override
  public List<Integer> getIndices() {
    return m_indices;
  }

  @Override
  public void setIndices(List<Integer> indices) {
    m_indices = indices;
  }
}
