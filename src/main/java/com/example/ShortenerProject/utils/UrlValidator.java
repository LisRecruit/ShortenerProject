package com.example.ShortenerProject.utils;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class UrlValidator {
    private final WebClient webClient;
    public UrlValidator(WebClient webClient){
        this.webClient=webClient;
    }
    public Mono<Boolean> isValidUrl(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().equals(HttpStatus.OK)) // Проверяем код ответа
                .timeout(Duration.ofSeconds(5)) // Устанавливаем таймаут
                .onErrorResume(WebClientResponseException.class, ex -> Mono.just(false)) // Обработка HTTP ошибок
                .onErrorResume(Exception.class, ex -> Mono.just(false)); // Обработка остальных ошибок
    }
}
