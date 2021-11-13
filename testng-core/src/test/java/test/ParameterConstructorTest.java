package test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test parameters passed to constructors
 *
 * @author cbeust
 */
public class ParameterConstructorTest {
  private String m_string;
  private int m_int;
  private boolean m_boolean;
  private byte m_byte;
  private char m_char;
  private double m_double;
  private float m_float;
  private long m_long;
  private short m_short;

  @Parameters({"string", "int", "boolean", "byte", "char", "double", "float", "long", "short"})
  public ParameterConstructorTest(
      String s, int i, boolean bo, byte b, char c, double d, float f, long l, short sh) {
    m_string = s;
    m_int = i;
    m_boolean = bo;
    m_byte = b;
    m_char = c;
    m_double = d;
    m_float = f;
    m_long = l;
    m_short = sh;
  }

  @Test
  public void verify() {
    assertEquals("Cedric", m_string);
    assertEquals(42, m_int);
    assertTrue(m_boolean);
    assertEquals(43, m_byte);
    assertEquals('c', m_char);
    assertEquals(44.0, m_double, 0.1);
    assertEquals(45.0f, m_float, 0.1);
    assertEquals(46, m_long);
    assertEquals(47, m_short);
  }
}
