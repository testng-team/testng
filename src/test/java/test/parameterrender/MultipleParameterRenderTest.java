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
//public class MultipleParameterRenderTest {
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
//    public void doStuff2(@ParameterOverride(parameterRender = "render1") int id,
//                         @ParameterOverride(parameterRender = "render2") String s,
//                         @ParameterOverride(parameterRender = "render3") Object[] array) {
//        System.out.println(id + "," + s);
//        Assert.assertNotEquals(s, "[GUID]");
//
//        System.out.println("printing array...");
//        for (Object o : array) {
//            System.out.println(o.toString());
//            Assert.assertNotEquals(o.toString(), "[GUID]");
//        }
//    }
//
//    @ParameterRender(name = "render1")
//    public int render1(int i) {
//        return i * 10;
//    }
//
//    @ParameterRender(name = "render2")
//    public String render2(String o) {
//        if (o.equals("[GUID]")) {
//            return UUID.randomUUID().toString();
//        } else {
//            return o;
//        }
//    }
//
//    @ParameterRender(name = "render3")
//    public Object[] render3(Object[] array) {
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
//        return new Object[][] {
//                new Object[] { 1, "abcd", new Object[] { "[www]", "[GUID]", "iii" } },
//                new Object[] { 2, "[GUID]", new Object[] { "ttt" } } };
//    }
//
//}
