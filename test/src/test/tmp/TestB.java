package test.tmp;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestB {
    @BeforeClass
    public void insertB() {
        // Inserts an entity 'B'. This is an expensive operation.
        // After this, the entity 'B' is the first row on the table.
    }

    @Test
    public void testGetB() {
        // Checks that 'B' is the first row on the table, and loads it.
    }

    @Test(dependsOnMethods = "testGetB")
    public void testViewB_Details1() {
        // Loads the first row (assumed B) and checks some details
    }

    @Test(dependsOnMethods = "testGetB")
    public void testViewB_Details2() {
        // Loads the first row (assumed B) and checks some details
    }

    @Test(dependsOnMethods = "testGetB")
    public void testViewB_Details3() {
        // Loads the first row (assumed B) and checks some details
    }

    @Test(dependsOnMethods = "testGetB")
    public void testViewB_Details4() {
    }

}
