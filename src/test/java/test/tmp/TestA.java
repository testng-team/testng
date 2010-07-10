package test.tmp;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestA {
    @BeforeClass
    public void insertA() {
        // Inserts an entity 'A'. This is an expensive operation.
        // After this, the entity 'A' is the first row on the table.
    }

    @Test
    public void testGetA() {
        // Checks that 'A' is the first row on the table, and loads it.
    }

    @Test(dependsOnMethods = "testGetA")
    public void testViewA_Details1() {
        // Loads the first row (assumed A) and checks some details
    }

    @Test(dependsOnMethods = "testGetA")
    public void testViewA_Details2() {
        // Loads the first row (assumed A) and checks some details
    }

    @Test(dependsOnMethods = "testGetA")
    public void testViewA_Details3() {
        // Loads the first row (assumed A) and checks some details
    }

    @Test(dependsOnMethods = "testGetA")
    public void testViewA_Details4() {
        // Loads the first row (assumed A) and checks some details
    }
}
