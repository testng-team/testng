package test.custom;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.testng.IAnnotationTransformer;
import org.testng.TestNG;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class CustomAttributeTest extends SimpleBaseTest {

  @Test
  public void ensureCustomAttributesAreAvailable() {
    runTest(null, "joy", Arrays.asList("KingFisher", "Bira"));
  }

  @Test
  public void ensureCustomAttributesCanBeAlteredViaAnnotationTransformer() {
    runTest(new CustomAttributesTransformer(), "sorrow", Arrays.asList("Coffee", "Tea"));
  }

  private static void runTest(IAnnotationTransformer transformer, String key, List<String> values) {
    TestNG testng = create(TestClassSample.class);
    CustomAttributesListener listener = new CustomAttributesListener();
    testng.addListener(listener);
    if (transformer != null) {
      testng.addListener(transformer);
    }
    testng.run();
    List<CustomAttribute> attributes = listener.getAttributes();
    assertThat(attributes).hasSize(1);
    CustomAttribute attribute = attributes.get(0);
    assertThat(attribute.name()).isEqualTo(key);
    assertThat(attribute.values()).containsAll(values);
  }
}
