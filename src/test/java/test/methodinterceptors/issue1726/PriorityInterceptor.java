package test.methodinterceptors.issue1726;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

public class PriorityInterceptor implements IMethodInterceptor {

    public List<IMethodInstance> intercept(List<IMethodInstance> methods,
                                           ITestContext context) {
        Comparator<IMethodInstance> comparator = Comparator.comparingInt(PriorityInterceptor::getPriority);
        methods.sort(comparator);
        return methods;
    }

    private static int getPriority(IMethodInstance mi) {
        int result = 0;
        Method method = mi.getMethod().getConstructorOrMethod().getMethod();
        Priority a1 = method.getAnnotation(Priority.class);
        if (a1 != null) {
            return a1.value();
        }
        Class<?> cls = method.getDeclaringClass();
        Priority classPriority = cls.getAnnotation(Priority.class);
        if (classPriority != null) {
            result = classPriority.value();
        }
        return result;
    }

}