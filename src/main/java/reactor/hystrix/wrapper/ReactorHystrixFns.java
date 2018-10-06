package reactor.hystrix.wrapper;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.RxReactiveStreams;

import java.util.function.Function;
import java.util.function.Supplier;

public final class ReactorHystrixFns {
    public static <T> Mono<T> protectCall(Supplier<T> toRun) {
        return protectCall(toRun, null);
    }

    public static <T> Mono<T> protectCall(Supplier<T> toRun, Function<Throwable, T> fallback) {
        return protectCall("default", "default", toRun, fallback);
    }
    public static <T> Mono<T> protectCall(String commandKey, String groupKey, Supplier<T> toRun, Function<Throwable, T> fallback) {
        return protectCall(HystrixCommand.Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey)), toRun, fallback);
    }

    public static <T> Mono<T> protectCall(HystrixCommand.Setter setter,
                                          Supplier<T> supplier, Function<Throwable, T> fallback) {
        Observable<T> res = new GenericHystrixCommand<T>(setter, supplier, fallback).toObservable();
        return Mono.from(RxReactiveStreams.toPublisher(res));
    }
}
