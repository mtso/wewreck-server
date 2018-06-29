package com.wepay.app;

import com.wepay.resource.PaymentsEndpoint;
import com.wepay.resource.PingPongEndpoint;
import com.wepay.resource.WebHooks;
import io.dropwizard.websockets.WebsocketBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codahale.metrics.MetricRegistry;
import com.wepay.wehack.configuration.ApplicationConfiguration;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class MainApp extends Application<ApplicationConfiguration> {
    private WebsocketBundle websocketBundle = new WebsocketBundle(PingPongEndpoint.class);

    private static Logger log = LoggerFactory.getLogger(MainApp.class);
    private static MetricRegistry metrics;
    public static final String SERVICE_NAME = System.getenv("WEPAY_SERVICE_NAME") == null ? "wehack"
            : System.getenv("WEPAY_SERVICE_NAME");

    public static void main(String[] args) throws Exception {
        new MainApp().run(args);
    }

    @Override
    public String getName() {
        return SERVICE_NAME;
    }

    @Override
    public void initialize(Bootstrap<ApplicationConfiguration> bootstrap) {
        log.debug("Initialized AppConfigs");
        metrics = bootstrap.getMetricRegistry();
        bootstrap.addBundle(websocketBundle);
    }

    @Override
    public void run(ApplicationConfiguration configuration, Environment environment) throws Exception {
        log.info("Initializing the application: " + getName());
        log.debug("Registering resources ");
        environment.jersey().register(new WebHooks());

        websocketBundle.addEndpoint(PingPongEndpoint.class);
        websocketBundle.addEndpoint(PaymentsEndpoint.class);
    }
}
