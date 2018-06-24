package test.beforegroups;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.internal.ClassHelper;
import org.testng.internal.PackageUtils;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.beforegroups.issue1694.BaseClassWithBeforeGroups;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BeforeGroupsTest extends SimpleBaseTest {
  @Test
  public void testInSequentialMode() throws IOException {
    runTest(XmlSuite.ParallelMode.NONE);
  }

  @Test
  public void testParallelMode() throws IOException {
    runTest(XmlSuite.ParallelMode.CLASSES);
  }

  private static void runTest(XmlSuite.ParallelMode mode) throws IOException {
    XmlSuite suite = createXmlSuite("sample_suite");
    String pkg = BaseClassWithBeforeGroups.class.getPackage().getName();
    Class<?>[] classes = findClassesInPackage(pkg);
    XmlTest xmlTest = createXmlTestWithPackages(suite, "sample_test", classes);
    xmlTest.addIncludedGroup("regression");
    xmlTest.setParallel(mode);
    TestNG tng = create(suite);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();
    List<String> beforeGroups = Lists.newArrayList();
    List<String> afterGroups = Lists.newArrayList();
    for (String name : listener.getInvokedMethodNames()) {
      if (name.equalsIgnoreCase(BaseClassWithBeforeGroups.BEFORE_GROUPS)) {
        beforeGroups.add(name);
      }
      if (name.equalsIgnoreCase(BaseClassWithBeforeGroups.AFTER_GROUPS)) {
        afterGroups.add(name);
      }
    }
    assertThat(beforeGroups).containsOnly(BaseClassWithBeforeGroups.BEFORE_GROUPS);
    assertThat(afterGroups).containsOnly(BaseClassWithBeforeGroups.AFTER_GROUPS);
  }

  private static Class<?>[] findClassesInPackage(String packageName) throws IOException {
    String[] classes =
        PackageUtils.findClassesInPackage(packageName, new ArrayList<>(), new ArrayList<>());
    List<Class<?>> loadedClasses = new ArrayList<>();
    for (String clazz : classes) {
      loadedClasses.add(ClassHelper.forName(clazz));
    }
    return loadedClasses.toArray(new Class<?>[0]);
  }
}
