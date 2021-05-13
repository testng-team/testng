package test.xml;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;
import org.testng.reporters.Buffer;
import org.testng.reporters.IBuffer;
import org.testng.reporters.XMLStringBuffer;

import java.util.Properties;

import static org.testng.reporters.XMLStringBuffer.EOL;

public class XMLStringBufferTest {

    @Test
    public void testMethod() {
        IBuffer result = Buffer.create();
        XMLStringBuffer sb = new XMLStringBuffer(result, "");

        sb.push("family");
        Properties p = new Properties();
        p.setProperty("prop1", "value1");
        p.setProperty("prop2", "value2");
        sb.addRequired("cedric", "true", p);
        sb.addRequired("alois", "true");
        sb.addOptional("anne-marie", (String) null);
        sb.pop();
        String expected = "<family>" + EOL +
                "  <cedric prop2=\"value2\" prop1=\"value1\">true</cedric>" + EOL +
                "  <alois>true</alois>" + EOL +
                "</family>";
        assertThat(result.toString().trim()).isEqualTo(expected);
    }

}
