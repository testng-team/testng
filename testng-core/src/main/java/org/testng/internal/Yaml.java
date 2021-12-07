package org.testng.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.testng.TestNGException;
import org.testng.internal.objects.InstanceCreator;
import org.testng.util.Strings;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlScript;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
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
    Constructor constructor = new TestNGConstructor(XmlSuite.class);
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
      is = new FileInputStream(new File(filePath));
    }
    XmlSuite result = y.load(is);

    result.setFileName(filePath);

    // Adjust XmlTest parents and indices
    for (XmlTest t : result.getTests()) {
      t.setSuite(result);
      int index = 0;
      for (XmlClass c : t.getClasses()) {
        c.setIndex(index++);
      }
    }

    return result;
  }

  private static void maybeAdd(StringBuilder sb, String key, Object value, Object def) {
    maybeAdd(sb, "", key, value, def);
  }

  private static void maybeAdd(StringBuilder sb, String sp, String key, Object value, Object def) {
    if (value != null && !value.equals(def)) {
      sb.append(sp).append(key).append(": ").append(value.toString()).append("\n");
    }
  }

  /*
   * The main entry point to convert an XmlSuite into YAML. This method is allowed to be used by
   * external tools (e.g. Eclipse).
   */
  public static StringBuilder toYaml(XmlSuite suite) {
    StringBuilder result = new StringBuilder();

    maybeAdd(result, "name", suite.getName(), null);
    maybeAdd(result, "junit", suite.isJUnit(), XmlSuite.DEFAULT_JUNIT);
    maybeAdd(result, "verbose", suite.getVerbose(), XmlSuite.DEFAULT_VERBOSE);
    maybeAdd(result, "threadCount", suite.getThreadCount(), XmlSuite.DEFAULT_THREAD_COUNT);
    maybeAdd(
        result,
        "dataProviderThreadCount",
        suite.getDataProviderThreadCount(),
        XmlSuite.DEFAULT_DATA_PROVIDER_THREAD_COUNT);
    maybeAdd(result, "timeOut", suite.getTimeOut(), null);
    maybeAdd(result, "parallel", suite.getParallel(), XmlSuite.DEFAULT_PARALLEL);
    maybeAdd(
        result,
        "configFailurePolicy",
        suite.getConfigFailurePolicy().toString(),
        XmlSuite.DEFAULT_CONFIG_FAILURE_POLICY);
    maybeAdd(
        result,
        "skipFailedInvocationCounts",
        suite.skipFailedInvocationCounts(),
        XmlSuite.DEFAULT_SKIP_FAILED_INVOCATION_COUNTS);

    toYaml(result, "", suite.getParameters());
    toYaml(result, suite.getPackages());

    if (!suite.getListeners().isEmpty()) {
      result.append("listeners:\n");
      toYaml(result, "  ", suite.getListeners());
    }

    if (!suite.getPackages().isEmpty()) {
      result.append("packages:\n");
      toYaml(result, suite.getPackages());
    }
    if (!suite.getTests().isEmpty()) {
      result.append("tests:\n");
      for (XmlTest t : suite.getTests()) {
        toYaml(result, t);
      }
    }

    if (!suite.getChildSuites().isEmpty()) {
      result.append("suite-files:\n");
      toYaml(result, "  ", suite.getSuiteFiles());
    }

    return result;
  }

  /** Convert a XmlTest into YAML */
  private static void toYaml(StringBuilder result, XmlTest t) {
    String sp2 = Strings.repeat(" ", 2);
    result.append("  ").append("- name: ").append(t.getName()).append("\n");

    maybeAdd(result, sp2, "junit", t.isJUnit(), XmlSuite.DEFAULT_JUNIT);
    maybeAdd(result, sp2, "verbose", t.getVerbose(), XmlSuite.DEFAULT_VERBOSE);
    maybeAdd(result, sp2, "timeOut", t.getTimeOut(), null);
    maybeAdd(result, sp2, "parallel", t.getParallel(), XmlSuite.DEFAULT_PARALLEL);
    maybeAdd(
        result,
        sp2,
        "skipFailedInvocationCounts",
        t.skipFailedInvocationCounts(),
        XmlSuite.DEFAULT_SKIP_FAILED_INVOCATION_COUNTS);

    maybeAdd(result, "preserveOrder", sp2, t.getPreserveOrder(), XmlSuite.DEFAULT_PRESERVE_ORDER);

    toYaml(result, sp2, t.getLocalParameters());

    if (!t.getIncludedGroups().isEmpty()) {
      result
          .append(sp2)
          .append("includedGroups: [ ")
          .append(Utils.join(t.getIncludedGroups(), ","))
          .append(" ]\n");
    }

    if (!t.getExcludedGroups().isEmpty()) {
      result
          .append(sp2)
          .append("excludedGroups: [ ")
          .append(Utils.join(t.getExcludedGroups(), ","))
          .append(" ]\n");
    }

    if (!t.getXmlDependencyGroups().isEmpty()) {
      result.append(sp2).append(sp2).append("xmlDependencyGroups:\n");
      t.getXmlDependencyGroups()
          .forEach(
              (k, v) -> result.append(sp2).append(sp2).append(sp2).append(k + ": " + v + "\n"));
    }

    Map<String, List<String>> mg = t.getMetaGroups();
    if (mg.size() > 0) {
      result.append(sp2).append("metaGroups: { ");
      boolean first = true;
      for (Map.Entry<String, List<String>> entry : mg.entrySet()) {
        if (!first) {
          result.append(", ");
        }
        result
            .append(entry.getKey())
            .append(": [ ")
            .append(Utils.join(entry.getValue(), ","))
            .append(" ] ");
        first = false;
      }
      result.append(" }\n");
    }

    if (!t.getXmlPackages().isEmpty()) {
      result.append(sp2).append(sp2).append("xmlPackages:\n");
      for (XmlPackage xp : t.getXmlPackages()) {
        toYaml(result, sp2 + "  - ", xp);
      }
    }

    if (!t.getXmlClasses().isEmpty()) {
      result.append(sp2).append("classes:\n");
      for (XmlClass xc : t.getXmlClasses()) {
        toYaml(result, sp2 + "  ", xc);
      }
    }

    result.append("\n");
  }

  private static void toYaml(StringBuilder result, String sp2, XmlClass xc) {
    List<XmlInclude> im = xc.getIncludedMethods();
    List<String> em = xc.getExcludedMethods();
    String name = (im.isEmpty() && em.isEmpty()) ? "" : "name: ";

    result.append(sp2).append("- ").append(name).append(xc.getName()).append("\n");
    if (!im.isEmpty()) {
      result.append(sp2).append("  includedMethods:\n");
      for (XmlInclude xi : im) {
        toYaml(result, sp2 + "    ", xi);
      }
    }

    if (!em.isEmpty()) {
      result.append(sp2).append("  excludedMethods:\n");
      toYaml(result, sp2 + "    ", em);
    }
  }

  private static void toYaml(StringBuilder result, String sp, XmlInclude xi) {
    result.append(sp).append("- name: ").append(xi.getName()).append("\n");
    String sp2 = sp + "  ";
    toYaml(result, sp2, xi.getLocalParameters());
  }

  private static void toYaml(StringBuilder result, String sp, List<String> strings) {
    for (String l : strings) {
      result.append(sp).append("- ").append(l).append("\n");
    }
  }

  private static void toYaml(StringBuilder sb, List<XmlPackage> packages) {
    if (!packages.isEmpty()) {
      sb.append("packages:\n");
      for (XmlPackage p : packages) {
        toYaml(sb, "  ", p);
      }
    }
    for (XmlPackage p : packages) {
      toYaml(sb, "  ", p);
    }
  }

  private static void toYaml(StringBuilder sb, String sp, XmlPackage p) {
    sb.append(sp).append("name: ").append(p.getName()).append("\n");

    generateIncludeExclude(sb, sp, "includes", p.getInclude());
    generateIncludeExclude(sb, sp, "excludes", p.getExclude());
  }

  private static void generateIncludeExclude(
      StringBuilder sb, String sp, String key, List<String> includes) {
    if (!includes.isEmpty()) {
      sb.append(sp).append("  ").append(key).append("\n");
      for (String inc : includes) {
        sb.append(sp).append("    ").append(inc);
      }
    }
  }

  private static void mapToYaml(Map<String, String> map, StringBuilder out) {
    if (map.size() > 0) {
      out.append("{ ");
      boolean first = true;
      for (Map.Entry<String, String> e : map.entrySet()) {
        if (!first) {
          out.append(", ");
        }
        first = false;
        out.append(e.getKey()).append(": ").append(e.getValue());
      }
      out.append(" }\n");
    }
  }

  private static void toYaml(StringBuilder sb, String sp, Map<String, String> parameters) {
    if (!parameters.isEmpty()) {
      sb.append(sp).append("parameters").append(": ");
      mapToYaml(parameters, sb);
    }
  }

  private static class TestNGConstructor extends Constructor {

    public TestNGConstructor(Class<?> theRoot) {
      super(theRoot);
      yamlClassConstructors.put(NodeId.scalar, new ConstructParallelMode());
      yamlClassConstructors.put(NodeId.mapping, new ConstructXmlScript());
    }

    private class ConstructXmlScript extends ConstructMapping {

      @Override
      public Object construct(Node node) {
        if (node.getType().equals(org.testng.xml.XmlMethodSelector.class)) {
          final XmlScript xmlScript = new XmlScript();
          org.testng.xml.XmlMethodSelector selector = new org.testng.xml.XmlMethodSelector();
          MappingNode mappingNode = ((MappingNode) node);
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
