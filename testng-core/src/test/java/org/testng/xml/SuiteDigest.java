package org.testng.xml;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A canonical, human-readable dump of everything a suite file can express, used by the round trip
 * characterization tests.
 *
 * <p>Comparing digests rather than calling {@link XmlSuite#equals(Object)} is deliberate: {@code
 * equals} ignores 11 of the 26 fields of {@link XmlSuite}, among them the parameters, the groups,
 * the method selectors, {@code preserve-order}, {@code group-by-instances}, {@code guice-stage},
 * {@code parent-module} and {@code allow-return-values}. A round trip that dropped any of those
 * would still compare equal.
 *
 * <p>Any value lost or altered by a round trip shows up as a diff on a single line.
 */
public final class SuiteDigest {

  private SuiteDigest() {}

  public static String of(XmlSuite suite) {
    StringBuilder sb = new StringBuilder();
    append(sb, "suite.name", suite.getName());
    append(sb, "suite.verbose", suite.getVerbose());
    append(sb, "suite.parallel", suite.getParallel());
    append(sb, "suite.threadCount", suite.getThreadCount());
    append(sb, "suite.dataProviderThreadCount", suite.getDataProviderThreadCount());
    append(sb, "suite.useGlobalThreadPool", suite.useGlobalThreadPool());
    append(sb, "suite.shareThreadPoolForDataProviders", suite.isShareThreadPoolForDataProviders());
    append(sb, "suite.timeOut", suite.getTimeOut());
    append(sb, "suite.configFailurePolicy", suite.getConfigFailurePolicy());
    append(sb, "suite.skipFailedInvocationCounts", suite.skipFailedInvocationCounts());
    append(sb, "suite.preserveOrder", suite.getPreserveOrder());
    append(sb, "suite.groupByInstances", suite.getGroupByInstances());
    append(sb, "suite.allowReturnValues", suite.getAllowReturnValues());
    append(sb, "suite.parentModule", suite.getParentModule());
    append(sb, "suite.guiceStage", suite.getGuiceStage());
    append(sb, "suite.objectFactory", suite.getObjectFactoryClass());
    append(sb, "suite.listeners", suite.getListeners());
    append(sb, "suite.suiteFiles", suite.getSuiteFiles());
    append(sb, "suite.parameters", sorted(suite.getParameters()));
    append(sb, "suite.includedGroups", suite.getIncludedGroups());
    append(sb, "suite.excludedGroups", suite.getExcludedGroups());
    // Included/excluded groups only reflect <run>. Without the defines and dependencies a round
    // trip could drop a suite-level <define> or <dependencies> block and still look identical.
    appendGroups(sb, "suite", suite.getGroups());
    appendPackages(sb, "suite", suite.getPackages());
    appendMethodSelectors(sb, "suite", suite.getMethodSelectors());

    List<XmlTest> tests = suite.getTests();
    append(sb, "suite.tests.count", tests.size());
    for (XmlTest test : tests) {
      appendTest(sb, test);
    }
    return sb.toString();
  }

