package br.com.ajudamutua.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ReconciliationMetrics {
    private final Counter scanned;
    private final Counter settled;
    private final Counter failed;
    private final Counter pending;
    private final Counter divergence;
    private final Counter providerUnavailable;
    private final Timer providerQueryLatency;

    public ReconciliationMetrics(MeterRegistry registry) {
        this.scanned = Counter.builder("fazerobem.reconciliation.scanned.total")
                .description("Payment attempts scanned by reconciliation")
                .register(registry);
        this.settled = Counter.builder("fazerobem.reconciliation.settled.total")
                .description("Payment attempts settled after external reconciliation")
                .register(registry);
        this.failed = Counter.builder("fazerobem.reconciliation.failed.total")
                .description("Payment attempts marked failed after external reconciliation")
                .register(registry);
        this.pending = Counter.builder("fazerobem.reconciliation.pending.total")
                .description("Payment attempts still pending after external reconciliation")
                .register(registry);
        this.divergence = Counter.builder("fazerobem.reconciliation.divergence.total")
                .description("Provider/internal state divergences detected")
                .register(registry);
        this.providerUnavailable = Counter.builder("fazerobem.reconciliation.provider_unavailable.total")
                .description("Provider status queries that failed")
                .register(registry);
        this.providerQueryLatency = Timer.builder("fazerobem.reconciliation.provider_query")
                .description("External provider reconciliation query latency")
                .publishPercentileHistogram()
                .register(registry);
    }

    public void scanned() { scanned.increment(); }
    public void settled() { settled.increment(); }
    public void failed() { failed.increment(); }
    public void pending() { pending.increment(); }
    public void divergence() { divergence.increment(); }
    public void providerUnavailable() { providerUnavailable.increment(); }
    public void recordProviderQuery(long nanos) { providerQueryLatency.record(nanos, TimeUnit.NANOSECONDS); }
}
