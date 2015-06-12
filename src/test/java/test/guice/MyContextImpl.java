package test.guice;


public class MyContextImpl implements MyContext {
  private final MySession mySession = new MySession();

  @Override
  public MySession getSession() {
    return mySession;
  }
}
