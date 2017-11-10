package org.testng.internal.dynamicgraph;

import org.testng.annotations.Test;

public class HardDependencyTestClassSample {
    @Test
    public void a() {

    }

    @Test(dependsOnMethods = "a")
    public void b() {

    }

}
