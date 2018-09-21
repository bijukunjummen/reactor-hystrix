package org.bk.hystrix;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public class ReactorHystrixFns {
    public static <T> Mono<T> protectCall(Supplier<T> supplier) {
        return Mono.just(supplier.get());
    }
}
