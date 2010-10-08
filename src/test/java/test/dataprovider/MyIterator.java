package test.dataprovider;

import java.util.Iterator;

public class MyIterator implements Iterator<Object[]> {
  private static int m_count = 0;
  private Object[][] m_data;

  public MyIterator(Object[][] data) {
    m_data = data;
  }

  @Override
  public boolean hasNext() {
    return m_count < m_data.length;
  }

  @Override
  public Object[] next() {
    ppp("RETURNING INDEX " + m_count);
    return m_data[m_count++];
  }

  @Override
  public void remove() {
  }

  public static int getCount() {
    return m_count;
  }

  private static void ppp(String s) {
    System.out.println("[MyIterator] " + s);
  }

}
