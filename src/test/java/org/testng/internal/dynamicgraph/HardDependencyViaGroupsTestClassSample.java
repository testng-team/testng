package org.testng.internal.dynamicgraph;

import org.testng.annotations.Test;

public class HardDependencyViaGroupsTestClassSample {
    @Test(groups = "master")
    public void a() {

    }

    @Test(dependsOnGroups = "master")
    public void b() {

    }

}
