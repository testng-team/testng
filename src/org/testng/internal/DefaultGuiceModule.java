package org.testng.internal;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;

import org.testng.IAnnotationTransformer;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;

public class DefaultGuiceModule implements Module {

  public void configure(Binder binder) {
    binder.bind(IAnnotationFinder.class).to(JDK15AnnotationFinder.class).in(Singleton.class);
    binder.bind(IAnnotationTransformer.class).to(DefaultAnnotationTransformer.class)
        .in(Singleton.class);
  }
}
