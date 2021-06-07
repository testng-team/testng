package test.custom;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.ITestAnnotation;

public class CustomAttributesTransformer implements IAnnotationTransformer {

  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    CustomAttribute[] attributes =
        new CustomAttribute[] {new MoreAttribute("sorrow", new String[] {"Coffee", "Tea"})};
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
  }
}
