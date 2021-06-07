package test.reflect;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.testng.IClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

/**
 * Created on 12/30/15.
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 */
public class TestResultJustForTesting implements ITestResult {

  private final String id = UUID.randomUUID().toString();

  @Override
  public int getStatus() {
    return 0;
  }

  @Override
  public void setStatus(int status) {}

  @Override
  public ITestNGMethod getMethod() {
    return null;
  }

  @Override
  public Object[] getParameters() {
    return new Object[0];
  }

  @Override
  public void setParameters(Object[] parameters) {}

  @Override
  public IClass getTestClass() {
    return null;
  }

  @Override
  public Throwable getThrowable() {
    return null;
  }

  @Override
  public void setThrowable(Throwable throwable) {}

  @Override
  public long getStartMillis() {
    return 0;
  }

  @Override
  public long getEndMillis() {
    return 0;
  }

  @Override
  public void setEndMillis(long millis) {}

  @Override
  public String getName() {
    return null;
  }

  @Override
  public boolean isSuccess() {
    return false;
  }

  @Override
  public String getHost() {
    return null;
  }

  @Override
  public Object getInstance() {
    return null;
  }

  @Override
  public Object[] getFactoryParameters() {
    return new Object[0];
  }

  @Override
  public String getTestName() {
    return null;
  }

  @Override
  public void setTestName(String name) {}

  @Override
  public boolean wasRetried() {
    return false;
  }

  @Override
  public void setWasRetried(boolean wasRetried) {}

  @Override
  public List<ITestNGMethod> getSkipCausedBy() {
    return null;
  }

  @Override
  public String getInstanceName() {
    return null;
  }

  @Override
  public ITestContext getTestContext() {
    return null;
  }

  @Override
  public int compareTo(ITestResult o) {
    return 0;
  }

  @Override
  public Object getAttribute(String name) {
    return null;
  }

  @Override
  public void setAttribute(String name, Object value) {}

  @Override
  public Set<String> getAttributeNames() {
    return null;
  }

  @Override
  public Object removeAttribute(String name) {
    return null;
  }

  @Override
  public String id() {
    return id;
  }
}
