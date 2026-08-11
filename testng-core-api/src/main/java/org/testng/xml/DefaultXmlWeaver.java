package org.testng.xml;

import static org.testng.collections.CollectionUtils.hasElements;
import static org.testng.internal.Utils.isStringNotEmpty;
import static org.testng.xml.XmlSuite.*;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.testng.TestNGException;
import org.testng.internal.Utils;
import org.testng.reporters.XMLStringBuffer;

/**
 * This class provides String representation of both {@link XmlSuite} and {@link XmlTest} but adds
 * an XML comment as the test name and suite name at the end of the corresponding tags.
 *
 * <p>It is also the base class to extend when you want to change how a suite is written without
 * rewriting the whole serializer. There is one {@code asXml} method per element of {@code
 * testng.xml}, each {@code protected}, so a subclass overrides only the element it cares about and
 * inherits the rest:
 *
 * <pre>{@code
 * public class MyWeaver extends DefaultXmlWeaver {
 *   @Override
 *   protected void asXml(XMLStringBuffer xsb, XmlClass xmlClass) {
 *     xsb.addEmptyElement("class", "name", xmlClass.getName());
 *   }
 * }
 * }</pre>
 *
 * <p>Each hook writes into the buffer it is handed, so indentation follows the buffer's own
 * push/pop depth and a subclass never has to compute it.
 *
 * <p>Select it at runtime with {@code -Dtestng.xml.weaver=fully.qualified.MyWeaver}. The class
 * needs a public no-argument constructor, which is how {@code XmlWeaver} instantiates it.
 *
 * <p><b>One gap to be aware of:</b> the {@code <groups>} block of a {@code <test>} is still written
 * inline by {@link #asXml(XmlTest, String)}, so overriding {@link #asXml(XMLStringBuffer,
 * XmlGroups)}, {@link #asXml(XMLStringBuffer, XmlDefine)}, {@link #asXml(XMLStringBuffer, XmlRun)}
 * or {@link #asXml(XMLStringBuffer, XmlDependencies)} affects a suite-level {@code <groups>} but
 * not a test-level one. Routing it through those hooks is not a pure refactoring: group
 * dependencies parsed from a suite file are stored on {@link XmlTest} itself, not on its {@link
 * XmlGroups} -- {@code XmlGroups.setXmlDependencies} is never called by the reader -- so delegating
 * would read them from the empty side and drop them. Unifying the two is model surgery, tracked by
 * #3317 rather than done here.
 *
 * @see IWeaveXml
 */
public class DefaultXmlWeaver implements IWeaveXml {
  // TODO: move constants to XmlSuite?
  /**
   * The name of the TestNG DTD. Must stay in sync with {@code Parser.TESTNG_DTD}, which is the
   * version the reader resolves from the classpath. The two had drifted apart, so the emitted
   * doctype advertised a schema that was never the one used to read the file back. They cannot
   * share a constant: {@code Parser} lives in testng-core, which depends on this module.
   */
  private static final String TESTNG_DTD = "testng-1.1.dtd";

  private static final String HTTPS_TESTNG_DTD_URL = "https://testng.org/" + TESTNG_DTD;

  /** Immutable, so a single instance can serve every {@code asXmlFragment} call. */
  private static final DefaultXmlWeaver LEGACY_FRAGMENT_WEAVER = new DefaultXmlWeaver();

  private final String defaultComment;

  /** Writes the name of each named tag as a trailing XML comment, as TestNG always has. */
  public DefaultXmlWeaver() {
    this(null);
  }

  /**
   * @param defaultComment the comment to close every tag with, or {@code null} to fall back to the
   *     tag's own {@code name} attribute. Pass the empty string to write no comment at all, which
   *     is what {@link CommentDisabledXmlWeaver} does.
   */
  protected DefaultXmlWeaver(String defaultComment) {
    this.defaultComment = defaultComment;
  }

