package test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test parameters passed to constructors
 *
 * @author cbeust
 */
public class ParameterConstructorTest {
  private final String m_string;
  private final int m_int;
  private final boolean m_boolean;
  private final byte m_byte;
  private final char m_char;
  private final double m_double;
  private final float m_float;
  private final long m_long;
  private final short m_short;

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
    assertThat("Cedric").isEqualTo(m_string);
    assertThat(42).isEqualTo(m_int);
    assertThat(m_boolean).isTrue();
    assertThat(43).isEqualTo(m_byte);
    assertThat('c').isEqualTo(m_char);
    assertThat(44.0).isCloseTo(m_double, offset(0.1));
    assertThat(45.0f).isCloseTo(m_float, offset(0.1f));
    assertThat(46).isEqualTo(m_long);
    assertThat(47).isEqualTo(m_short);
  }
}
