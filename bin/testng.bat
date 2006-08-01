set ROOT=c:\java\TestNG
set VERSION=4.7beta
set JAR=%ROOT%\testng-%VERSION%-jdk15.jar;%ROOT%\test\test.jar
	rem set JAR=%ROOT%\testng-%VERSION%-jdk15.jar;%ROOT%\test\build
	rem set JAR=%ROOT%\testng-%VERSION%-jdk14.jar;%ROOT%\test-14\build

java -ea -classpath %ROOT%\3rdparty\junit.jar;%JAVA_HOME%\lib\tools.jar;%JAR%;%CLASSPATH% org.testng.TestNG %1 %2 %3 %4 %5 %6 %7 %8 %9
