package test.tmp;

import org.testng.annotations.Test;

@Test
public class A extends Base {
    public void a1() {
        throw new RuntimeException();
    }
    
    public void a2() {}

    public void a3() {}
}
