This area provides spring support classes for testng.

They provide the following services:
	- Context caching
	- Dependency Injection for test classes
	- Inherited instance variables useful for testing.
	- Transaction management appropriate to tests.

Please refer to http://static.springframework.org/spring/docs/1.2.x/reference/testing.html for more details.

The code here is based on spring's test support classes found in the org.springframework.test package.

Here is a list of the modifications that were performed:

	1- Renamed package name from org.springframework.test to org.testng.spring.test to avoid conflicts.
	2- Removed dependency on JUnit from AbstractSpringContextTests class
			Removed import org.junit stuff
			Removed extends TestCase
	3- Added javadoc tags in setup and teardown methods of AbstractDependencyInjectionSpringContextTests (jdk14 src tree) 
			to make it compatible with testng.
	4- Added annotations in setup and teardown methods of AbstractDependencyInjectionSpringContextTests (jdk15 src tree) 
			to make it compatible with testng.

