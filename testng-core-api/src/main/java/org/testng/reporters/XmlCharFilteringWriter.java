package org.testng.reporters;

import java.io.Writer;

/**
 * A writer that appends into an {@link IBuffer}, dropping the characters XML 1.0 does not allow,
 * without ever holding more than one slice of its input in memory.
 *
 * <p>This is the streaming form of the substitution {@link XMLStringBuffer#toXML()} performs. That
 * one reads the whole buffer back as a {@code String} and runs a regular expression over it, which
 * is exactly what a buffer backed by a temporary file exists to avoid; this one is what {@link
 * XMLStringBuffer#addBuffer(XMLStringBuffer)} uses to fold one buffer into another.
 *
 * <p>The two must agree character for character. A {@code Matcher} walks its input by code point,
 * so a surrogate pair stands for the single character it encodes -- always a legal one, since the
 * whole of U+10000..U+10FFFF is allowed -- while a surrogate that is not part of a pair is a
 * character of its own and an illegal one. A pair can therefore straddle two {@code write} calls,
 * and a high surrogate is held back until the next call says whether it opened a pair or stood
 * alone. {@link #flush()} does not resolve it, for the same reason; {@link #close()} does, and
 * drops it.
 */
class XmlCharFilteringWriter extends Writer {

  /**
   * How much is accumulated before being handed to the buffer.
   *
   * <p>It has to stay below {@code FileStringBuffer.MAX}, which is where that class stops adding to
   * its in-memory builder and opens the temporary file for the one append instead: a slice at or
   * above it would cost two file opens each. Package-private so the tests can put a surrogate pair
   * on this boundary rather than on a copy of the number.
   */
  static final int SLICE = 8192;

  private final IBuffer buffer;
  private final StringBuilder slice = new StringBuilder(SLICE);

  /**
   * A high surrogate whose pair, if any, is in the characters not written yet. Zero when none is
   * held back, which no high surrogate can be. It is never written on its own, so what is held here
   * when the last character has been written is simply dropped.
   */
  private char pendingHighSurrogate;

  XmlCharFilteringWriter(IBuffer buffer) {
    this.buffer = buffer;
  }

  @Override
  public void write(char[] characters, int offset, int length) {
    for (int i = offset; i < offset + length; i++) {
      char c = characters[i];
      if (pendingHighSurrogate != 0) {
        if (Character.isLowSurrogate(c)) {
          // A whole character of the supplementary planes, which is always allowed. Both halves
          // go in before the slice can be handed over: a slice ending between them would reach
          // the buffer as a lone surrogate, and a buffer spilling to its file encodes each append
          // on its own, so the two halves would be written as two '?'.
          slice.append(pendingHighSurrogate).append(c);
          pendingHighSurrogate = 0;
          drainIfFull();
          continue;
        }
        // The high surrogate stood alone, so it is dropped, and c is read on its own terms.
        pendingHighSurrogate = 0;
      }
      if (Character.isHighSurrogate(c)) {
        pendingHighSurrogate = c;
      } else if (isAllowed(c)) {
        slice.append(c);
        drainIfFull();
      }
    }
  }

  /**
   * @return whether XML 1.0 allows this character, for anything but a high surrogate -- a low
   *     surrogate reaching here is one that opened nothing, and is not allowed.
   */
  private static boolean isAllowed(char c) {
    return c == '\t'
        || c == '\n'
        || c == '\r'
        || (c >= 0x0020 && c <= 0xD7FF)
        || (c >= 0xE000 && c <= 0xFFFD);
  }

  private void drainIfFull() {
    if (slice.length() >= SLICE) {
      drain();
    }
  }

  private void drain() {
    if (slice.length() > 0) {
      buffer.append(slice.toString());
      slice.setLength(0);
    }
  }

  @Override
  public void flush() {
    drain();
  }

  @Override
  public void close() {
    drain();
  }
}
