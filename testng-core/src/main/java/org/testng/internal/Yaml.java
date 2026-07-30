package org.testng.internal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import org.testng.TestNGException;
import org.testng.internal.objects.InstanceCreator;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlDefine;
import org.testng.xml.XmlGroups;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlScript;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

/** YAML support for TestNG. */
public final class Yaml {

  private Yaml() {}

  public static XmlSuite parse(String filePath, InputStream is, boolean loadClasses)
      throws FileNotFoundException {
    Constructor constructor = new TestNGConstructor(XmlSuite.class, new LoaderOptions());
    {
      TypeDescription suiteDescription = new TypeDescription(XmlSuite.class);
      suiteDescription.addPropertyParameters("packages", XmlPackage.class);
      suiteDescription.addPropertyParameters("listeners", String.class);
      suiteDescription.addPropertyParameters("tests", XmlTest.class);
      suiteDescription.addPropertyParameters("method-selectors", XmlMethodSelector.class);
      constructor.addTypeDescription(suiteDescription);
    }

    {
      TypeDescription testDescription = new TypeDescription(XmlTest.class);
      testDescription.addPropertyParameters("classes", XmlClass.class);
      testDescription.addPropertyParameters("metaGroups", String.class, List.class);
      testDescription.addPropertyParameters("method-selectors", XmlMethodSelector.class);
      constructor.addTypeDescription(testDescription);
    }

    TypeDescription xmlClassDescription = new XmlClassTypeDescriptor(loadClasses);
    constructor.addTypeDescription(xmlClassDescription);

    org.yaml.snakeyaml.Yaml y = new org.yaml.snakeyaml.Yaml(constructor);
    if (is == null) {
      is = new FileInputStream(filePath);
    }
    XmlSuite result = y.load(is);

    result.setFileName(filePath);

    // Adjust XmlTest parents and indices
    int testIndex = 0;
    for (XmlTest t : result.getTests()) {
      t.setIndex(testIndex++);
      t.setSuite(result);
      int classIndex = 0;
      for (XmlClass c : t.getClasses()) {
        c.setIndex(classIndex++);
      }
    }

    return result;
  }

  /**
   * Converts an {@link XmlSuite} into YAML. This method is allowed to be used by external tools
   * (e.g. Eclipse).
   *
   * <p>The document is built as plain maps and lists and then handed to snakeyaml, which owns
   * quoting, escaping and indentation. Writing the text by hand is what made the output of this
   * method unreadable for years: a parameter valued {@code a,b}, {@code off} or {@code 2.0} needs a
   * different treatment in each context, and the emitter already knows all of them.
   *
   * <p>Only the keys the YAML reader can bind are written, so that {@code parse -> toYaml -> parse}
   * is lossless. Six values a suite file can carry are therefore left out, because no key would
   * read them back: a test {@code time-out}, an include's invocation numbers, the suite level
   * {@code group-by-instances} (the test level one is written), the object factory, {@code
   * use-global-thread-pool}, and a test {@code script} -- which is already covered by the method
   * selectors it is stored in.
   *
   * @param suite the suite to serialize
   * @return the YAML representation of the suite
   */
  public static StringBuilder toYaml(XmlSuite suite) {
    return new StringBuilder(new org.yaml.snakeyaml.Yaml(dumperOptions()).dump(suiteToMap(suite)));
  }

  private static DumperOptions dumperOptions() {
    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    options.setIndent(2);
    // Indent sequence items under the key they belong to, the shape every hand written suite file
    // in the corpus already uses.
    options.setIndicatorIndent(2);
    options.setIndentWithIndicator(true);
    // Written files must not differ between platforms.
    options.setLineBreak(DumperOptions.LineBreak.UNIX);
    // Never fold a line. Folding a plain scalar at the first of two consecutive spaces collapses
    // them, which would silently rewrite a "depends-on" listing several groups.
    options.setWidth(Integer.MAX_VALUE);
    options.setSplitLines(false);
    return options;
  }

