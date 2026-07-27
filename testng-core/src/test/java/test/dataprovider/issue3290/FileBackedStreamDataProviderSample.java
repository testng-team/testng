package test.dataprovider.issue3290;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Exercises a genuinely resource-backed {@link Stream} (one produced by {@link Files#lines(Path)},
 * which keeps a file handle open) to prove that lazily-loaded, resource-backed streams work end to
 * end and are closed once consumed. {@link #CLOSE_COUNT} is asserted by the driving test.
 */
public class FileBackedStreamDataProviderSample {

  public static final AtomicInteger CLOSE_COUNT = new AtomicInteger(0);

  @DataProvider
  public Stream<Object[]> data() {
    try {
      Path file = Files.createTempFile("dp-stream", ".csv");
      file.toFile().deleteOnExit();
      Files.write(file, Arrays.asList("Jack,5", "Joe,10"));
      // Files.lines keeps the file handle open until the stream is closed; onClose lets the test
      // confirm our pipeline releases it.
      return Files.lines(file)
          .map(line -> line.split(","))
          .map(parts -> new Object[] {parts[0], Integer.parseInt(parts[1])})
          .onClose(CLOSE_COUNT::incrementAndGet);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Test(dataProvider = "data")
  public void testMethod(String name, int age) {
    assertThat(name).isNotNull();
    assertThat(age).isPositive();
  }
}
