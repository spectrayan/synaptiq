package com.spectrayan.synaptiq.integration.tenant;

import com.spectrayan.synaptiq.integration.core.TenantRouteNamingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.NamedNode;
import org.apache.camel.Processor;
import org.apache.camel.spi.InterceptStrategy;

/**
 * Camel interceptor that enforces tenant isolation on every exchange.
 * <p>
 * Extracts the tenant ID from the route ID prefix and sets the
 * {@code X-Tenant-Id} header on every exchange for downstream use.
 */
@Slf4j
public class TenantIsolationInterceptor implements InterceptStrategy {

    public static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    public Processor wrapProcessorInInterceptors(CamelContext context,
                                                  NamedNode definition,
                                                  Processor target,
                                                  Processor nextTarget) throws Exception {
        return exchange -> {
            String routeId = exchange.getFromRouteId();
            if (routeId != null) {
                String tenantId = TenantRouteNamingStrategy.extractTenantId(routeId);
                if (tenantId != null) {
                    exchange.getIn().setHeader(TENANT_HEADER, tenantId);
                }
            }
            target.process(exchange);
        };
    }
}
