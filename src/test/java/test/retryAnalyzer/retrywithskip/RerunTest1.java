package test.retryAnalyzer.retrywithskip;

import org.testng.SkipWithRetryCheckException;
import org.testng.annotations.Test;

public class RerunTest1 {   
    
    @Test
    public void test1() {        
        throw new SkipWithRetryCheckException("", false);        
    }
    
    @Test
    public void test2() {        
        throw new SkipWithRetryCheckException("");        
    }

}
