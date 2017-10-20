package org.testng.internal;

import org.testng.IResultMap;
import org.testng.ITestContext;

import java.util.BitSet;

/**
 * |---------------------|---------|--------|-------------|------------------------------------------|
 * | FailedWithinSuccess | Skipped | Failed | Status Code | Remarks                                  |
 * |---------------------|---------|--------|-------------|------------------------------------------|
 * | 0                   | 0       | 0      | 0           | Passed tests                             |
 * | 0                   | 0       | 1      | 1           | Failed tests                             |
 * | 0                   | 1       | 0      | 2           | Skipped tests                            |
 * | 0                   | 1       | 1      | 3           | Skipped/Failed tests                     |
 * | 1                   | 0       | 0      | 4           | FailedWithinSuccess tests                |
 * | 1                   | 0       | 1      | 5           | FailedWithinSuccess/Failed tests         |
 * | 1                   | 1       | 0      | 6           | FailedWithinSuccess/Skipped tests        |
 * | 1                   | 1       | 1      | 7           | FailedWithinSuccess/Skipped/Failed tests |
 * |---------------------|---------|--------|-------------|------------------------------------------|
 */
public class ExitCode {

    public static final int HAS_NO_TEST = 8;
    private static final int FAILED_WITHIN_SUCCESS = 4;
    private static final int SKIPPED = 2;
    private static final int FAILED = 1;
    private static final int SIZE = 3;

    private final BitSet exitCodeBits;

    ExitCode() {
        this(new BitSet(SIZE));
    }

    public static boolean hasFailureWithinSuccessPercentage(int returnCode) {
        return (returnCode & FAILED_WITHIN_SUCCESS) == FAILED_WITHIN_SUCCESS;
    }

    public static boolean hasSkipped(int returnCode) {
        return (returnCode & SKIPPED) == SKIPPED;
    }

    public static boolean hasFailure(int returnCode) {
        return (returnCode & FAILED) == FAILED;
    }

    public static ExitCode newExitCodeRepresentingFailure() {
        BitSet bitSet = new BitSet(SIZE);
        bitSet.set(0, true);
        bitSet.set(1, false);
        bitSet.set(2, false);

        return new ExitCode(bitSet);
    }

    private ExitCode(BitSet exitCodeBits) {
        this.exitCodeBits = exitCodeBits;
    }

    void computeAndUpdate(ITestContext context) {
        computeAndUpdate(0, context.getFailedTests(), context.getFailedConfigurations());
        computeAndUpdate(1, context.getSkippedTests(), context.getSkippedConfigurations());
        computeAndUpdate(2, context.getFailedButWithinSuccessPercentageTests(), null);
    }

    private void computeAndUpdate(int index, IResultMap testResults, IResultMap configResults) {
        boolean containsResults = testResults.size() != 0;
        if (configResults != null) {
            containsResults = containsResults || configResults.size() != 0;
        }
        if (containsResults) {
            this.exitCodeBits.set(index);
        }
    }

    public boolean hasFailure() {
        return exitCodeBits.get(0);
    }

    public boolean hasSkip() {
        return exitCodeBits.get(1);
    }

    public boolean hasFailureWithinSuccessPercentage() {
        return exitCodeBits.get(2);
    }

    public int getExitCode() {
        int exitCode = 0;
        for (int i = 0; i < exitCodeBits.length(); i++) {
            if (exitCodeBits.get(i)) {
                exitCode = exitCode | (1 << i);
            }
        }

        return exitCode;
    }
}