  @Override
  public String asXml(XmlSuite xmlSuite) {
    XMLStringBuffer xsb = new XMLStringBuffer();
    xsb.setDefaultComment(defaultComment);
    xsb.setDocType("suite SYSTEM \"" + HTTPS_TESTNG_DTD_URL + '\"');
    Properties p = new Properties();
    p.setProperty("name", xmlSuite.getName());
    if (xmlSuite.getVerbose() != null) {
      XmlUtils.setProperty(
          p, "verbose", xmlSuite.getVerbose().toString(), DEFAULT_VERBOSE.toString());
    }
    final XmlSuite.ParallelMode parallel = xmlSuite.getParallel();
    if (parallel != null && !XmlSuite.DEFAULT_PARALLEL.equals(parallel)) {
      p.setProperty("parallel", parallel.toString());
    }
    XmlUtils.setProperty(
        p,
        "use-global-thread-pool",
        String.valueOf(xmlSuite.useGlobalThreadPool()),
        DEFAULT_SHARE_THREAD_POOL.toString());
    XmlUtils.setProperty(
        p,
        "share-thread-pool-for-data-providers",
        String.valueOf(xmlSuite.isShareThreadPoolForDataProviders()),
        DEFAULT_SHARE_THREAD_POOL_FOR_DATA_PROVIDERS.toString());
    XmlUtils.setProperty(
        p,
        "group-by-instances",
        String.valueOf(xmlSuite.getGroupByInstances()),
        DEFAULT_GROUP_BY_INSTANCES.toString());
    XmlUtils.setProperty(
        p,
        "configfailurepolicy",
        xmlSuite.getConfigFailurePolicy().toString(),
        DEFAULT_CONFIG_FAILURE_POLICY.toString());
    XmlUtils.setProperty(
        p,
        "thread-count",
        String.valueOf(xmlSuite.getThreadCount()),
        DEFAULT_THREAD_COUNT.toString());
    XmlUtils.setProperty(
        p,
        "data-provider-thread-count",
        String.valueOf(xmlSuite.getDataProviderThreadCount()),
        DEFAULT_DATA_PROVIDER_THREAD_COUNT.toString());
    if (isStringNotEmpty(xmlSuite.getTimeOut())) {
      p.setProperty("time-out", xmlSuite.getTimeOut());
    }
    XmlUtils.setProperty(
        p,
        "skipfailedinvocationcounts",
        xmlSuite.skipFailedInvocationCounts().toString(),
        DEFAULT_SKIP_FAILED_INVOCATION_COUNTS.toString());
    if (null != xmlSuite.getObjectFactoryClass()) {
      p.setProperty("object-factory", xmlSuite.getObjectFactoryClass().getName());
    }
    if (isStringNotEmpty(xmlSuite.getParentModule())) {
      p.setProperty("parent-module", xmlSuite.getParentModule());
    }
    if (isStringNotEmpty(xmlSuite.getGuiceStage())) {
      p.setProperty("guice-stage", xmlSuite.getGuiceStage());
    }
    XmlUtils.setProperty(
        p,
        "allow-return-values",
        String.valueOf(xmlSuite.getAllowReturnValues()),
        DEFAULT_ALLOW_RETURN_VALUES.toString());
    xsb.push("suite", p);

    if (xmlSuite.getGroups() != null) {
      asXml(xsb, xmlSuite.getGroups());
    } else {
      // Only synthesize a <groups> block when the suite has no XmlGroups of its own to write.
      // getIncludedGroups()/getExcludedGroups() read through to that same XmlGroups, so emitting
      // both produced two sibling <groups> elements -- which the DTD allows only once, making
      // TestNG's own output invalid. When the groups come from a parent suite there is nothing
      // else to write, and flattening them here is what keeps a generated suite self-contained.
      List<String> included = xmlSuite.getIncludedGroups();
      List<String> excluded = xmlSuite.getExcludedGroups();
      if (hasElements(included) || hasElements(excluded)) {
        xsb.push("groups");
        xsb.push("run");
        for (String g : included) {
          xsb.addEmptyElement("include", "name", g);
        }
        for (String g : excluded) {
          xsb.addEmptyElement("exclude", "name", g);
        }
        xsb.pop("run");
        xsb.pop("groups");
      }
    }

    dumpParameters(xsb, xmlSuite.getParameters());

    if (hasElements(xmlSuite.getListeners())) {
      xsb.push("listeners");
      for (String listenerName : xmlSuite.getLocalListeners()) {
        Properties listenerProps = new Properties();
        listenerProps.setProperty("class-name", listenerName);
        xsb.addEmptyElement("listener", listenerProps);
      }
      xsb.pop("listeners");
    }

    if (hasElements(xmlSuite.getXmlPackages())) {
      xsb.push("packages");

      for (XmlPackage pack : xmlSuite.getXmlPackages()) {
        asXml(xsb, pack);
      }

      xsb.pop("packages");
    }

    if (xmlSuite.getXmlMethodSelectors() != null) {
      asXml(xsb, xmlSuite.getXmlMethodSelectors());
    } else {
      if (hasElements(xmlSuite.getMethodSelectors())) {
        xsb.push("method-selectors");
        for (XmlMethodSelector selector : xmlSuite.getMethodSelectors()) {
          asXml(xsb, selector);
        }

        xsb.pop("method-selectors");
      }
    }

    List<String> suiteFiles = xmlSuite.getSuiteFiles();
    if (!suiteFiles.isEmpty()) {
      xsb.push("suite-files");
      for (String sf : suiteFiles) {
        Properties prop = new Properties();
        prop.setProperty("path", sf);
        xsb.addEmptyElement("suite-file", prop);
      }
      xsb.pop("suite-files");
    }

    for (XmlTest test : xmlSuite.getTests()) {
      // The last fragment still spliced in rather than woven straight into the buffer:
      // asXml(XmlTest,
      // String) is an IWeaveXml method and has to return a String, so its indent stays explicit.
      //
      // Not test.toXml("  "): that re-resolves the weaver from testng.xml.weaver, so a subclass
      // serializing a suite through its own instance would have every override below <test>
      // silently bypassed -- including the one CommentDisabledXmlWeaver relies on.
      xsb.getStringBuffer().append(asXml(test, "  "));
    }

    xsb.pop("suite");

    return xsb.toXML();
  }

