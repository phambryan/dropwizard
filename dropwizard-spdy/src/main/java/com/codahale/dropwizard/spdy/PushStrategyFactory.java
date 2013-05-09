package com.codahale.dropwizard.spdy;

import com.codahale.dropwizard.util.Subtyped;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.eclipse.jetty.spdy.server.http.PushStrategy;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public interface PushStrategyFactory extends Subtyped {
    PushStrategy build();
}
