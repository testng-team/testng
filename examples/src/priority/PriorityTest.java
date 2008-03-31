package priority;

import org.testng.annotations.Test;

import priority.Priority;

public class PriorityTest {

  @Test
  public void b1() { System.out.println("Default priority");}
  
  @Priority(-3)
  @Test
  public void a1()  { System.out.println("Priority -3");}
  
  @Priority(-3)
  @Test
  public void a2()  { System.out.println("Priority -3");}

  @Priority(3)
  @Test
  public void c1()  { System.out.println("Priority 3");}

  @Priority(3)
  @Test
  public void c2()  { System.out.println("Priority 3");}

  @Priority(4)
  @Test
  public void d1()  { System.out.println("Priority 4");}
}
