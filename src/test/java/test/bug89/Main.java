package test.bug89;

import org.testng.TestNG;


public class Main {
	public static void main(String[] args) {
		  withOutXml();
		  withXml();
	}
	
	@SuppressWarnings("rawtypes")
	public static void withOutXml(){
		   TestNG runTest = new TestNG();
		   Class [] tests = new Class[1];
		   tests[0]=C.class;
		   runTest.setTestClasses(tests);
		   runTest.setParallel("classes");
		   runTest.setVerbose(5);
		   runTest.run();
	}
	
	@SuppressWarnings("static-access")
	public static void withXml(){
		   TestNG runTest = new TestNG();
		   String args[]={"src/test/java/luis/samples/testng.xml"};
		   runTest.main(args);
	}
	
}