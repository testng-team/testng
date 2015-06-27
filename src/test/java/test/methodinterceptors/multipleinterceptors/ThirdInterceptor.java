package test.methodinterceptors.multipleinterceptors;

import java.util.ArrayList;
import java.util.List;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

public class ThirdInterceptor implements IMethodInterceptor{
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        List<IMethodInstance> result = new ArrayList<IMethodInstance>();
        MultipleInterceptorsTest.interceptors.add(this.getClass());
        for (IMethodInstance method : methods) {
            String name = method.getMethod().getMethodName();
            if (!name.equals("c")) {
                result.add(method);
            }
        }
        return result;
    }
}
