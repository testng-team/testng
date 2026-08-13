package org.testng.collections;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import org.testng.annotations.Test;

/**
 * Characterization tests for the {@link Lists} members that have no JDK equivalent: the two {@code
 * merge} overloads and {@code intersection}. The plain factories are not covered -- they delegate
 * straight to a JDK constructor.
 */
public class ListsTest {

  /** Asymmetric on purpose: a symmetric predicate would not catch an argument-order flip. */
  private static final BiPredicate<String, String> EXISTING_STARTS_WITH_CANDIDATE =
      String::startsWith;

  @Test
  public void mergeConcatenatesInOrderAndKeepsDuplicates() {
    List<String> merged = Lists.merge(Arrays.asList("a", "b"), Arrays.asList("b", "c"));

    assertThat(merged).containsExactly("a", "b", "b", "c");
  }

  @Test
  public void mergeReturnsAMutableCopyAndLeavesTheInputsAlone() {
    List<String> first = Lists.newArrayList("a");
    List<String> second = Lists.newArrayList("b");

    List<String> merged = Lists.merge(first, second);
    merged.add("c");

    assertThat(first).containsExactly("a");
    assertThat(second).containsExactly("b");
    assertThat(merged).containsExactly("a", "b", "c");
  }

  @Test
  public void conditionalMergeAppendsOnlyTheItemsNoExistingItemMatches() {
    List<String> merged =
        Lists.merge(
            Lists.newArrayList("ab"), EXISTING_STARTS_WITH_CANDIDATE, Arrays.asList("a", "xy"));

    // "a" is dropped because the existing "ab" starts with it; "xy" is appended.
    assertThat(merged).containsExactly("ab", "xy");
  }

  @Test
  public void conditionalMergeCallsTheConditionWithTheExistingItemFirst() {
    // Same data as above with the predicate flipped: nothing is dropped, which is only true if the
    // existing item is the first argument.
    List<String> merged =
        Lists.merge(
            Lists.newArrayList("ab"),
            (existing, candidate) -> candidate.startsWith(existing),
            Arrays.asList("a", "xy"));

    assertThat(merged).containsExactly("ab", "a", "xy");
  }

  @Test
  public void conditionalMergeDeduplicatesAgainstTheGrowingResult() {
    // "xy" is appended by the first vararg list, then filtered out of the second one.
    List<String> merged =
        Lists.merge(
            Lists.<String>newArrayList(),
            String::equals,
            Arrays.asList("xy"),
            Arrays.asList("xy", "z"));

    assertThat(merged).containsExactly("xy", "z");
  }

  @Test
  public void conditionalMergeKeepsDuplicatesAlreadyPresentInTheFirstList() {
    // Only incoming items are filtered; the seed list is copied verbatim.
    List<String> merged =
        Lists.merge(Arrays.asList("a", "a"), String::equals, Collections.singletonList("a"));

    assertThat(merged).containsExactly("a", "a");
  }

  @Test
  public void conditionalMergeWithoutAnyListReturnsACopyOfTheFirstList() {
    List<String> seed = Arrays.asList("a", "b");

    List<String> merged = Lists.merge(seed, String::equals);

    assertThat(merged).containsExactly("a", "b").isNotSameAs(seed);
  }

  @Test
  public void intersectionFiltersTheFirstListAndKeepsItsDuplicates() {
    // A filter, not a set operation: order follows the first list and its duplicates survive.
    List<String> intersection =
        Lists.intersection(Arrays.asList("b", "a", "b", "c"), Arrays.asList("a", "b"));

    assertThat(intersection).containsExactly("b", "a", "b");
  }

  @Test
  public void intersectionOfDisjointListsIsEmpty() {
    assertThat(Lists.intersection(Arrays.asList("a"), Arrays.asList("b"))).isEmpty();
  }
}
