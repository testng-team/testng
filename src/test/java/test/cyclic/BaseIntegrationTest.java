package test.cyclic;

import org.testng.annotations.BeforeClass;

public abstract class BaseIntegrationTest {

    @BeforeClass(groups="integration")
    protected void initIntegrationTesting() {
        //...
    }

    @BeforeClass(groups="integration")
    void executeBeforeClassDbOperations() {
        //...
    }

 }

