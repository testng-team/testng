package test.tmp;

import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.List;

@Test
public class A {
  public void f() {
    Object a = null;
    Object b = null;
    if (a==b) {}
  }
  public void g() {
  }

  public static List<String> taco(String s) {
    List<String> result = Lists.newArrayList();
    if (s.length() == 1) {
      result.add(s.toLowerCase());
      result.add(s.toUpperCase());
    } else {
      List<String> shorter = taco(s.substring(1));
      for (String ss : shorter) {
        result.add(Character.toLowerCase(s.charAt(0)) + ss);
        result.add(Character.toUpperCase(s.charAt(0)) + ss);
      }
    }
    return result;
  }

  public static void main(String[] args) {
    System.out.println(taco("taco"));
  }
}