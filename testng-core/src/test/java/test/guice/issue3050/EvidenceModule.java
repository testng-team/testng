package test.guice.issue3050;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.UUID;

public class EvidenceModule extends AbstractModule {

  @Provides
  @Singleton
  private UUID randomUUID() {
    return UUID.randomUUID();
  }
}