  @Override
  public String asXml(XmlTest xmlTest, String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);
    xsb.setDefaultComment(defaultComment);
    Properties p = new Properties();
    p.setProperty("name", xmlTest.getName());
    XmlUtils.setProperty(
        p, "parallel", xmlTest.getParallel().toString(), XmlSuite.DEFAULT_PARALLEL.toString());
    XmlUtils.setProperty(
        p, "verbose", Integer.toString(xmlTest.getVerbose()), XmlSuite.DEFAULT_VERBOSE.toString());

    if (null != xmlTest.getTimeOut()) {
      p.setProperty("time-out", xmlTest.getTimeOut());
    }

    if (xmlTest.getPreserveOrder() != null
        && !XmlSuite.DEFAULT_PRESERVE_ORDER.equals(xmlTest.getPreserveOrder())) {
      p.setProperty("preserve-order", xmlTest.getPreserveOrder().toString());
    }

    if (xmlTest.getThreadCount() != -1) {
      p.setProperty("thread-count", Integer.toString(xmlTest.getThreadCount()));
    }

    XmlUtils.setProperty(
        p,
        "group-by-instances",
        String.valueOf(xmlTest.getGroupByInstances()),
        XmlSuite.DEFAULT_GROUP_BY_INSTANCES.toString());

    xsb.push("test", p);

    if (null != xmlTest.getMethodSelectors() && !xmlTest.getMethodSelectors().isEmpty()) {
      xsb.push("method-selectors");
      for (XmlMethodSelector selector : xmlTest.getMethodSelectors()) {
        asXml(xsb, selector);
      }

      xsb.pop("method-selectors");
    }

    dumpParameters(xsb, xmlTest.getLocalParameters());

    // groups

