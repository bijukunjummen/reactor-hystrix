package reactor.hystrix.wrapper;

import com.netflix.hystrix.HystrixCommand;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@link HystrixCommand} that takes a {@link Supplier} as the protected function to execute.
 * In case of an error the fallback is handled by a fallback {@link Function} which takes
 * returns a fallback response given the exception as a parameter
 */
class GenericHystrixCommand<T> extends HystrixCommand<T> {

    private Supplier<T> toRun;

    private Function<Throwable, T> fallback;


    GenericHystrixCommand(Setter setter, Supplier<T> toRun, Function<Throwable, T> fallback) {
        super(setter);
        this.toRun = toRun;
        this.fallback = fallback;
    }

    protected T run() {
        return this.toRun.get();
    }

    @Override
    protected T getFallback() {
        return (this.fallback != null)
                ? this.fallback.apply(getExecutionException())
                : super.getFallback();
    }

}
