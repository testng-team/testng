package org.testng.collections;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.testng.annotations.Test;

/**
 * Characterization tests: they record what {@link CollectionUtils} does today, including the parts
 * that look like accidents, so that a later move of these methods can be proven behaviour
 * preserving.
 */
public class CollectionUtilsTest {

  @Test
  public void hasElementsIsNullTolerantForCollections() {
    // The null tolerance is the whole reason this method exists rather than !c.isEmpty();
    // DefaultXmlWeaver static-imports it precisely for that.
    assertThat(CollectionUtils.hasElements((List<String>) null)).isFalse();
    assertThat(CollectionUtils.hasElements(Collections.emptyList())).isFalse();
    assertThat(CollectionUtils.hasElements(Collections.singletonList("a"))).isTrue();
  }

  @Test
  public void hasElementsIsNullTolerantForMaps() {
    assertThat(CollectionUtils.hasElements((java.util.Map<String, String>) null)).isFalse();
    assertThat(CollectionUtils.hasElements(Collections.emptyMap())).isFalse();
    assertThat(CollectionUtils.hasElements(Collections.singletonMap("a", "b"))).isTrue();
  }

  @Test
  public void asIterableWrapsTheIteratorWithoutCopyingIt() {
    Iterator<String> iterator = Arrays.asList("a", "b").iterator();

    assertThat(CollectionUtils.asIterable(iterator)).containsExactly("a", "b");
  }

  @Test
  public void asIterableIsSingleShot() {
    // asIterable returns () -> iterator, so every call to iterator() hands back the same, by then
    // exhausted, iterator. The obvious "cleanup" to () -> collection.iterator() would silently make
    // it re-iterable, so pin the current contract.
    Iterable<String> iterable = CollectionUtils.asIterable(Arrays.asList("a", "b").iterator());

    assertThat(iterable).containsExactly("a", "b");
    assertThat(iterable).isEmpty();
  }
}