    if ((xmlTest.getXmlGroups() != null
            && (!xmlTest.getXmlGroups().getDefines().isEmpty()
                || (xmlTest.getXmlGroups().getRun() != null
                    && (!xmlTest.getXmlGroups().getRun().getIncludes().isEmpty()
                        || !xmlTest.getXmlGroups().getRun().getExcludes().isEmpty()))))
        || !xmlTest.getXmlDependencyGroups().isEmpty()) {
      xsb.push("groups");

      // define
      if (xmlTest.getXmlGroups() != null) {
        for (XmlDefine define : xmlTest.getXmlGroups().getDefines()) {
          Properties metaGroupProp = new Properties();
          metaGroupProp.setProperty("name", define.getName());

          xsb.push("define", metaGroupProp);

          for (String groupName : define.getIncludes()) {
            Properties includeProps = new Properties();
            includeProps.setProperty("name", groupName);

            xsb.addEmptyElement("include", includeProps);
          }

          xsb.pop("define");
        }
      }

      // run
      if ((xmlTest.getXmlGroups() != null && xmlTest.getXmlGroups().getRun() != null)
          && (!xmlTest.getXmlGroups().getRun().getIncludes().isEmpty()
              || !xmlTest.getXmlGroups().getRun().getExcludes().isEmpty())) {
        xsb.push("run");

        for (String includeGroupName : xmlTest.getXmlGroups().getRun().getIncludes()) {
          Properties includeProps = new Properties();
          includeProps.setProperty("name", includeGroupName);

          xsb.addEmptyElement("include", includeProps);
        }

        for (String excludeGroupName : xmlTest.getXmlGroups().getRun().getExcludes()) {
          Properties excludeProps = new Properties();
          excludeProps.setProperty("name", excludeGroupName);

          xsb.addEmptyElement("exclude", excludeProps);
        }

        xsb.pop("run");
      }

      // group dependencies

      if (xmlTest.getXmlDependencyGroups() != null && !xmlTest.getXmlDependencyGroups().isEmpty()) {
        xsb.push("dependencies");
        for (Map.Entry<String, String> entry : xmlTest.getXmlDependencyGroups().entrySet()) {
          xsb.addEmptyElement("group", "name", entry.getKey(), "depends-on", entry.getValue());
        }
        xsb.pop("dependencies");
      }

      xsb.pop("groups");
    }

    if (null != xmlTest.getXmlPackages() && !xmlTest.getXmlPackages().isEmpty()) {
      xsb.push("packages");

      for (XmlPackage pack : xmlTest.getXmlPackages()) {
        asXml(xsb, pack);
      }

      xsb.pop("packages");
    }

    // classes
    if (null != xmlTest.getXmlClasses() && !xmlTest.getXmlClasses().isEmpty()) {
      xsb.push("classes");
      for (XmlClass cls : xmlTest.getXmlClasses()) {
        asXml(xsb, cls);
      }
      xsb.pop("classes");
    }

    xsb.pop("test");

