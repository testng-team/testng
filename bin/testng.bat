set ROOT=c:\java\TestNG
set JAR=%ROOT%\testng-5.2beta-jdk15.jar;%ROOT%\test\build
	rem set JAR=%ROOT%\testng-4.5-jdk14.jar;%ROOT%\test-14\build

java -ea -classpath %ROOT%\3rdparty\junit.jar;%JAVA_HOME%\lib\tools.jar;%JAR%;%CLASSPATH% org.testng.TestNG %1 %2 %3 %4 %5 %6 %7 %8 %9
