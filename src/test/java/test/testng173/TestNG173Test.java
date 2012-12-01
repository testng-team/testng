package test.testng173;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

public class TestNG173Test extends SimpleBaseTest {

	@Test
	public void orderShouldBePreservedInMethodsWithSameNameAndInDifferentClasses() {
		TestNG tng = create();
		XmlSuite s = createXmlSuite("PreserveOrder");
		XmlTest t = new XmlTest(s);

		t.getXmlClasses().add(new XmlClass("test.testng173.ClassA"));
		t.getXmlClasses().add(new XmlClass("test.testng173.ClassB"));

		t.setPreserveOrder("true");

		tng.setXmlSuites(Arrays.asList(s));

		TestListenerAdapter tla = new TestListenerAdapter();
		tng.addListener(tla);
		tng.run();

		// bug
		//verifyPassedTests(tla, "test1", "test2", "testX", "test1", "test2");

		// Proposed fix
		verifyPassedTests(tla, "test1", "test2", "testX", "test2", "test1");
	}

	@Test
	public void orderShouldBePreservedInMethodsWithSameNameAndInDifferentClassesAndDifferentPackage() {
		TestNG tng = create();
		XmlSuite s = createXmlSuite("PreserveOrder");
		XmlTest t = new XmlTest(s);

		t.getXmlClasses().add(new XmlClass("test.testng173.ClassA"));
		t.getXmlClasses().add(new XmlClass("test.testng173.anotherpackage.ClassC"));

		t.setPreserveOrder("true");

		tng.setXmlSuites(Arrays.asList(s));

		TestListenerAdapter tla = new TestListenerAdapter();
		tng.addListener(tla);
		tng.run();

		// bug
		//verifyPassedTests(tla, "test1", "test2", "testX", "test1", "test2");

		verifyPassedTests(tla, "test1", "test2", "testX", "test2", "test1");
	}

}
