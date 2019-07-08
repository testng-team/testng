package test.retryAnalyzer.retrywithskip;

import org.testng.SkipWithRetryCheckException;
import org.testng.annotations.Test;

public class RerunTest2 {
    
    @Test
    public void test1() {        
        throw new SkipWithRetryCheckException("", false);        
    }
    
    @Test(retryAnalyzer = SimpleRetryAnalyzer.class)
    public void test2() {        
        throw new SkipWithRetryCheckException("");        
    }
    
    @Test
    public void test3() {        
        throw new SkipWithRetryCheckException("");        
    }

}
