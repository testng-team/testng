package org.testng.reporters;

import org.testng.internal.PropertyUtils;
import org.testng.internal.ReporterConfig;

import java.util.List;

public interface IReporterConfig {

    default void setProperties(List<ReporterConfig.Property> properties) {
        for (ReporterConfig.Property property : properties) {
            setProperty(property.getName(), property.getValue());
        }
    }

    default void setProperty(String name, String value) {
        PropertyUtils.setProperty(this, name, value);
    }
}
