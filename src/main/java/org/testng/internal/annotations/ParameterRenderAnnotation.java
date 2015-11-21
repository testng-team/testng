package org.testng.internal.annotations;

import org.testng.annotations.IParameterRenderAnnotation;

/**
 * An implementation of IDataProvider.
 *
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class ParameterRenderAnnotation extends BaseAnnotation implements IParameterRenderAnnotation {
    private String m_name;

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public void setName(String name) {
        m_name = name;
    }
}
