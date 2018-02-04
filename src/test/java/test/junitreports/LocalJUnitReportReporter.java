package test.junitreports;

import org.testng.ISuite;
import org.testng.reporters.JUnitReportReporter;
import org.testng.reporters.XMLReporterConfig;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class LocalJUnitReportReporter extends JUnitReportReporter implements TestsuiteRetriever {
    private List<Testsuite> testsuites = new ArrayList<>();

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, XMLReporterConfig config) {
        super.generateReport(xmlSuites, suites, config);
        String dir = config.getOutputDirectory() + File.separator + "junitreports";
        File directory = new File(dir);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        testsuites.addAll(LocalJUnitXMLReporter.getSuites(files));
    }

    public Testsuite getTestsuite(String name) {
        for (Testsuite suite : testsuites) {
            if (suite.getName().equals(name)) {
                return suite;
            }
        }
        return null;
    }
}