  private static Map<String, Object> suiteToMap(XmlSuite suite) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("name", suite.getName());
    // The verbosity is compared against the level that is actually in effect rather than against
    // XmlSuite.DEFAULT_VERBOSE: getVerbose() falls back to -Dtestng.default.verbose, so comparing
    // against the constant would write out a value the suite never declared, and would make the
    // output depend on the JVM it was produced in.
    putIfDifferent(result, "verbose", suite.getVerbose(), RuntimeBehavior.getDefaultVerboseLevel());
    putIfDifferent(result, "parallel", suite.getParallel(), XmlSuite.DEFAULT_PARALLEL);
    putIfDifferent(result, "threadCount", suite.getThreadCount(), XmlSuite.DEFAULT_THREAD_COUNT);
    putIfDifferent(
        result,
        "dataProviderThreadCount",
        suite.getDataProviderThreadCount(),
        defaultDataProviderThreadCount());
    putIfPresent(result, "timeOut", suite.getTimeOut());
    putIfDifferent(
        result,
        "configFailurePolicy",
        suite.getConfigFailurePolicy(),
        XmlSuite.DEFAULT_CONFIG_FAILURE_POLICY);
    putIfDifferent(
        result,
        "skipFailedInvocationCounts",
        suite.skipFailedInvocationCounts(),
        XmlSuite.DEFAULT_SKIP_FAILED_INVOCATION_COUNTS);
    putIfDifferent(
        result, "preserveOrder", suite.getPreserveOrder(), XmlSuite.DEFAULT_PRESERVE_ORDER);
    putIfDifferent(
        result,
        "allowReturnValues",
        suite.getAllowReturnValues(),
        XmlSuite.DEFAULT_ALLOW_RETURN_VALUES);
    putIfDifferent(
        result,
        "shareThreadPoolForDataProviders",
        suite.isShareThreadPoolForDataProviders(),
        XmlSuite.DEFAULT_SHARE_THREAD_POOL_FOR_DATA_PROVIDERS);
    putIfPresent(result, "parentModule", suite.getParentModule());
    putIfPresent(result, "guiceStage", suite.getGuiceStage());
    putIfPresent(result, "parameters", parameters(suite.getParameters()));
    putIfPresent(result, "listeners", copyOf(suite.getListeners()));
    putGroups(result, suite.getGroups());
    putIfPresent(result, "packages", packagesToNodes(suite.getXmlPackages()));
    putIfPresent(result, "methodSelectors", selectorsToNodes(suite.getMethodSelectors()));
    putIfPresent(result, "suiteFiles", copyOf(suite.getSuiteFiles()));

