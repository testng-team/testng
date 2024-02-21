package test.custom;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.ITestAnnotation;

public class CustomAttributesTransformer implements IAnnotationTransformer {

  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    CustomAttribute[] attributes =
        new CustomAttribute[] {new MoreAttribute("sorrow", "Coffee", "Tea")};
    annotation.setAttributes(attributes);
  }

  public static class MoreAttribute implements CustomAttribute {

    private final String key;
    private final String[] values;

    public MoreAttribute(String key, String... values) {
      this.key = key;
      this.values = values;
    }

    @Override
    public String name() {
      return key;
    }

    @Override
    public String[] values() {
      return values;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return CustomAttribute.class;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MoreAttribute that = (MoreAttribute) o;
      return Objects.equals(key, that.key) && Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
      int result = Objects.hash(key);
      result = 31 * result + Arrays.hashCode(values);
      return result;
    }
  }
}
