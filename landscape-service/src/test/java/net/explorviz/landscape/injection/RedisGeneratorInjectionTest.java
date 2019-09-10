package net.explorviz.landscape.injection;


import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Properties;
import javax.inject.Inject;
import net.explorviz.landscape.server.main.DependencyInjectionBinder;
import net.explorviz.shared.common.idgen.RedisServiceIdGenerator;
import net.explorviz.shared.common.idgen.ServiceIdGenerator;
import net.explorviz.shared.common.idgen.UuidServiceIdGenerator;
import net.explorviz.shared.config.annotations.injection.ConfigInjectionResolver;
import net.explorviz.shared.config.annotations.injection.ConfigValuesInjectionResolver;
import net.explorviz.shared.config.helper.PropertyHelper;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Check id generator injections for this service.
 *
 * @see DependencyInjectionBinder
 */
public class RedisGeneratorInjectionTest {

  @Inject
  private ServiceIdGenerator serviceIdGen;

  /**
   * Inject dependencies.
   */
  @BeforeEach
  public void setUp() {

    final Properties props = PropertyHelper.getLoadedProperties();
    props.remove("service.generator.id.redis");
    props.put("service.generator.id.redis", true);

    this.updateConfigInjectionProperties(props);

    final AbstractBinder binder = new DependencyInjectionBinder();
    final ServiceLocator locator = ServiceLocatorUtilities.bind(binder);
    locator.inject(this);
  }

  private void updateConfigInjectionProperties(final Properties props) {
    ConfigInjectionResolver.setPassedProperties(props);
    ConfigValuesInjectionResolver.setPassedProperties(props);
    PropertyHelper.setPassedProperties(props);
  }

  /**
   * Check if injected service generator is (per default) UuidServiceIdGenerator.
   *
   * @see UuidServiceIdGenerator
   */
  @Test
  public void testRedisServiceGeneratorInjection() {

    final String failMessage = "Redis service generator injection failed. "
        + "Injected wrong type, expected: '%s', but was '%s'";

    assertTrue(this.serviceIdGen instanceof RedisServiceIdGenerator,
        String.format(failMessage,
            RedisServiceIdGenerator.class.getName(),
            this.serviceIdGen.getClass().getName()));
  }

}
