package test.guice;

import com.google.inject.Provider;

public class MyServiceProvider implements Provider<MyService> {

  @Override
  public MyService get() {
    return new MyService() {
      @Override
      public void serve(MySession mySession) {
      }
    };
  }
}
