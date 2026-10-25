package org.testng;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.Test;

public class CauseChainTest {

  @Test(description = "GITHUB-3570", timeOut = 10_000)
  public void causesThatPointAtEachOtherArePrintedOnce() {
    Exception first = new Exception("first");
    Exception second = new Exception("second");
    first.initCause(second);
    second.initCause(first);

    assertThat(printCauses(new TestNGException("outer", first)))
        .containsSubsequence("Caused by: " + first, "Caused by: " + second)
        .hasSize(2);
  }

  private static List<String> printCauses(Throwable throwable) {
    PrintStream original = System.err;
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    System.setErr(new PrintStream(bytes, true));
    try {
      TestNG.printCauses(throwable);
    } finally {
      System.setErr(original);
    }
    return Arrays.asList(new String(bytes.toByteArray(), StandardCharsets.UTF_8).split("\\R"));
  }
}