  private static void appendTest(StringBuilder sb, XmlTest test) {
    String prefix = "test[" + test.getIndex() + ']';
    append(sb, prefix + ".name", test.getName());
    append(sb, prefix + ".verbose", test.getVerbose());
    append(sb, prefix + ".parallel", test.getParallel());
    append(sb, prefix + ".threadCount", test.getThreadCount());
    append(sb, prefix + ".timeOut", test.getTimeOut());
    append(sb, prefix + ".preserveOrder", test.getPreserveOrder());
    append(sb, prefix + ".groupByInstances", test.getGroupByInstances());
    append(sb, prefix + ".allowReturnValues", test.getAllowReturnValues());
    append(sb, prefix + ".skipFailedInvocationCounts", test.skipFailedInvocationCounts());
    append(sb, prefix + ".parameters", sorted(test.getLocalParameters()));
    append(sb, prefix + ".includedGroups", test.getIncludedGroups());
    append(sb, prefix + ".excludedGroups", test.getExcludedGroups());
    append(sb, prefix + ".metaGroups", sorted(test.getMetaGroups()));
    append(sb, prefix + ".dependencyGroups", sorted(test.getXmlDependencyGroups()));
    appendScript(sb, prefix, test.getScript());
    appendPackages(sb, prefix, test.getXmlPackages());
    appendMethodSelectors(sb, prefix, test.getMethodSelectors());

    for (XmlClass xmlClass : test.getXmlClasses()) {
      String classPrefix = prefix + ".class[" + xmlClass.getIndex() + ']';
      append(sb, classPrefix + ".name", xmlClass.getName());
      append(sb, classPrefix + ".parameters", sorted(xmlClass.getLocalParameters()));
      append(sb, classPrefix + ".excludedMethods", xmlClass.getExcludedMethods());
      for (XmlInclude include : xmlClass.getIncludedMethods()) {
        String includePrefix = classPrefix + ".include[" + include.getIndex() + ']';
        append(sb, includePrefix + ".name", include.getName());
        append(sb, includePrefix + ".description", include.getDescription());
        append(sb, includePrefix + ".invocationNumbers", include.getInvocationNumbers());
        append(sb, includePrefix + ".parameters", sorted(include.getLocalParameters()));
      }
    }
  }

  private static void appendGroups(StringBuilder sb, String prefix, XmlGroups groups) {
    if (groups == null) {
      append(sb, prefix + ".groups", null);
      return;
    }
    List<XmlDefine> defines = groups.getDefines();
    append(sb, prefix + ".groups.defines.count", defines.size());
    for (int i = 0; i < defines.size(); i++) {
      append(sb, prefix + ".groups.define[" + i + "].name", defines.get(i).getName());
      append(sb, prefix + ".groups.define[" + i + "].includes", defines.get(i).getIncludes());
    }
    List<XmlDependencies> dependencies = groups.getDependencies();
    append(sb, prefix + ".groups.dependencies.count", dependencies.size());
    for (int i = 0; i < dependencies.size(); i++) {
      append(
          sb,
          prefix + ".groups.dependencies[" + i + ']',
          sorted(dependencies.get(i).getDependencies()));
    }
  }

  /**
   * Packages are described by name and filters only. {@code XmlPackage.getXmlClasses()} is
   * deliberately not called: it scans the classpath, which would make the digest depend on the
   * runtime environment rather than on the suite file.
   */
  private static void appendPackages(StringBuilder sb, String prefix, List<XmlPackage> packages) {
    append(sb, prefix + ".packages.count", packages.size());
    for (int i = 0; i < packages.size(); i++) {
      XmlPackage xmlPackage = packages.get(i);
      append(sb, prefix + ".package[" + i + "].name", xmlPackage.getName());
      append(sb, prefix + ".package[" + i + "].include", xmlPackage.getInclude());
      append(sb, prefix + ".package[" + i + "].exclude", xmlPackage.getExclude());
    }
  }

  private static void appendMethodSelectors(
      StringBuilder sb, String prefix, List<XmlMethodSelector> selectors) {
    append(sb, prefix + ".methodSelectors.count", selectors.size());
    for (int i = 0; i < selectors.size(); i++) {
      XmlMethodSelector selector = selectors.get(i);
      String selectorPrefix = prefix + ".methodSelector[" + i + ']';
      append(sb, selectorPrefix + ".className", selector.getClassName());
      append(sb, selectorPrefix + ".priority", selector.getPriority());
      appendScript(sb, selectorPrefix, selector.getScript());
    }
  }

  private static void appendScript(StringBuilder sb, String prefix, XmlScript script) {
    if (script == null) {
      return;
    }
    append(sb, prefix + ".script.language", script.getLanguage());
    append(sb, prefix + ".script.expression", script.getExpression());
  }

  private static <V> Map<String, V> sorted(Map<String, V> map) {
    return new TreeMap<>(map);
  }

  private static void append(StringBuilder sb, String key, Object value) {
    sb.append(key).append('=').append(value).append('\n');
  }
}