    List<Object> tests = new ArrayList<>();
    for (XmlTest test : suite.getTests()) {
      tests.add(testToMap(test));
    }
    putIfPresent(result, "tests", tests);
    return result;
  }

  /**
   * Values a test inherits from its suite are compared against the suite rather than against the
   * defaults, and dropped when they match. The getters of {@link XmlTest} fall back to the suite,
   * so writing them unconditionally would materialize the suite's values into every test.
   */
  private static Map<String, Object> testToMap(XmlTest test) {
    XmlSuite suite = test.getSuite();
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("name", test.getName());
    putIfDifferent(result, "verbose", test.getVerbose(), suite.getVerbose());
    putIfDifferent(result, "parallel", test.getParallel(), suite.getParallel());
    putIfDifferent(result, "threadCount", test.getThreadCount(), suite.getThreadCount());
    putIfDifferent(result, "preserveOrder", test.getPreserveOrder(), suite.getPreserveOrder());
    putIfDifferent(
        result, "groupByInstances", test.getGroupByInstances(), suite.getGroupByInstances());
    putIfDifferent(
        result, "allowReturnValues", test.getAllowReturnValues(), suite.getAllowReturnValues());
    putIfDifferent(
        result,
        "skipFailedInvocationCounts",
        test.skipFailedInvocationCounts(),
        suite.skipFailedInvocationCounts());
    putIfPresent(result, "parameters", parameters(test.getLocalParameters()));
    putGroups(result, test.getXmlGroups());
    putIfPresent(result, "xmlDependencyGroups", sorted(test.getXmlDependencyGroups()));
    putIfPresent(result, "methodSelectors", selectorsToNodes(test.getMethodSelectors()));
    putIfPresent(result, "packages", packagesToNodes(test.getXmlPackages()));
    putIfPresent(result, "classes", classesToNodes(test.getXmlClasses()));
    return result;
  }

  /**
   * The {@code <groups>} block is read from the model it was parsed into, never from {@code
   * getIncludedGroups()}: on a test that getter returns the union with the suite's groups, and on a
   * suite it delegates to the parent suite. Either one would duplicate groups on the way out.
   */
  private static void putGroups(Map<String, Object> result, XmlGroups groups) {
    if (groups == null) {
      return;
    }
    if (groups.getRun() != null) {
      putIfPresent(result, "includedGroups", copyOf(groups.getRun().getIncludes()));
      putIfPresent(result, "excludedGroups", copyOf(groups.getRun().getExcludes()));
    }
    Map<String, Object> metaGroups = new TreeMap<>();
    for (XmlDefine define : groups.getDefines()) {
      metaGroups.put(define.getName(), copyOf(define.getIncludes()));
    }
    putIfPresent(result, "metaGroups", metaGroups);
  }

  private static List<Object> packagesToNodes(List<XmlPackage> packages) {
    List<Object> result = new ArrayList<>();
    for (XmlPackage xmlPackage : packages) {
      result.add(packageToNode(xmlPackage));
    }
    return result;
  }

  /**
   * A package with no filter collapses to its name, the form the reader builds through {@code
   * XmlPackage(String)} and the one the hand written fixtures use.
   *
   * <p>{@code getXmlClasses()} is deliberately not called: it scans the classpath, which has
   * nothing to do with what the suite file says.
   */
  private static Object packageToNode(XmlPackage xmlPackage) {
    List<String> include = xmlPackage.getInclude();
    List<String> exclude = xmlPackage.getExclude();
    if (include.isEmpty() && exclude.isEmpty()) {
      return xmlPackage.getName();
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("name", xmlPackage.getName());
    // Singular, because that is what the reader binds -- XmlPackage.setInclude/setExclude.
    putIfPresent(result, "include", copyOf(include));
    putIfPresent(result, "exclude", copyOf(exclude));
    return result;
  }

  private static List<Object> classesToNodes(List<XmlClass> classes) {
    List<Object> result = new ArrayList<>();
    for (XmlClass xmlClass : classes) {
      result.add(classToNode(xmlClass));
    }
    return result;
  }

  private static Object classToNode(XmlClass xmlClass) {
    Map<String, Object> parameters = parameters(xmlClass.getLocalParameters());
    List<Object> includedMethods = includesToNodes(xmlClass.getIncludedMethods());
    List<String> excludedMethods = xmlClass.getExcludedMethods();
    if (parameters.isEmpty() && includedMethods.isEmpty() && excludedMethods.isEmpty()) {
      return xmlClass.getName();
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("name", xmlClass.getName());
    putIfPresent(result, "parameters", parameters);
    putIfPresent(result, "includedMethods", includedMethods);
    putIfPresent(result, "excludedMethods", copyOf(excludedMethods));
    return result;
  }

  private static List<Object> includesToNodes(List<XmlInclude> includes) {
    List<Object> result = new ArrayList<>();
    for (XmlInclude include : includes) {
      result.add(includeToNode(include));
    }
    return result;
  }

  /**
   * The invocation numbers of an include are not written: {@link XmlInclude} exposes them through
   * {@code addInvocationNumbers}, not through a setter, so no key would read them back.
   */
  private static Object includeToNode(XmlInclude include) {
    Map<String, Object> parameters = parameters(include.getLocalParameters());
    if (parameters.isEmpty() && include.getDescription() == null) {
      return include.getName();
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("name", include.getName());
    putIfPresent(result, "description", include.getDescription());
    putIfPresent(result, "parameters", parameters);
    return result;
  }

  private static List<Object> selectorsToNodes(List<org.testng.xml.XmlMethodSelector> selectors) {
    List<Object> result = new ArrayList<>();
    for (org.testng.xml.XmlMethodSelector selector : selectors) {
      result.add(selectorToMap(selector));
    }
    return result;
  }

  /**
   * Method selectors are written flat, because that is how the reader takes them apart: {@code
   * ConstructXmlScript} reads {@code className}, {@code priority}, {@code expression} and {@code
   * language} off the mapping itself and ignores anything else.
   */
  private static Map<String, Object> selectorToMap(org.testng.xml.XmlMethodSelector selector) {
    Map<String, Object> result = new LinkedHashMap<>();
    putIfPresent(result, "className", selector.getClassName());
    putIfDifferent(
        result,
        "priority",
        selector.getPriority(),
        org.testng.xml.XmlMethodSelector.DEFAULT_PRIORITY);
    XmlScript script = selector.getScript();
    if (script != null) {
      putIfPresent(result, "expression", script.getExpression());
      putIfPresent(result, "language", script.getLanguage());
    }
    return result;
  }

  /**
   * Parameters are read through a raw map on purpose. The reader has no type description for them,
   * so snakeyaml resolves {@code true} or {@code 44.0} to a {@link Boolean} or a {@link Double} and
   * stores it in a {@code Map<String, String>} through an erased setter -- iterating it as strings
   * would throw. Handing those values back to the emitter as they are makes it quote them, which is
   * what puts a {@link String} back in the map on the next read.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Map<String, Object> parameters(Map<String, String> parameters) {
    return sorted((Map) parameters);
  }

  /** Sorted, because the model stores these in hash maps and a file must not depend on that. */
  private static Map<String, Object> sorted(Map<String, ?> map) {
    return new TreeMap<>(map);
  }

  private static <T> List<T> copyOf(List<T> values) {
    return new ArrayList<>(values);
  }

  private static int defaultDataProviderThreadCount() {
    String property = RuntimeBehavior.getDefaultDataProviderThreadCount();
    try {
      if (!property.trim().isEmpty()) {
        return Integer.parseInt(property);
      }
    } catch (NumberFormatException ignored) {
      // getDataProviderThreadCount() falls back to the suite's value in that case, so do we.
    }
    return XmlSuite.DEFAULT_DATA_PROVIDER_THREAD_COUNT;
  }

  private static void putIfDifferent(
      Map<String, Object> result, String key, Object value, Object defaultValue) {
    if (value != null && !value.equals(defaultValue)) {
      result.put(key, value instanceof Enum ? value.toString() : value);
    }
  }

  private static void putIfPresent(Map<String, Object> result, String key, Object value) {
    if (value == null) {
      return;
    }
    if (value instanceof String && ((String) value).isEmpty()) {
      return;
    }
    if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
      return;
    }
    if (value instanceof Map && ((Map<?, ?>) value).isEmpty()) {
      return;
    }
    result.put(key, value);
  }

  private static class TestNGConstructor extends Constructor {

    public TestNGConstructor(Class<?> theRoot, LoaderOptions loadingConfig) {
      super(theRoot, loadingConfig);
      yamlClassConstructors.put(NodeId.scalar, new ConstructParallelMode());
      yamlClassConstructors.put(NodeId.mapping, new ConstructXmlScript());
    }

    private class ConstructXmlScript extends ConstructMapping {

      @Override
      public Object construct(Node node) {
        if (node.getType().equals(org.testng.xml.XmlMethodSelector.class)) {
          final XmlScript xmlScript = new XmlScript();
          org.testng.xml.XmlMethodSelector selector = new org.testng.xml.XmlMethodSelector();
          MappingNode mappingNode = (MappingNode) node;
          List<NodeTuple> tuples = mappingNode.getValue();
          for (NodeTuple tuple : tuples) {
            setValue(tuple, "expression", xmlScript::setExpression);
            setValue(tuple, "language", xmlScript::setLanguage);
            setValue(tuple, "className", selector::setClassName);
            setValue(tuple, "priority", text -> selector.setPriority(Integer.parseInt(text)));
          }
          selector.setScript(xmlScript);
          return selector;
        }
        return super.construct(node);
      }

      private void setValue(NodeTuple tuple, String key, Consumer<String> cons) {
        ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
        ScalarNode valueNode = (ScalarNode) tuple.getValueNode();
        if (keyNode.getValue().equals(key)) {
          String value = constructScalar(valueNode);
          cons.accept(value);
        }
      }
    }

    private class ConstructParallelMode extends ConstructScalar {

      @Override
      public Object construct(Node node) {
        if (node.getType().equals(XmlSuite.ParallelMode.class)) {
          String parallel = constructScalar((ScalarNode) node);
          return XmlSuite.ParallelMode.getValidParallel(parallel);
        }
        if (node.getType().equals(XmlSuite.FailurePolicy.class)) {
          String failurePolicy = constructScalar((ScalarNode) node);
          return XmlSuite.FailurePolicy.getValidPolicy(failurePolicy);
        }
        return super.construct(node);
      }
    }
  }

  private static class XmlClassTypeDescriptor extends TypeDescription {

    private final boolean loadClasses;

    public XmlClassTypeDescriptor(boolean loadClasses) {
      super(XmlClass.class);
      this.loadClasses = loadClasses;
    }

    @Override
    public Object newInstance(Node node) {
      String className;

      try {
        java.lang.reflect.Constructor<?> c =
            XmlClass.class.getDeclaredConstructor(String.class, boolean.class);
        c.setAccessible(true);
        if (node instanceof MappingNode) {
          Node valueNode =
              ((MappingNode) node)
                  .getValue().stream()
                      .filter(
                          nodeTuple ->
                              ((ScalarNode) nodeTuple.getKeyNode()).getValue().equals("name"))
                      .findFirst()
                      .orElseThrow(() -> new TestNGException("Node 'name' not found"))
                      .getValueNode();
          className = ((ScalarNode) valueNode).getValue();
        } else {
          className = ((ScalarNode) node).getValue();
        }
        return InstanceCreator.newInstance(c, className, loadClasses);
      } catch (Exception e) {
        throw new TestNGException("Failed to instantiate class", e);
      }
    }
  }
}
