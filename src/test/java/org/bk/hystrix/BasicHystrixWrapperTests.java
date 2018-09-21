package org.bk.hystrix;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.bk.hystrix.ReactorHystrixFns.protectCall;

public class BasicHystrixWrapperTests {
    
    @Test
    public void testACallProtectedByHystrix() {
        Mono<Integer> result = protectCall(() -> 1);

        StepVerifier.create(result)
                .expectNext(1)
                .expectComplete()
                .verify();
    }
}
