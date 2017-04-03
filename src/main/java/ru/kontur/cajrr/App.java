package ru.kontur.cajrr;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kontur.cajrr.api.Cluster;
import ru.kontur.cajrr.api.Node;
import ru.kontur.cajrr.health.RepairHealthCheck;
import ru.kontur.cajrr.resources.RepairResource;
import ru.kontur.cajrr.resources.RingResource;
import ru.kontur.cajrr.resources.TableResource;

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
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {}

    @Override
    public void run(AppConfiguration configuration,
                    Environment environment) throws Exception {


        final RingResource ringResource = new RingResource(configuration);
        final RepairResource repairResource = new RepairResource(configuration);
        final TableResource tableResource = new TableResource(configuration);
        environment.jersey().register(repairResource);
        environment.jersey().register(tableResource);
        environment.jersey().register(ringResource);

        final RepairHealthCheck healthCheck = new RepairHealthCheck(configuration);
        environment.healthChecks().register("repair", healthCheck);

        Cluster cluster = new Cluster(configuration, repairResource, ringResource, tableResource);
        environment.lifecycle().manage(cluster);
    }
}
