package test.duplicate;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by EasonYi
 */
public interface Id {
  AtomicInteger ID = new AtomicInteger(1);
}
