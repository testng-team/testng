package test.reports;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.XMLReporter;
import test.SimpleBaseTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

public class XmlReporterTest extends SimpleBaseTest {
    @Test(description = "GITHUB-1566")
    public void testMethod() throws IOException {
        String suiteName = UUID.randomUUID().toString();
        File fileLocation = createDirInTempDir(suiteName);
        TestNG testng = create(fileLocation.toPath(), Issue1566Sample.class);
        testng.setUseDefaultListeners(true);
        testng.run();
        File file = new File(fileLocation, XMLReporter.FILE_NAME);
        boolean flag = false;
        Pattern pattern = Pattern.compile("\\p{Cc}");
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (pattern.matcher(line).find()) {
                    flag = true;
                }
            }
        }
        Assert.assertFalse(flag, "Should not have found a control character");
    }
}
