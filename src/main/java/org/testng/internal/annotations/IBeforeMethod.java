package org.testng.internal.annotations;

public interface IBeforeMethod extends IBaseBeforeAfter {

    /**
     * The list of groups the test method must belong to one of which.
     */
    public String[] getGroupFilters();

}
