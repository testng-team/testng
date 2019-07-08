package test.retryAnalyzer.retrywithskip;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.annotations.DisabledRetryAnalyzer;

public class AddReryWithTransformer implements IAnnotationTransformer{
    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        Class<? extends IRetryAnalyzer> retry = annotation.getRetryAnalyzerClass();
        if (retry.getSimpleName().equals(DisabledRetryAnalyzer.class.getSimpleName())) {
            annotation.setRetryAnalyzer(SimpleRetryAnalyzer.class);
        }
    }
}
