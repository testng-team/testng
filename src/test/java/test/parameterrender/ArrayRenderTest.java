//package test.parameterrender;
//
//import java.util.List;
//import java.util.UUID;
//
//import org.testng.Assert;
//import org.testng.TestNG;
//import org.testng.annotations.DataProvider;
//import org.testng.annotations.ParameterOverride;
//import org.testng.annotations.ParameterRender;
//import org.testng.annotations.Test;
//import org.testng.collections.Lists;
//
//public class ArrayRenderTest {
//
//    public static void main(String[] args) {
//        TestNG testng = new TestNG();
//        List<String> suites = Lists.newArrayList();
//        suites.add("src/test/java/test/parameterrender/testng.xml");
//        testng.setTestSuites(suites);
//        testng.run();
//    }
//
//    @Test(dataProvider = "data")
//    public void doStuff2(@ParameterOverride(parameterRender = "render") Object[] array) {
//        for (Object s : array) {
//            System.out.println(s);
//            Assert.assertNotEquals(s, "[GUID]");
//        }
//    }
//
//    @ParameterRender
//    public Object[] render(Object[] array) {
//        for (int i = 0; i < array.length; i++) {
//            if (array[i].equals("[GUID]")) {
//                array[i] = UUID.randomUUID().toString();
//            }
//        }
//        return array;
//    }
//
//    @DataProvider(name = "data")
//    public Object[][] createData() {
//        return new Object[][] { new Object[] { new Object[] { "abcd", "xyz" } },
//                new Object[] { new Object[] { "[GUID]", "[GUID]", "[GUID]" } }, };
//    }
//
//}
