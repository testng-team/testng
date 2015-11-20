package org.testng;

import org.testng.xml.XmlSuite;

import java.util.List;

/**
 * Implementations of this interface will gain access to the {@link XmlSuite} object and thus let users be able to
 * alter a suite or a test based on their own needs.
 * This listener can be added ONLY via the following two ways :
 * <ol>
 * <li>&lt;<code>listeners</code>&gt; tag in a suite file.</li>
 * <li>via Service loaders</li>
 * </ol>
 * <p/>
 * <b>Note: </b>This listener <b><u>will NOT be invoked</u></b> if it is wired in via the &#064;<code>Listeners</code>
 * annotation.
 */
public interface IAlterSuiteListener extends ITestNGListener {
    /**
     * @param suites - The list of {@link XmlSuite}s that are part of the current execution.
     */
    void alter(List<XmlSuite> suites);

}
