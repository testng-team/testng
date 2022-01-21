package test.attributes.samples.custom;

import org.testng.annotations.CustomAttribute;
import org.testng.annotations.Test;

class TestClassSample {

    @Test(
        attributes = [
            CustomAttribute(
                name = "joy",
                values = ["KingFisher", "Bira"]
            )
        ]
    )
    fun testMethod() {
    }
}
