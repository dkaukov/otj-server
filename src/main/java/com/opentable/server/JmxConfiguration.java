package com.opentable.server;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import com.google.common.base.MoreObjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import com.opentable.server.JmxConfiguration.JmxmpServer;

/**
 * JMX Configuration.
 *
 * <p>
 * Note that this configuration class injects a static {@link MBeanServer}. This will fail if you are creating
 * multiple contexts in the same process and attempting to register MBeans. When might you do this? A natural
 * circumstance is integration testing, in which you might spin up two servers in the same process and have
 * them talk to one another. Or, as another example, you might spin up a Discovery server and then wire up
 * two other servers to discover each other, all in the same process. To handle this circumstance
 * appropriately, consult the {@code TestMBeanServerConfiguration}, in the testing package of this codebase.
 */
@Configuration
@Import(JmxmpServer.class)
@EnableMBeanExport
public class JmxConfiguration {

    /**
     * Get/create the platform MBean Server
     * @return the platform MBean server
     */
    @Bean
    public MBeanServer getMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }

    /**
     * Sets up a JMXMP Sever
     */
    @Component
    static class JmxmpServer {
        private static final Logger LOG = LoggerFactory.getLogger(JmxmpServer.class);
        static final String WILDCARD_BIND = "0.0.0.0"; // NOPMD

        @Value("${ot.jmx.port:${PORT1:#{null}}}")
        private Integer jmxPort;

        @Value("${ot.jmx.address:#{null}}")
        private String jmxAddress;

        @Value("${ot.jmx.url-format:service:jmx:jmxmp://%s:%s}")
        private String urlFormat;

        private final MBeanServer mbs;
        private JMXConnectorServer server;

        /**
         * Creates a JMXMP Server for the given MBean Server
         * @param mbs the MBean Server to expose
         */
        @Inject
        JmxmpServer(MBeanServer mbs) {
            this.mbs = mbs;
        }

        /**
         * Create and start the JMX Connector Server
         * @throws IOException if the connector server cannot be made because of a communication problem
         */
        @PostConstruct
        public void start() throws IOException {
            if (jmxPort == null) {
                LOG.info("No JMX port set, not exporting");
                return;
            }

            final String url = String.format(
                    urlFormat,
                    MoreObjects.firstNonNull(jmxAddress, WILDCARD_BIND),
                    jmxPort);

            LOG.info("Starting JMX Connector Server '{}'", url);

            Map<String, String> jmxEnv = new HashMap<>();
            jmxEnv.put("jmx.remote.server.address.wildcard",
                    Boolean.toString(jmxAddress == null));

            JMXServiceURL jmxUrl = new JMXServiceURL(url);
            server = JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl, jmxEnv, mbs);
            server.start();
        }

        /**
         * Close the JMX Server if present
         * @throws IOException if the server cannot be closed cleanly. When this exception is thrown, the server has already attempted to close all client connections. All client connections are closed except possibly those that generated exceptions when the server attempted to close them.
         */
        @PreDestroy
        public void close() throws IOException {
            if (server != null) {
                server.stop();
                server = null;
            }
        }
    }
}
