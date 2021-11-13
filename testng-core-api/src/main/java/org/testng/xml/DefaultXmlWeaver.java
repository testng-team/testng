package org.testng.xml;

import static org.testng.collections.CollectionUtils.hasElements;
import static org.testng.internal.Utils.isStringNotEmpty;
import static org.testng.xml.XmlSuite.*;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.testng.reporters.XMLStringBuffer;

/**
 * This class provides String representation of both {@link XmlSuite} and {@link XmlTest} but adds
 * an XML comment as the test name and suite name at the end of the corresponding tags.
 */
class DefaultXmlWeaver implements IWeaveXml {
  // TODO: move constants to XmlSuite?
  /** The name of the TestNG DTD. */
  private static final String TESTNG_DTD = "testng-1.0.dtd";

  private static final String HTTPS_TESTNG_DTD_URL = "https://testng.org/" + TESTNG_DTD;

  private final String defaultComment;

  DefaultXmlWeaver() {
    this(null);
  }

  DefaultXmlWeaver(String defaultComment) {
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
    if (!DEFAULT_JUNIT.equals(xmlSuite.isJUnit())) {
      p.setProperty(
          "junit",
          xmlSuite.isJUnit() != null ? xmlSuite.isJUnit().toString() : "false"); // TESTNG-141
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

    if (xmlSuite.getGroups() != null) {
      xsb.getStringBuffer().append(xmlSuite.getGroups().toXml("  "));
    }

    XmlUtils.dumpParameters(xsb, xmlSuite.getParameters());

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
        xsb.getStringBuffer().append(pack.toXml("    "));
      }

      xsb.pop("packages");
    }

    if (xmlSuite.getXmlMethodSelectors() != null) {
      xsb.getStringBuffer().append(xmlSuite.getXmlMethodSelectors().toXml("  "));
    } else {
      if (hasElements(xmlSuite.getMethodSelectors())) {
        xsb.push("method-selectors");
        for (XmlMethodSelector selector : xmlSuite.getMethodSelectors()) {
          xsb.getStringBuffer().append(selector.toXml("  "));
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
      xsb.getStringBuffer().append(test.toXml("  "));
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
        p, "junit", Boolean.toString(xmlTest.isJUnit()), XmlSuite.DEFAULT_JUNIT.toString());
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
        xsb.getStringBuffer().append(selector.toXml(indent + "    "));
      }

      xsb.pop("method-selectors");
    }

    XmlUtils.dumpParameters(xsb, xmlTest.getLocalParameters());

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
        xsb.getStringBuffer().append(pack.toXml("      "));
      }

      xsb.pop("packages");
    }

    // classes
    if (null != xmlTest.getXmlClasses() && !xmlTest.getXmlClasses().isEmpty()) {
      xsb.push("classes");
      for (XmlClass cls : xmlTest.getXmlClasses()) {
        xsb.getStringBuffer().append(cls.toXml(indent + "    "));
      }
      xsb.pop("classes");
    }

    xsb.pop("test");

    return xsb.toXML();
  }
}
