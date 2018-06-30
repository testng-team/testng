package test.objectfactory.github1827;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class GitHub1827Sample {

    private final int value;

    public GitHub1827Sample(int value) {
        this.value = value;
    }

    @Test
    public void test() {
        assertEquals(value, 1);
    }
}
