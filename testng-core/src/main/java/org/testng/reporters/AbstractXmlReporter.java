package org.testng.reporters;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.Utils;
import org.testng.util.TimeUtils;

public abstract class AbstractXmlReporter implements IReporter, ICustomizeXmlReport {

  private final XMLReporterConfig config = new XMLReporterConfig();

  public String fileName() {
    return RuntimeBehavior.getDefaultFileNameForXmlReports();
  }

  @Override
  public XMLReporterConfig getConfig() {
    return config;
  }

  @Override
  public void addCustomTagsFor(XMLStringBuffer xmlBuffer, ITestResult testResult) {}

  protected final void writeReporterOutput(XMLStringBuffer xmlBuffer) {
    writeReporterOutput(xmlBuffer, Reporter.getOutput());
  }

  protected final void writeReporterOutput(XMLStringBuffer xmlBuffer, List<String> output) {
    // TODO: Cosmin - maybe a <line> element isn't indicated for each line
    xmlBuffer.push(XMLReporterConfig.TAG_REPORTER_OUTPUT);
    for (String line : output) {
      if (line != null) {
        xmlBuffer.push(XMLReporterConfig.TAG_LINE);
        xmlBuffer.addCDATA(line);
        xmlBuffer.pop();
      }
    }
    xmlBuffer.pop();
  }

  protected final void writeSuite(XMLStringBuffer rootBuffer, ISuite suite) {
    switch (config.getFileFragmentationLevel()) {
      case XMLReporterConfig.FF_LEVEL_NONE:
        writeSuiteToBuffer(rootBuffer, suite);
        break;
      case XMLReporterConfig.FF_LEVEL_SUITE:
      case XMLReporterConfig.FF_LEVEL_SUITE_RESULT:
        File suiteFile = referenceSuite(rootBuffer, suite);
        writeSuiteToFile(suiteFile, suite);
        break;
      default:
        throw new AssertionError("Unexpected value: " + config.getFileFragmentationLevel());
    }
  }

  private void writeSuiteToFile(File suiteFile, ISuite suite) {
    XMLStringBuffer xmlBuffer = new XMLStringBuffer();
    writeSuiteToBuffer(xmlBuffer, suite);
    File parentDir = suiteFile.getParentFile();
    boolean ignored = suiteFile.getParentFile().mkdirs();
    if (parentDir.exists() || suiteFile.getParentFile().exists()) {
      Utils.writeUtf8File(parentDir.getAbsolutePath(), fileName(), xmlBuffer.toXML());
    }
  }

