package test.tmp;

public class B extends A {
	@Override
	public Object createObject() {
    System.out.println("CREATING OBJECT");
		return new Object();
	}
}
