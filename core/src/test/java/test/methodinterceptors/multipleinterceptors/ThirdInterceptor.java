package test.methodinterceptors.multipleinterceptors;

public class ThirdInterceptor extends MethodNameFilterInterceptor {

    public ThirdInterceptor() {
        super("c");
    }
}
