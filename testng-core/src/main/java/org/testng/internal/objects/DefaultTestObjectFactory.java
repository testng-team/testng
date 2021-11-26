package org.testng.internal.objects;

import org.testng.ITestObjectFactory;

/**
 * Intended to be the default way of instantiating objects within TestNG. Intentionally does not
 * provide any specific implementation because the interface already defines the default behavior.
 * This class still exists to ensure that we dont use an anonymous object instantiation so that its
 * easy to find out what type of object factory is being injected into our object dispensing
 * mechanisms.
 */
public class DefaultTestObjectFactory implements ITestObjectFactory {}
