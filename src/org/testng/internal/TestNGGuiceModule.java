package org.testng.internal;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.internal.Nullable;

import org.testng.IAnnotationTransformer;
import org.testng.IObjectFactory;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;

public class TestNGGuiceModule implements Module {

  private IAnnotationTransformer m_annotationTransformer;
  private IObjectFactory m_objectFactory;

  public TestNGGuiceModule(IAnnotationTransformer transformer, IObjectFactory factory) {
    m_annotationTransformer = transformer;
    m_objectFactory = factory;
  }

  public void configure(Binder binder) {
    binder.bind(IAnnotationFinder.class).to(JDK15AnnotationFinder.class).in(Singleton.class);
    binder.bind(IConfiguration.class).to(Configuration.class).in(Singleton.class);
  }

  @Provides
  IAnnotationTransformer provideAnnotationTransformer() {
    return m_annotationTransformer;
  }

  @Provides
  IObjectFactory provideObjectFactory() {
    return m_objectFactory;
  }
}
