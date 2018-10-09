package com.opentable.server;

import com.google.common.base.Preconditions;

import org.eclipse.jetty.server.ServerConnector;

import com.opentable.server.HttpServerInfo.ConnectorInfo;

/**
 * Information about a server connector
 */
class ServerConnectorInfo implements ConnectorInfo {
    private final String name;
    private final ServerConnector connector;
    private final ServerConnectorConfig config;

    /**
     * Create a server connector information object
     * @param name the name of the connector
     * @param connector the connector to create
     * @param config the configuration of the connector
     */
    ServerConnectorInfo(String name, ServerConnector connector, ServerConnectorConfig config) {
        this.name = name;
        this.connector = connector;
        this.config = config;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPort() {
        final int port = connector.getLocalPort();
        Preconditions.checkState(port > 0, "port not yet assigned for connector '%s'", name);
        return port;
    }

    @Override
    public String getProtocol() {
        return config.getProtocol();
    }
}
