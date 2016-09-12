package org.testng.annotations;

import org.testng.internal.annotations.IDataProvidable;

import java.util.List;

/**
 * Encapsulate the @Factory / @testng.factory annotation
 *
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IFactoryAnnotation extends IParameterizable, IDataProvidable {

    List<Integer> getIndices();
    void setIndices(List<Integer> indices);
}
