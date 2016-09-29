package ru.kontur.cajrr;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kontur.cajrr.api.Cluster;
import ru.kontur.cajrr.health.RepairHealthCheck;
import ru.kontur.cajrr.resources.RepairResource;
import ru.kontur.cajrr.resources.RingResource;

import java.io.IOException;

/**
 * Cajjr
 *
 */
public class App extends Application<AppConfiguration>
{
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public App() {
        super();
        LOG.info("Default Cajrr constructor called");
    }

    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public String getName() {
        return "cajrr";
    }

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/", "/ui", "index.html"));
    }

    @Override
    public void run(AppConfiguration configuration,
                    Environment environment) throws IOException {

        configuration.clusters.forEach(Cluster::connect);

        final RingResource ringResource = new RingResource(configuration);
        final RepairResource repairResource = new RepairResource(configuration);
        final RepairHealthCheck healthCheck = new RepairHealthCheck(configuration);

        environment.healthChecks().register("repair", healthCheck);
        environment.jersey().register(repairResource);
        environment.jersey().register(ringResource);
    }
}
