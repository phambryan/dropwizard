package com.codahale.dropwizard.setup;

import com.codahale.dropwizard.Bundle;
import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.ConfiguredBundle;
import com.codahale.dropwizard.Service;
import com.codahale.dropwizard.cli.Command;
import com.codahale.dropwizard.cli.ConfiguredCommand;
import com.codahale.dropwizard.configuration.ConfigurationSourceProvider;
import com.codahale.dropwizard.configuration.FileConfigurationSourceProvider;
import com.codahale.dropwizard.jackson.Jackson;
import com.codahale.dropwizard.jetty.ConnectorFactory;
import com.codahale.dropwizard.logging.AppenderFactory;
import com.codahale.dropwizard.server.ServerFactory;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.jersey.spi.service.ServiceFinder;

import java.lang.management.ManagementFactory;
import java.util.List;

public class Bootstrap<T extends Configuration> {
    private static final ImmutableList<Class<?>> SPI_CLASSES = ImmutableList.of(
            AppenderFactory.class,
            ServerFactory.class,
            ConnectorFactory.class
    );

    private final Service<T> service;
    private final ObjectMapper objectMapper;
    private final List<Bundle> bundles;
    private final List<ConfiguredBundle<? super T>> configuredBundles;
    private final List<Command> commands;
    private final MetricRegistry metricRegistry;

    private ConfigurationSourceProvider configurationProvider;
    private ClassLoader classLoader;

    public Bootstrap(Service<T> service) {
        this.service = service;
        this.objectMapper = Jackson.newObjectMapper();
        final SubtypeResolver resolver = objectMapper.getSubtypeResolver();
        for (Class<?> klass : SPI_CLASSES) {
            resolver.registerSubtypes(ServiceFinder.find(klass).toClassArray());
        }
        this.bundles = Lists.newArrayList();
        this.configuredBundles = Lists.newArrayList();
        this.commands = Lists.newArrayList();
        this.metricRegistry = new MetricRegistry();
        metricRegistry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet());
        metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
        metricRegistry.register("jvm.threads", new ThreadStatesGaugeSet());

        this.configurationProvider = new FileConfigurationSourceProvider();
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    public Service<T> getService() {
        return service;
    }

    public ConfigurationSourceProvider getConfigurationProvider() {
        return configurationProvider;
    }

    public void setConfigurationProvider(ConfigurationSourceProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void addBundle(Bundle bundle) {
        bundle.initialize(this);
        bundles.add(bundle);
    }

    public void addBundle(ConfiguredBundle<? super T> bundle) {
        bundle.initialize(this);
        configuredBundles.add(bundle);
    }

    public void addCommand(Command command) {
        commands.add(command);
    }

    public void addCommand(ConfiguredCommand<T> command) {
        commands.add(command);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void runWithBundles(T configuration, Environment environment) throws Exception {
        for (Bundle bundle : bundles) {
            bundle.run(environment);
        }
        for (ConfiguredBundle<? super T> bundle : configuredBundles) {
            bundle.run(configuration, environment);
        }
    }

    public ImmutableList<Command> getCommands() {
        return ImmutableList.copyOf(commands);
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
}
