package org.testng.internal.collections;

import java.util.ArrayList;
import java.util.List;

public final class Ints {

    private Ints() {
        throw new AssertionError();
    }

    public static List<Integer> asList(int... ints) {
        ArrayList<Integer> result = new ArrayList<>(ints.length);
        for (int val : ints) {
            result.add(val);
        }
        return result;
    }
}
