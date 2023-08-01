package org.example;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        FluxExamples.blockingReactiveStream();
        FluxExamples.nonBlockingReactiveStream();
        FluxExamples.hotPublisher();
        FluxExamples.coldPublisher();
        FluxExamples.preparedMulticastsPublisher();
        FluxExamples.cachingSubscription();
        FluxExamples.multicastsPublisher();
        FluxExamples.parallelSubscribeOn();
        FluxExamples.parallelPublishOn();
        FluxExamples.parallelFlux();
        FluxExamples.parallelSinks();
        FluxExamples.checkIfSubscriptionIsCancelled();
        FluxExamples.sinkUnicast();
        FluxExamples.sinkMulticast();
    }
}
