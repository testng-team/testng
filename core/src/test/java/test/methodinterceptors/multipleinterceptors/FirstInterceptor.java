package test.methodinterceptors.multipleinterceptors;

public class FirstInterceptor extends MethodNameFilterInterceptor {

    public FirstInterceptor() {
        super("a");
    }
}
