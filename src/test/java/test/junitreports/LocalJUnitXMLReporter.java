package test.junitreports;

import org.testng.ITestContext;
import org.testng.reporters.JUnitXMLReporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class LocalJUnitXMLReporter extends JUnitXMLReporter implements TestsuiteRetriever {
    private List<Testsuite> testsuites = new ArrayList<>();

    protected void generateReport(ITestContext context) {
        super.generateReport(context);
        String dir = context.getOutputDirectory();
        File directory = new File(dir);
        File[] files = directory.listFiles((dir1, name) -> name.endsWith(".xml"));
        testsuites.addAll(getSuites(files));
    }

    public Testsuite getTestsuite(String name) {
        for (Testsuite suite : testsuites) {
            if (suite.getName().equals(name)) {
                return suite;
            }
        }
        return null;
    }

    static List<Testsuite> getSuites(File[] files) {
        List<Testsuite> testsuites = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                TestsuiteXmlParser parser = new TestsuiteXmlParser();
                try {
                    testsuites.add(parser.parse("", new FileInputStream(file), false));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return testsuites;
    }
}