  private File referenceSuite(XMLStringBuffer xmlBuffer, ISuite suite) {
    String relativePath = suite.getName() + File.separatorChar + fileName();
    File suiteFile = new File(config.getOutputDirectory(), relativePath);
    Properties attrs = new Properties();
    attrs.setProperty(XMLReporterConfig.ATTR_URL, relativePath);
    xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_SUITE, attrs);
    return suiteFile;
  }

  private void writeSuiteToBuffer(XMLStringBuffer xmlBuffer, ISuite suite) {
    xmlBuffer.push(XMLReporterConfig.TAG_SUITE, getSuiteAttributes(suite));
    writeSuiteGroups(xmlBuffer, suite);

    Map<String, ISuiteResult> results = suite.getResults();
    XMLSuiteResultWriter suiteResultWriter = new XMLSuiteResultWriter(config, this);
    for (Map.Entry<String, ISuiteResult> result : results.entrySet()) {
      suiteResultWriter.writeSuiteResult(xmlBuffer, result.getValue());
    }

    xmlBuffer.pop();
  }

  private Set<ITestNGMethod> getUniqueMethodSet(Collection<ITestNGMethod> methods) {
    return new LinkedHashSet<>(methods);
  }

  private void writeSuiteGroups(XMLStringBuffer xmlBuffer, ISuite suite) {
    xmlBuffer.push(XMLReporterConfig.TAG_GROUPS);
    Map<String, Collection<ITestNGMethod>> methodsByGroups = suite.getMethodsByGroups();
    for (Map.Entry<String, Collection<ITestNGMethod>> entry : methodsByGroups.entrySet()) {
      Properties groupAttrs = new Properties();
      groupAttrs.setProperty(XMLReporterConfig.ATTR_NAME, entry.getKey());
      xmlBuffer.push(XMLReporterConfig.TAG_GROUP, groupAttrs);
      Set<ITestNGMethod> groupMethods = getUniqueMethodSet(entry.getValue());
      for (ITestNGMethod groupMethod : groupMethods) {
        Properties methodAttrs = new Properties();
        methodAttrs.setProperty(XMLReporterConfig.ATTR_NAME, groupMethod.getMethodName());
        methodAttrs.setProperty(XMLReporterConfig.ATTR_METHOD_SIG, groupMethod.toString());
        methodAttrs.setProperty(XMLReporterConfig.ATTR_CLASS, groupMethod.getRealClass().getName());
        xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_METHOD, methodAttrs);
      }
      xmlBuffer.pop();
    }
    xmlBuffer.pop();
  }

  private Properties getSuiteAttributes(ISuite suite) {
    Properties props = new Properties();
    props.setProperty(XMLReporterConfig.ATTR_NAME, suite.getName());

    // Calculate the duration
    Map<String, ISuiteResult> results = suite.getResults();
    Date minStartDate = new Date();
    Date maxEndDate = null;
    // TODO: We could probably optimize this in order not to traverse this twice
    for (Map.Entry<String, ISuiteResult> result : results.entrySet()) {
      ITestContext testContext = result.getValue().getTestContext();
      Date startDate = testContext.getStartDate();
      Date endDate = testContext.getEndDate();
      if (minStartDate.after(startDate)) {
        minStartDate = startDate;
      }
      if (maxEndDate == null || maxEndDate.before(endDate)) {
        maxEndDate = endDate != null ? endDate : startDate;
      }
    }
    // The suite could be completely empty
    if (maxEndDate == null) {
      maxEndDate = minStartDate;
    }
    setDurationAttributes(config, props, minStartDate, maxEndDate);
    return props;
  }

  protected static void setDurationAttributes(
      XMLReporterConfig config, Properties attributes, Date minStartDate, Date maxEndDate) {

    String startTime =
        TimeUtils.formatTimeInLocalOrSpecifiedTimeZone(
            minStartDate.getTime(), config.getTimestampFormat());
    String endTime =
        TimeUtils.formatTimeInLocalOrSpecifiedTimeZone(
            maxEndDate.getTime(), config.getTimestampFormat());
    long duration = maxEndDate.getTime() - minStartDate.getTime();

    attributes.setProperty(XMLReporterConfig.ATTR_STARTED_AT, startTime);
    attributes.setProperty(XMLReporterConfig.ATTR_FINISHED_AT, endTime);
    attributes.setProperty(XMLReporterConfig.ATTR_DURATION_MS, Long.toString(duration));
  }

  protected final Properties writeSummaryCount(Count count) {
    Properties p = new Properties();
    p.put("passed", count.passed);
    p.put("failed", count.failed);
    p.put("skipped", count.skipped);
    if (count.retried > 0) {
      p.put("retried", count.retried);
    }
    p.put("ignored", count.ignored);
    p.put("total", count.total());
    return p;
  }

  protected final Count computeCountForSuite(ISuite s) {
    int passed = 0;
    int failed = 0;
    int skipped = 0;
    int retried = 0;
    int ignored = 0;
    for (ISuiteResult sr : s.getResults().values()) {
      ITestContext testContext = sr.getTestContext();
      passed += testContext.getPassedTests().size();
      failed += testContext.getFailedTests().size();
      int retriedPerTest = 0;
      int skippedPerTest = 0;
      for (ITestResult result : testContext.getSkippedTests().getAllResults()) {
        if (result.wasRetried()) {
          retriedPerTest++;
        } else {
          skippedPerTest++;
        }
      }
      skipped += skippedPerTest;
      retried += retriedPerTest;
      ignored +=
          (int) testContext.getExcludedMethods().stream().filter(ITestNGMethod::isTest).count();
    }
    return Count.Builder.builder()
        .withPassed(passed)
        .withFailed(failed)
        .withSkipped(skipped)
        .withRetried(retried)
        .withIgnored(ignored)
        .build();
  }

  public static class Count {
    private int passed;
    private int failed;
    private int skipped;
    private int retried;
    private int ignored;

    public int total() {
      return passed + failed + skipped + retried + ignored;
    }

    public void add(Count count) {
      this.passed += count.passed;
      this.failed += count.failed;
      this.skipped += count.skipped;
      this.retried += count.retried;
      this.ignored += count.ignored;
    }

    private Count(Builder builder) {
      passed = builder.passed;
      failed = builder.failed;
      skipped = builder.skipped;
      retried = builder.retried;
      ignored = builder.ignored;
    }

    public static final class Builder {
      private int passed;
      private int failed;
      private int skipped;
      private int retried;
      private int ignored;

      private Builder() {}

      public static Builder builder() {
        return new Builder();
      }

      public Builder withPassed(int val) {
        passed = val;
        return this;
      }

      public Builder withFailed(int val) {
        failed = val;
        return this;
      }

      public Builder withSkipped(int val) {
        skipped = val;
        return this;
      }

      public Builder withRetried(int val) {
        retried = val;
        return this;
      }

      public Builder withIgnored(int val) {
        ignored = val;
        return this;
      }

      public Count build() {
        return new Count(this);
      }
    }
  }
}