    return xsb.toXML();
  }

  protected void asXml(XMLStringBuffer xsb, XmlGroups xmlGroups) {
    List<XmlDefine> defines = xmlGroups.getDefines();
    XmlRun run = xmlGroups.getRun();
    List<XmlDependencies> dependencies = xmlGroups.getDependencies();
    boolean hasGroups = hasElements(defines) || run != null || hasElements(dependencies);

    if (hasGroups) {
      xsb.push("groups");
    }

    for (XmlDefine d : defines) {
      asXml(xsb, d);
    }

    if (null != run) {
      // XmlRun is optional and is not always available, so check before serializing it.
      asXml(xsb, run);
    }

    for (XmlDependencies d : dependencies) {
      asXml(xsb, d);
    }

    if (hasGroups) {
      xsb.pop("groups");
    }
  }

  protected void asXml(XMLStringBuffer xsb, XmlDefine xmlDefine) {
    List<String> includes = xmlDefine.getIncludes();
    boolean hasElements = hasElements(includes);
    if (hasElements) {
      xsb.push("define", "name", xmlDefine.getName());
    }
    for (String s : includes) {
      xsb.addEmptyElement("include", "name", s);
    }
    if (hasElements) {
      xsb.pop("define");
    }
  }

  protected void asXml(XMLStringBuffer xsb, XmlRun xmlRun) {
    List<String> includes = xmlRun.getIncludes();
    List<String> excludes = xmlRun.getExcludes();
    boolean hasElements = hasElements(excludes) || hasElements(includes);
    if (hasElements) {
      xsb.push("run");
    }
    for (String s : includes) {
      xsb.addEmptyElement("include", "name", s);
    }
    for (String s : excludes) {
      xsb.addEmptyElement("exclude", "name", s);
    }
    if (hasElements) {
      xsb.pop("run");
    }
  }

  protected void asXml(XMLStringBuffer xsb, XmlDependencies xmlDependencies) {
    Map<String, String> groups = xmlDependencies.getDependencies();
    boolean hasElements = hasElements(groups);
    if (hasElements) {
      xsb.push("dependencies");
    }
    for (Map.Entry<String, String> entry : groups.entrySet()) {
      // <!ELEMENT dependencies (group*)>: an <include> here is rejected by the DTD, and the
      // reader only maps <group> (TestNGContentHandler#xmlGroup), so writing one lost the
      // dependency on the way back in.
      xsb.addEmptyElement("group", "name", entry.getKey(), "depends-on", entry.getValue());
    }
    if (hasElements) {
      xsb.pop("dependencies");
    }
  }

  protected void asXml(XMLStringBuffer xsb, XmlPackage xmlPackage) {
    List<String> includes = xmlPackage.getInclude();
    List<String> excludes = xmlPackage.getExclude();

    if (includes.isEmpty() && excludes.isEmpty()) {
      xsb.addEmptyElement("package", "name", xmlPackage.getName());
      return;
    }

    xsb.push("package", "name", xmlPackage.getName());
    for (String m : includes) {
      xsb.addEmptyElement("include", "name", m);
    }
    for (String m : excludes) {
      xsb.addEmptyElement("exclude", "name", m);
    }
    xsb.pop("package");
  }

  protected void asXml(XMLStringBuffer xsb, XmlMethodSelectors xmlMethodSelectors) {
    List<XmlMethodSelector> selectors = xmlMethodSelectors.getMethodSelectors();
    if (hasElements(selectors)) {
      xsb.push("method-selectors");
      for (XmlMethodSelector selector : selectors) {
        asXml(xsb, selector);
      }

      xsb.pop("method-selectors");
    }
  }

  protected void asXml(XMLStringBuffer xsb, XmlMethodSelector xmlMethodSelector) {
    xsb.push("method-selector");

    XmlScript script = xmlMethodSelector.getScript();
    if (null != xmlMethodSelector.getClassName()) {
      Properties clsProp = new Properties();
      clsProp.setProperty("name", xmlMethodSelector.getClassName());
      // Omit the value the parser falls back to when the attribute is absent, so that a
      // round trip is lossless. A negative priority is meaningful (see RunInfo#includeMethod)
      // and must therefore be written out.
      if (xmlMethodSelector.getPriority() != XmlMethodSelector.DEFAULT_PRIORITY) {
        clsProp.setProperty("priority", String.valueOf(xmlMethodSelector.getPriority()));
      }
      xsb.addEmptyElement("selector-class", clsProp);
    } else if (script != null && script.getLanguage() != null) {
      xsb.push("script", "language", script.getLanguage());
      xsb.addCDATA(script.getExpression());
      xsb.pop("script");
    } else {
      throw new TestNGException("Invalid Method Selector:  found neither class name nor language");
    }

    xsb.pop("method-selector");
  }

  protected void asXml(XMLStringBuffer xsb, XmlClass xmlClass) {
    List<XmlInclude> includedMethods = xmlClass.getIncludedMethods();
    List<String> excludedMethods = xmlClass.getExcludedMethods();
    Map<String, String> parameters = xmlClass.getLocalParameters();

    boolean hasMethods = !includedMethods.isEmpty() || !excludedMethods.isEmpty();
    if (parameters.isEmpty() && !hasMethods) {
      xsb.addEmptyElement("class", "name", xmlClass.getName());
      return;
    }

    xsb.push("class", "name", xmlClass.getName());
    dumpParameters(xsb, parameters);

    if (hasMethods) {
      xsb.push("methods");
      for (XmlInclude m : includedMethods) {
        asXml(xsb, m);
      }
      for (String m : excludedMethods) {
        xsb.addEmptyElement("exclude", "name", m);
      }
      xsb.pop("methods");
    }

    xsb.pop("class");
  }

  protected void asXml(XMLStringBuffer xsb, XmlInclude xmlInclude) {
    Properties p = new Properties();
    p.setProperty("name", xmlInclude.getName());
    if (xmlInclude.getDescription() != null) {
      p.setProperty("description", xmlInclude.getDescription());
    }
    List<Integer> invocationNumbers = xmlInclude.getInvocationNumbers();
    if (invocationNumbers != null && !invocationNumbers.isEmpty()) {
      p.setProperty("invocation-numbers", XmlClass.listToString(invocationNumbers));
    }

    Map<String, String> parameters = xmlInclude.getLocalParameters();
    if (!parameters.isEmpty()) {
      xsb.push("include", p);
      dumpParameters(xsb, parameters);
      xsb.pop("include");
    } else {
      xsb.addEmptyElement("include", p);
    }
  }

  /**
   * Writes a {@code <parameter>} element per entry, skipping the ones with a null key or value.
   * Exposed to subclasses because every element that carries parameters needs it.
   *
   * @param xsb the buffer to write into
   * @param parameters the parameters of the element being written
   */
  protected static void dumpParameters(XMLStringBuffer xsb, Map<String, String> parameters) {
    for (Map.Entry<String, String> para : parameters.entrySet()) {
      if (para.getKey() == null) {
        Utils.log("Skipping a null parameter.");
        continue;
      }
      if (para.getValue() == null) {
        Utils.log(
            String.format("Skipping parameter [%s] since it has a null value", para.getKey()));
        continue;
      }
      // BUGFIX: TESTNG-27
      xsb.addEmptyElement("parameter", "name", para.getKey(), "value", para.getValue());
    }
  }

  private static XMLStringBuffer newFragmentBuffer(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);
    xsb.setDefaultComment(LEGACY_FRAGMENT_WEAVER.defaultComment);
    return xsb;
  }

  /**
   * The serializer behind the deprecated {@code toXml(String)} methods that the model classes still
   * expose. Those methods never consulted {@code -Dtestng.xml.weaver}: they built their XML inline,
   * so routing them through {@link XmlWeaver} now would change what a user with a custom weaver
   * gets back. They keep using the default serializer.
   */
  static String asXmlFragment(XmlGroups groups, String indent) {
    XMLStringBuffer xsb = newFragmentBuffer(indent);
    LEGACY_FRAGMENT_WEAVER.asXml(xsb, groups);
    return xsb.toXML();
  }

  static String asXmlFragment(XmlDefine define, String indent) {
    XMLStringBuffer xsb = newFragmentBuffer(indent);
    LEGACY_FRAGMENT_WEAVER.asXml(xsb, define);
    return xsb.toXML();
  }

  static String asXmlFragment(XmlRun run, String indent) {
    XMLStringBuffer xsb = newFragmentBuffer(indent);
    LEGACY_FRAGMENT_WEAVER.asXml(xsb, run);
    return xsb.toXML();
  }

  static String asXmlFragment(XmlDependencies dependencies, String indent) {
    XMLStringBuffer xsb = newFragmentBuffer(indent);
    LEGACY_FRAGMENT_WEAVER.asXml(xsb, dependencies);
    return xsb.toXML();
  }

  static String asXmlFragment(XmlPackage xmlPackage, String indent) {
    XMLStringBuffer xsb = newFragmentBuffer(indent);
    LEGACY_FRAGMENT_WEAVER.asXml(xsb, xmlPackage);
    return xsb.toXML();
  }

  static String asXmlFragment(XmlMethodSelectors selectors, String indent) {
    XMLStringBuffer xsb = newFragmentBuffer(indent);
    LEGACY_FRAGMENT_WEAVER.asXml(xsb, selectors);
    return xsb.toXML();
  }

  static String asXmlFragment(XmlMethodSelector selector, String indent) {
    XMLStringBuffer xsb = newFragmentBuffer(indent);
    LEGACY_FRAGMENT_WEAVER.asXml(xsb, selector);
    return xsb.toXML();
  }

  static String asXmlFragment(XmlClass xmlClass, String indent) {
    XMLStringBuffer xsb = newFragmentBuffer(indent);
    LEGACY_FRAGMENT_WEAVER.asXml(xsb, xmlClass);
    return xsb.toXML();
  }

  static String asXmlFragment(XmlInclude include, String indent) {
    XMLStringBuffer xsb = newFragmentBuffer(indent);
    LEGACY_FRAGMENT_WEAVER.asXml(xsb, include);
    return xsb.toXML();
  }
}
