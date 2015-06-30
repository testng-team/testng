package test.methodinterceptors.multipleinterceptors;

public class SecondInterceptor extends MethodNameFilterInterceptor {

    public SecondInterceptor() {
        super("b");
    }
}
