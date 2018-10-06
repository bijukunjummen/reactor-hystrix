package reactor.hystrix.wrapper;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.Hystrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static reactor.hystrix.wrapper.ReactorHystrixFns.protectCall;

public class ReactorHystrixFunctionTests {

    @BeforeEach
    public void init() {
        Hystrix.reset();
        ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.circuitBreaker.forceOpen", "false");
    }
    
    @Test
    public void testCleanCall() {
        Mono<Integer> result = protectCall(() -> 1);

        StepVerifier.create(result)
                .expectNext(1)
                .expectComplete()
                .verify();
    }

    @Test
    public void testSupplierCallOnlyOnSubscription() {
        AtomicInteger counter = new AtomicInteger();
        Mono<Integer> result = protectCall(() -> counter.incrementAndGet());

        assertThat(counter.get()).isEqualTo(0);

        result.block();
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    public void testMultipleSubscriptions() {
        AtomicInteger counter = new AtomicInteger();
        Mono<Integer> result = protectCall(() -> counter.incrementAndGet());
        assertThat(result.block()).isEqualTo(1);
        assertThatThrownBy(() -> result.block()).hasRootCauseInstanceOf(Exception.class);
        
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    public void testShortCircuitedCallWithNoFallbackThrowsRuntimeException() {
        ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.circuitBreaker.forceOpen", "true");

        Mono<Integer> result = protectCall(() -> 1);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void testShortCicuitedWithFallback() {
        ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.circuitBreaker.forceOpen", "true");

        Mono<Integer> result = protectCall(() -> 1, (t) -> 2);

        StepVerifier.create(result)
                .expectNext(2)
                .expectComplete()
                .verify();
    }

    @Test
    public void testProtectWithExceptionsAndFallback() {
        Mono<String> result = protectCall(() -> {
            throw new RuntimeException("Something is wrong!");
        }, (t) -> "fallback");

        StepVerifier.create(result)
                .expectNext("fallback")
                .expectComplete()
                .verify();
    }

}
