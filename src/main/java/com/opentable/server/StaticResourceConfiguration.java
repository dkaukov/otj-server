package com.opentable.server;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO Don't serve directory indexes.

/**
 * Configures serving of static resources
 */
@Configuration
public class StaticResourceConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(StaticResourceConfiguration.class);

    /**
     * Default name of folder on classpath with static assets
     */
    static final String DEFAULT_PATH_NAME = "static";

    /**
     * SpEL expression to get the name of the classpath directory with static assets
     */
    private static final String PATH_CONFIG_VALUE = "${ot.httpserver.static-path:" + DEFAULT_PATH_NAME + "}";

    private final String staticPathName;

    /**
     * Create static resource configuration
     * @param staticPathName the name of the directory with static assets to serve
     */
    @Inject
    StaticResourceConfiguration(@Value(PATH_CONFIG_VALUE) final String staticPathName) {
        this.staticPathName = staticPathName;
    }

    /**
     * Get the path to the directory with static assets
     * @return the path to the directory with static assets
     */
    public String staticPath() {
        return staticPath("");
    }

    /**
     * Get the path to a sub-directory with static assets. The argument path passed in will be returned prefixed by the static directory
     * @param rest the rest of the (excluding the directory with static assets), do not prefix with a slash
     * @return the full path to the static assets directory requested
     */
    public String staticPath(final String rest) {
        return String.format("/%s/%s", staticPathName, rest);
    }

    /**
     * Create a bean to register a servlet for serving static resources
     * @return a servlet registration bean for static resources
     */
    @Bean
    @SuppressFBWarnings("NP_LOAD_OF_KNOWN_NULL_VALUE")
    public ServletRegistrationBean<DefaultServlet> staticResourceServlet() {
        try(Resource rsrc = Resource.newClassPathResource(staticPath())){
            LOG.debug("Found static resources at {}", rsrc);

            if (rsrc == null) {
                LOG.info("Didn't find '/static' on classpath, not serving static files");
                ServletRegistrationBean<DefaultServlet> servletRegistrationBean =  new ServletRegistrationBean<>(new DefaultServlet());
                servletRegistrationBean.setName("static-inactive");
                servletRegistrationBean.setEnabled(false);
                return servletRegistrationBean;
            }

            DefaultServlet servlet = new DefaultServlet();
            ServletRegistrationBean<DefaultServlet> bean = new ServletRegistrationBean<>(servlet, staticPath() + "*");
            bean.addInitParameter("gzip", "true");
            bean.addInitParameter("etags", "true");
            bean.addInitParameter("resourceBase", StringUtils.substringBeforeLast(rsrc.toString(), staticPathName));
            LOG.debug("Configuring static resources: {}", bean.getInitParameters());
            return bean;
        }
    }
}
