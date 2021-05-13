# This script will update serviceloader.jar with the latest version of TmpSuiteListener.class,
# which is used by test.serviceloader.ServiceLoaderTest.
# Run this script after building TestNG and its tests with ant

j=${PWD}
rm -rf /tmp/sl
mkdir /tmp/sl
cd /tmp/sl
jar xvf ${j}/serviceloader.jar
echo "test.serviceloader.TmpSuiteListener" >META-INF/services/org.testng.ITestNGListener
cp ${j}/../../../target/test-classes/test/serviceloader/TmpSuiteListener.class test/tmp
jar cvf ${j}/serviceloader.jar .
