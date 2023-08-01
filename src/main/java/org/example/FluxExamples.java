package org.example;

import com.github.javafaker.Faker;
import org.example.helper.NameProducer;
import reactor.core.Disposable;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class FluxExamples {
    private static final Faker FAKER = Faker.instance();

    public static void blockingReactiveStream() {
        System.out.println("--- blockingReactiveStream");
        Flux.range(0, 5).map(i -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return i;
                })
                .log()
                .subscribe(System.out::println);
        System.out.println();
    }

    public static void nonBlockingReactiveStream() {
        System.out.println("--- nonBlockingReactiveStream");
        Disposable subscription = Flux.interval(Duration.of(1, ChronoUnit.SECONDS))
                .log()
                .subscribe(System.out::println);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        subscription.dispose();
        System.out.println();
    }

    public static void hotPublisher() {
        System.out.println("--- hotPublisher");
        Flux<String> flux = Flux.just(UUID.randomUUID().toString())
                .doOnSubscribe(s -> System.out.println("new subscriber for the hot publisher"));
        // the value will be the same for both subscribers
        flux.subscribe(System.out::println);
        flux.subscribe(System.out::println);
        System.out.println();
    }

    public static void coldPublisher() {
        System.out.println("--- coldPublisher");
        Flux<String> flux = Flux.defer(() -> Flux.just(UUID.randomUUID().toString()))
                .doOnSubscribe(s -> System.out.println("new subscriber for the cold publisher"));
        // the value will be the different for subscribers
        flux.subscribe(System.out::println);
        flux.subscribe(System.out::println);
        System.out.println();
    }

    public static void preparedMulticastsPublisher() {
        System.out.println("--- broadcastPublisher");
        Flux<Integer> source = Flux.range(1, 3)
                .doOnSubscribe(s -> System.out.println("new subscriber for the cold publisher"));

        ConnectableFlux<Integer> conn = source.publish();

        conn.subscribe(e -> System.out.println("Subscriber 1 onNext: " + e));
        conn.subscribe(e -> System.out.println("Subscriber 2 onNext: " + e));

        System.out.println("all subscribers are ready, connecting");
        conn.connect();
        System.out.println();
    }

    public static void cachingSubscription() throws InterruptedException {
        System.out.println("--- cachingSubscription");
        Flux<Integer> source = Flux.range(1, 2)
                .doOnSubscribe(s -> System.out.println("new subscriber for the cold publisher"))
                .cache(Duration.ofSeconds(1));

        source.subscribe(e -> System.out.println("Subscriber 1 onNext: " + e));
        source.subscribe(e -> System.out.println("Subscriber 2 onNext: " + e));

        Thread.sleep(1200);
        source.subscribe(e -> System.out.println("Subscriber 3 onNext: " + e));
        System.out.println();
    }

    public static void multicastsPublisher() throws InterruptedException {
        System.out.println("--- multicastsPublisher");
        Flux<Integer> source = Flux.range(0, 5)
                .delayElements(Duration.ofMillis(100))
                .doOnSubscribe(s -> System.out.println("new subscriber for the multicast publisher"))
                .share(); // transform to the hot subscription

        source.subscribe(e -> System.out.println("Subscriber 1 onNext: " + e));
        Thread.sleep(400);
        source.subscribe(e -> System.out.println("Subscriber 2 onNext: " + e));
        Thread.sleep(1000);
        System.out.println();
    }

    public static void parallelSubscribeOn() throws InterruptedException {
        System.out.println("--- parallelSubscribeOn");
        Flux<Integer> flux = Flux.range(0, 10)
                .log()
                .subscribeOn(Schedulers.parallel());
        flux.subscribe(e -> System.out.println(e + ": Current thread: " + Thread.currentThread().getName()));
        flux.subscribe(e -> System.out.println(e + ": Current thread: " + Thread.currentThread().getName()));

        Thread.sleep(2000);
        System.out.println();
    }

    public static void parallelPublishOn() throws InterruptedException {
        System.out.println("--- parallelPublishOn");
        Flux<Integer> flux = Flux.range(0, 10)
                .log()
                .publishOn(Schedulers.parallel());
        flux.subscribe(e -> System.out.println(e + ": Current thread: " + Thread.currentThread().getName()));
        flux.subscribe(e -> System.out.println(e + ": Current thread: " + Thread.currentThread().getName()));
        Thread.sleep(2000);
        System.out.println();
    }

    public static void parallelFlux() throws InterruptedException {
        System.out.println("--- parallelFlux");
        Flux.range(0, 10)
                .parallel()
                .runOn(Schedulers.parallel())
                .log()
                .subscribe(e -> System.out.println(e + ": Current thread: " + Thread.currentThread().getName()));
        Thread.sleep(2000);
        System.out.println();
    }

    public static void parallelSinks() throws InterruptedException {
        System.out.println("--- parallelPublishing");

        NameProducer nameProducer = new NameProducer();

        Flux.create(nameProducer)
                .subscribe(System.out::println);

        Runnable runnable = nameProducer::produce;

        for (int i = 0; i < 10; i++) {
            new Thread(runnable).start();
        }

        Thread.sleep(2000);

        System.out.println();
    }

    public static void checkIfSubscriptionIsCancelled() {
        System.out.println("--- checkIfSubscriptionIsCancelled");
        Flux.create(fluxSink -> {
                    String country;
                    int counter = 0;
                    do{
                        country = FAKER.country().name();
                        System.out.println("emitting : " + country);
                        fluxSink.next(country);
                        counter++;
                    } while (!country.equalsIgnoreCase("canada") && !fluxSink.isCancelled() && counter < 10);
                    fluxSink.complete();
                })
                .take(3)
                .subscribe(System.out::println);
    }

    public static void sinkUnicast() {
        System.out.println("--- sinkUnicast");
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        Flux<String> flux = sink.asFlux();

        flux.subscribe(x -> System.out.println("subscriber 1: " + x), e -> System.out.println("subscriber 1 error: " + e));
        flux.subscribe(x -> System.out.println("subscriber 2: " + x), e -> System.out.println("subscriber 2 error: " + e));

        sink.tryEmitNext("hello");
        sink.tryEmitNext("world");
    }

    public static void sinkMulticast() {
        System.out.println("--- sinkMulticast");
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
        Flux<String> flux = sink.asFlux();

        // stored in the buffer until the first subscriber appears
        sink.tryEmitNext("hello");

        flux.subscribe(x -> System.out.println("subscriber 1: " + x));

        sink.tryEmitNext("world");

        flux.subscribe(x -> System.out.println("subscriber 2: " + x));
        sink.tryEmitNext("!");
    }

}
