package test.methodinterceptors.multipleinterceptors;

import java.util.ArrayList;
import java.util.List;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

public class FirstInterceptor implements IMethodInterceptor{
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        MultipleInterceptorsTest.interceptors.add(this.getClass());
        List<IMethodInstance> result = new ArrayList<IMethodInstance>();

        for (IMethodInstance method : methods) {
            String name = method.getMethod().getMethodName();
            if (!name.equals("a")) {
                result.add(method);
            }
        }
        return result;
    }
}
