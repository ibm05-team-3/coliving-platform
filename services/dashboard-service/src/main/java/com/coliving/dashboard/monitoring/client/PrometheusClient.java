package com.coliving.dashboard.monitoring.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Prometheus HTTP API로 즉시 조회(/api/v1/query)와 구간 조회(/api/v1/query_range)를 보내는 클라이언트. */
@Component
public class PrometheusClient {

    /** 즉시 조회 결과 한 줄. labels는 시계열의 라벨, value는 값. */
    public record Sample(Map<String, String> labels, double value) {}

    /** 구간 조회 결과의 점 하나. */
    public record Point(long epochSecond, double value) {}

    /** 구간 조회 결과의 시계열 하나. */
    public record Series(Map<String, String> labels, List<Point> points) {}

    private final RestClient restClient;

    public PrometheusClient(@Value("${monitoring.prometheus.url:http://localhost:9090}") String baseUrl) {
        // Prometheus가 꺼져 있을 때 화면이 오래 멈추지 않도록 타임아웃을 짧게 둔다.
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    /** PromQL을 지금 시점으로 조회한다. 연결 실패나 쿼리 오류는 예외로 던진다. */
    @SuppressWarnings("unchecked")
    public List<Sample> query(String promql) {
        List<Map<String, Object>> result = fetch("/api/v1/query?query={q}", promql);

        List<Sample> samples = new ArrayList<>();
        for (Map<String, Object> item : result) {
            Map<String, String> labels = (Map<String, String>) item.getOrDefault("metric", Map.of());
            List<Object> value = (List<Object>) item.get("value"); // [시각, "값"]
            samples.add(new Sample(labels, parseValue(value.get(1))));
        }
        return samples;
    }

    /** PromQL을 start~end 구간으로 조회한다. step은 점 사이 간격(초). */
    @SuppressWarnings("unchecked")
    public List<Series> queryRange(String promql, Instant start, Instant end, int stepSeconds) {
        List<Map<String, Object>> result = fetch(
                "/api/v1/query_range?query={q}&start={s}&end={e}&step={step}",
                promql, start.getEpochSecond(), end.getEpochSecond(), stepSeconds);

        List<Series> seriesList = new ArrayList<>();
        for (Map<String, Object> item : result) {
            Map<String, String> labels = (Map<String, String>) item.getOrDefault("metric", Map.of());
            List<List<Object>> values = (List<List<Object>>) item.get("values"); // [[시각, "값"], ...]

            List<Point> points = new ArrayList<>();
            for (List<Object> pair : values) {
                points.add(new Point(((Number) pair.get(0)).longValue(), parseValue(pair.get(1))));
            }
            seriesList.add(new Series(labels, points));
        }
        return seriesList;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetch(String uri, Object... uriVariables) {
        Map<String, Object> body = restClient.get()
                .uri(uri, uriVariables)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (body == null || !"success".equals(body.get("status"))) {
            throw new IllegalStateException("Prometheus 조회 실패: " + uri);
        }
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        return (List<Map<String, Object>>) data.get("result");
    }

    /** Prometheus는 값을 문자열로 준다. "NaN", "+Inf", "-Inf"도 올 수 있다. */
    private static double parseValue(Object raw) {
        String text = String.valueOf(raw);
        return switch (text) {
            case "+Inf" -> Double.POSITIVE_INFINITY;
            case "-Inf" -> Double.NEGATIVE_INFINITY;
            default -> Double.parseDouble(text); // "NaN"도 변환된다
        };
    }
}
