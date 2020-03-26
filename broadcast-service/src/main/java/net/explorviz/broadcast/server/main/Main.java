package net.explorviz.broadcast.server.main;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.filter.MetricsFilter;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.jetty.JettyStatisticsCollector;
import net.explorviz.shared.config.helper.PropertyHelper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * Main entry point for this Java application.
 *
 */
public final class Main {

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
  private static final int DEFAULT_PORT = 8081;

  private Main() {
    // Utility Class
  }

  /**
   * Starts the landscape service server.
   *
   */
  public static void main(final String[] args) {

    final Server server = new Server(getPort());

    final ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(createJaxRsApp()));
    final ServletContextHandler context = new ServletContextHandler(server, getContextPath());
    context.addServlet(jerseyServlet, "/*");

    // Prometheus
    StatisticsHandler stats = new StatisticsHandler();
    stats.setHandler(server.getHandler());
    server.setHandler(stats);

    new JettyStatisticsCollector(stats).register();

    context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");

    MetricsFilter metricsFilter =
        new MetricsFilter("explorviz_request_time",
            "Filter to measure and forward request times", 20, null);
    FilterHolder filterHolder = new FilterHolder(metricsFilter);
    context.addFilter(filterHolder, "/v1/*", EnumSet.of(DispatcherType.REQUEST));

    // Collect JVM metrics
    DefaultExports.initialize();

    try {
      server.start();
    } catch (final Exception e) { // NOPMD
      LOGGER.error("Server start failed", e);
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        server.stop();
      } catch (final Exception e) { // NOPMD
        LOGGER.error("Server stop failed", e);
      }
    }));
  }

  private static int getPort() {
    try {
      return PropertyHelper.getIntegerProperty("server.port");
    } catch (final NumberFormatException e) {
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info(
            "ATTENTION: Using default port " + DEFAULT_PORT + ". Check explorviz.properties file.",
            e);
      }
    }
    return DEFAULT_PORT;
  }

  private static String getContextPath() {
    final String statedContextPath = PropertyHelper.getStringProperty("server.contextPath");

    if (statedContextPath == null) {
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("ATTENTION: Using default contextPath '/' for server. "
            + "Maybe your stated server.contextPath property is no valid string.");
      }
      return "/";
    } else {
      return statedContextPath;
    }
  }

  private static ResourceConfig createJaxRsApp() {
    return new ResourceConfig(new BroadcastApplication());
  }

}
