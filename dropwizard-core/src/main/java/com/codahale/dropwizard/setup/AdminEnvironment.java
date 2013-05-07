package com.codahale.dropwizard.setup;

import com.codahale.dropwizard.jetty.setup.ServletEnvironment;
import com.codahale.dropwizard.servlets.tasks.GarbageCollectionTask;
import com.codahale.dropwizard.servlets.tasks.Task;
import com.codahale.dropwizard.servlets.tasks.TaskServlet;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The administrative environment of a Dropwizard service.
 */
public class AdminEnvironment extends ServletEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminEnvironment.class);

    private final HealthCheckRegistry healthChecks;
    private final TaskServlet tasks;

    /**
     * Creates a new {@link AdminEnvironment}.
     *
     * @param handler      a servlet context handler
     * @param healthChecks a health check registry
     */
    public AdminEnvironment(ServletContextHandler handler,
                            HealthCheckRegistry healthChecks) {
        super(handler);
        this.healthChecks = healthChecks;
        this.healthChecks.register("deadlocks", new ThreadDeadlockHealthCheck());
        this.tasks = new TaskServlet();
        tasks.add(new GarbageCollectionTask());
        addServlet(tasks, "/tasks/*");
        handler.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarting(LifeCycle event) {
                logTasks();
                logHealthChecks();
            }
        });
    }

    /**
     * Adds the given task to the set of tasks exposed via the admin interface.
     *
     * @param task a task
     */
    public void addTask(Task task) {
        tasks.add(checkNotNull(task));
    }

    /**
     * Adds the given health check to the set of health checks exposed via the admin interface.
     *
     * @param healthCheck a health check
     */
    public void addHealthCheck(String name, HealthCheck healthCheck) {
        healthChecks.register(checkNotNull(name), checkNotNull(healthCheck));
    }

    private void logTasks() {
        final StringBuilder stringBuilder = new StringBuilder(1024).append(String.format("%n%n"));

        for (Task task : tasks.getTasks()) {
            stringBuilder.append(String.format("    %-7s /tasks/%s (%s)%n",
                                               "POST",
                                               task.getName(),
                                               task.getClass().getCanonicalName()));
        }

        LOGGER.info("tasks = {}", stringBuilder.toString());
    }

    private void logHealthChecks() {
        if (healthChecks.getNames().size() <= 1) {
            LOGGER.warn(String.format(
                    "%n" +
                            "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!%n" +
                            "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!%n" +
                            "!    THIS SERVICE HAS NO HEALTHCHECKS. THIS MEANS YOU WILL NEVER KNOW IF IT    !%n" +
                            "!    DIES IN PRODUCTION, WHICH MEANS YOU WILL NEVER KNOW IF YOU'RE LETTING     !%n" +
                            "!     YOUR USERS DOWN. YOU SHOULD ADD A HEALTHCHECK FOR EACH DEPENDENCY OF     !%n" +
                            "!     YOUR SERVICE WHICH FULLY (BUT LIGHTLY) TESTS YOUR SERVICE'S ABILITY TO   !%n" +
                            "!      USE THAT SERVICE. THINK OF IT AS A CONTINUOUS INTEGRATION TEST.         !%n" +
                            "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!%n" +
                            "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
            ));
        }
        LOGGER.debug("health checks = {}", healthChecks.getNames());
    }
}
