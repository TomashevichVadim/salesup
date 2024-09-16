package org.example;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
@CommonsLog
public class CrptApi {

    private final ChronoUnit timeUnit;
    private final Long timeUnitCount;
    private final HttpClientProvider httpClientProvider;
    private final Long maxThreadsCount;
    private AtomicLong executedThreadCounter;

    public CrptApi(ChronoUnit chronoUnit, Long timeUnitCount, Long maxThreadsCount) {
        this.timeUnit = chronoUnit;
        this.timeUnitCount = timeUnitCount;
        this.httpClientProvider = new HttpClientProvider();
        this.executedThreadCounter = new AtomicLong();
        this.maxThreadsCount = maxThreadsCount;

        Thread.startVirtualThread(() -> {
            LocalDateTime timeStamp = LocalDateTime.now();
            while (true) {
                if (LocalDateTime.now().isAfter(timeStamp.plus(timeUnitCount, chronoUnit))) {
                    executedThreadCounter.set(0);
                    timeStamp = LocalDateTime.now();
                }
            }
        });
    }

    @SneakyThrows
    public Void execute() {
        if (executedThreadCounter.longValue() < maxThreadsCount) {
            executedThreadCounter.incrementAndGet();
            httpClientProvider.sendDocument(Request.prepareDefaultRequest());
            log.info("Executing thread %s".formatted(Thread.currentThread().getName()));
        } else {
            log.info("Thread await %s".formatted(Thread.currentThread().getName()));
            Thread.sleep(Duration.between(LocalDateTime.now(), LocalDateTime.now().plus(timeUnitCount, timeUnit)));
            execute();
        }
        return null;
    }

    @Getter
    @Setter
    @Builder(toBuilder = true)
    static class Request {

        private Description description;

        private boolean importRequest;

        @JsonAlias("doc_id")
        private String docId;

        @JsonAlias("doc_status")
        private String docStatus;

        @JsonAlias("doc_type")
        private String docType;

        @JsonAlias("owner_inn")
        private String ownerInn;

        @JsonAlias("participant_inn")
        private String participantInn;

        @JsonAlias("producer_inn")
        private String producerInn;

        @JsonAlias("production_date")
        private String productionDate;

        @JsonAlias("production_type")
        private String productionType;

        @JsonAlias("reg_date")
        private String regDate;

        @JsonAlias("reg_number")
        private String regNumber;

        private List<Product> products;

        public static Request prepareDefaultRequest() {
            return Request.builder()
                    .description(Description.builder().participantInn("particripantInn").build())
                    .importRequest(false)
                    .docId("docId")
                    .docStatus("docStatus")
                    .ownerInn("ownerInner")
                    .participantInn("participantInner")
                    .producerInn("producerInner")
                    .productionDate("2024-09-09T12:12:12")
                    .productionType("productionType")
                    .regDate("2024-09-09T12:12:12")
                    .regNumber("123")
                    .products(List.of(Product.prepareDefaultProduct()))
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder(toBuilder = true)
    static class Description {
        private String participantInn;
    }

    @Getter
    @Setter
    @Builder
    static class Product {

        @JsonAlias("certificate_document")
        private String certificateDocument;

        @JsonAlias("certificate_document_date")
        private String certificateDocumentDate;

        @JsonAlias("certificate_document_number")
        private String certificateDocumentNumber;

        @JsonAlias("owner_inn")
        private String ownerInn;

        @JsonAlias("producer_inn")
        private String producerInn;

        @JsonAlias("production_date")
        private String productionDate;

        @JsonAlias("tnved_code")
        private String tnvedCode;

        @JsonAlias("uit_code")
        private String uitCode;

        @JsonAlias("uitu_code")
        private String uituCode;

        public static Product prepareDefaultProduct() {
            return Product.builder()
                    .certificateDocument("document")
                    .certificateDocumentNumber("1234")
                    .certificateDocumentDate("2024-09-09T12:12:12")
                    .ownerInn("inn")
                    .producerInn("producer")
                    .productionDate("2024-09-09T12:12:12")
                    .tnvedCode("123")
                    .uitCode("456")
                    .uituCode("789")
                    .build();
        }
    }

    class HttpClientProvider {

        private final RestClient httpProvider;

        public HttpClientProvider() {
            httpProvider = RestClient.builder()
                    .requestFactory(new SimpleClientHttpRequestFactory())
                    .baseUrl("https://ismp.crpt.ru/api/v3/lk")
                    .build();
        }

        public void sendDocument(Request request) {
            try {
                httpProvider
                        .post()
                        .uri("/documents/create")
                        .body(request)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve().toBodilessEntity();
            } catch (Exception exception) {
                log.error(exception.getMessage());
            }
        }
    }
}

