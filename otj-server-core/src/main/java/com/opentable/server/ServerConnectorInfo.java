/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opentable.server;

import com.google.common.base.Preconditions;

import org.eclipse.jetty.server.ServerConnector;

import com.opentable.server.HttpServerInfo.ConnectorInfo;

class ServerConnectorInfo implements ConnectorInfo {
    private final String name;
    private final ServerConnector connector;
    private final ServerConnectorConfig config;

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
