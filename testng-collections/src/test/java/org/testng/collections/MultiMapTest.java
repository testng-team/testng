package org.testng.collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.testng.annotations.Test;

/**
 * Characterization tests for the multimap hierarchy.
 *
 * <p>Several of the behaviours pinned here read as bugs -- {@code get} inserts, {@code put} returns
 * the opposite of what Guava's {@code Multimap} returns, {@code isSorted} selects insertion order
 * rather than sorted order. They are recorded rather than fixed because production code depends on
 * them; changing any of them is a separate, deliberate change.
 */
public class MultiMapTest {

  @Test
  public void getInsertsAnEmptyCollectionForAnAbsentKey() {
    // computeIfAbsent, so a read grows the map. DependencyMap.getMethodDependingOn calls get with
    // arbitrary names, so this really happens in production.
    ListMultiMap<String, String> map = Maps.newListMultiMap();

    assertThat(map.get("absent")).isEmpty();

    assertThat(map.size()).isEqualTo(1);
    assertThat(map.containsKey("absent")).isTrue();
    assertThat(map.keySet()).containsExactly("absent");
    assertThat(map.isEmpty()).isFalse();
  }

  @Test
  public void putReturnsFalseForTheFirstValueOfAKey() {
    // put returns "the value was added AND the key already existed", which is the inverse of
    // Guava's Multimap.put contract.
    ListMultiMap<String, String> map = Maps.newListMultiMap();

    assertThat(map.put("k", "v1")).isFalse();
    assertThat(map.put("k", "v2")).isTrue();
    assertThat(map.put("k", "v2")).isTrue();
  }

  @Test
  public void putReturnsFalseWhenTheValueIsRejectedBySetSemantics() {
    SetMultiMap<String, String> map = Maps.newSetMultiMap();

    assertThat(map.put("k", "v")).isFalse();
    assertThat(map.put("k", "v")).isFalse();
    assertThat(map.get("k")).containsExactly("v");
  }

  @Test
  public void putAllWithNoValuesDoesNotCreateTheKey() {
    // The opposite of get: the loop never runs, so nothing is inserted.
    ListMultiMap<String, String> map = Maps.newListMultiMap();

    assertThat(map.putAll("k", Collections.emptyList())).isFalse();

    assertThat(map.containsKey("k")).isFalse();
    assertThat(map.size()).isZero();
  }

  @Test
  public void putAllReportsWhetherAnyValueWasAddedToAnExistingKey() {
    ListMultiMap<String, String> map = Maps.newListMultiMap();

    assertThat(map.putAll("k", Arrays.asList("v1", "v2"))).isTrue();
    assertThat(map.get("k")).containsExactly("v1", "v2");
  }

  @Test
  public void removeOnAnAbsentKeyCreatesIt() {
    // remove goes through get, which inserts.
    ListMultiMap<String, String> map = Maps.newListMultiMap();

    assertThat(map.remove("absent", "v")).isFalse();

    assertThat(map.containsKey("absent")).isTrue();
    assertThat(map.size()).isEqualTo(1);
  }

  @Test
  public void removeAllOnAnAbsentKeyReturnsNull() {
    // Unlike get, which never returns null: removeAll delegates to Map.remove.
    ListMultiMap<String, String> map = Maps.newListMultiMap();

    assertThat(map.removeAll("absent")).isNull();
    assertThat(map.containsKey("absent")).isFalse();
  }

  @Test
  public void keySetIsADefensiveCopy() {
    ListMultiMap<String, String> map = Maps.newListMultiMap();
    map.put("a", "1");

    Set<String> keys = map.keySet();
    map.put("b", "2");

    assertThat(keys).containsExactly("a");
  }

  @Test
  public void entrySetIsALiveView() {
    ListMultiMap<String, String> map = Maps.newListMultiMap();
    map.put("a", "1");

    Set<Map.Entry<String, List<String>>> entries = map.entrySet();
    map.put("b", "2");

    assertThat(entries).hasSize(2);
  }

  @Test
  public void iteratingEntrySetWhileReadingAnAbsentKeyThrows() {
    // DynamicGraphHelper.createInstanceDependencies iterates entrySet() and calls get() inside the
    // loop. That is only safe because get() on a *present* key does not structurally modify. Pin
    // the hazard so that turning entrySet() into a copy cannot quietly hide it.
    ListMultiMap<String, String> map = Maps.newListMultiMap();
    map.put("a", "1");
    map.put("b", "2");

    assertThatThrownBy(
            () -> {
              for (Map.Entry<String, List<String>> ignored : map.entrySet()) {
                map.get("absent");
              }
            })
        .isInstanceOf(java.util.ConcurrentModificationException.class);
  }

  @Test
  public void aSingleEntryHidesTheEntrySetHazard() {
    // HashMap.hasNext() does not check modCount -- only next() does. With one entry the loop ends
    // before the check, so the same code that throws above passes here. Recorded so that a future
    // reader does not conclude the mutating get() is safe under iteration.
    ListMultiMap<String, String> map = Maps.newListMultiMap();
    map.put("a", "1");

    for (Map.Entry<String, List<String>> ignored : map.entrySet()) {
      map.get("absent");
    }

    assertThat(map.size()).isEqualTo(2);
  }

  @Test
  public void valuesIsALiveView() {
    ListMultiMap<String, String> map = Maps.newListMultiMap();
    map.put("a", "1");

    assertThat(map.values()).hasSize(1);
    map.put("b", "2");
    assertThat(map.values()).hasSize(2);
  }

  @Test
  public void sortedMultiMapKeepsInsertionOrderNotSortedOrder() {
    // "sorted" is a misnomer: the flag selects LinkedHashMap over HashMap. DynamicGraphHelper and
    // InstanceBasedParallelParallelWorker both rely on the insertion order this gives.
    ListMultiMap<String, String> map = Maps.newSortedListMultiMap();
    map.put("b", "1");
    map.put("a", "2");
    map.put("c", "3");

    assertThat(map.entrySet()).extracting(Map.Entry::getKey).containsExactly("b", "a", "c");
  }

  @Test
  public void keySetLosesTheInsertionOrderOfASortedMultiMap() {
    // keySet() copies into a HashSet, so the order entrySet() guarantees is not available there.
    ListMultiMap<String, String> map = Maps.newSortedListMultiMap();
    map.put("b", "1");
    map.put("a", "2");
    map.put("c", "3");

    assertThat(map.keySet()).isInstanceOf(java.util.HashSet.class);
  }

  @Test
  public void listMultiMapKeepsDuplicatesAndOrder() {
    ListMultiMap<String, String> map = Maps.newListMultiMap();
    map.putAll("k", Arrays.asList("b", "a", "b"));

    assertThat(map.get("k")).containsExactly("b", "a", "b");
  }

  @Test
  public void setMultiMapDropsDuplicates() {
    SetMultiMap<String, String> map = Maps.newSetMultiMap();
    map.putAll("k", Arrays.asList("b", "a", "b"));

    assertThat(map.get("k")).containsExactlyInAnyOrder("a", "b");
  }

  @Test
  public void toStringRendersOneIndentedLinePerKey() {
    ListMultiMap<String, String> map = Maps.newListMultiMap();
    map.putAll("k", Arrays.asList("v1", "v2"));

    assertThat(map.toString()).isEqualTo("\n    k <-- v1 v2 ");
  }
}
