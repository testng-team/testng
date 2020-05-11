package org.testng;

import org.testng.IReporter;

/**
 * This interface is an extension of the standard {@link IReporter} for reporters with configurations
 * stored in a separate configuration object.
 */
public interface IConfiguredReporter extends IReporter {

    /**
     * Get the reporter configuration object.
     * <p>
     * <b>NOTE</b>: Reporter configuration objects must adhere to the JavaBean object conventions,
     * providing getter and setter methods that conform to standard naming rules. This enables
     * {@link ReporterConfig} to serialize, deserialize, and instantiate the reporter.
     *
     * @return reporter configuration object
     */
    Object getConfig();
}
