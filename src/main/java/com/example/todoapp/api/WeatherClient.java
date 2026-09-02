package com.example.todoapp.api;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class WeatherClient {

    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast?latitude=35.6895&longitude=139.6917&daily=weather_code&timezone=Asia/Tokyo";

    private final RestClient restClient;

    public WeatherClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3_000);
        requestFactory.setReadTimeout(5_000);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    public WeatherResult getWeather(LocalDate from, LocalDate to) {
        try {
            String responseBody = restClient.get().uri(WEATHER_URL).retrieve().body(String.class);
            String[] dates = arrayValues(responseBody, "time");
            String[] codes = arrayValues(responseBody, "weather_code");
            Map<String, String> result = new LinkedHashMap<>();
            for (int i = 0; i < dates.length && i < codes.length; i++) {
                LocalDate date = LocalDate.parse(dates[i].trim());
                if ((from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to))) {
                    result.put(date.toString(), weatherText(Integer.parseInt(codes[i].trim())));
                }
            }
            return new WeatherResult(result, false);
        } catch (RuntimeException e) {
            return new WeatherResult(Map.of(), true);
        }
    }

    private String[] arrayValues(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*\\[([^]]*)\\]").matcher(json);
        if (!matcher.find()) return new String[0];
        String values = matcher.group(1).replace("\"", "").trim();
        return values.isEmpty() ? new String[0] : values.split(",");
    }

    private String weatherText(int code) {
        if (code == 0) return "晴";
        if (code <= 3) return "曇";
        if (code <= 49) return "霧";
        if (code <= 69) return "雨";
        if (code <= 79) return "雪";
        if (code <= 82) return "雨";
        if (code <= 86) return "雪";
        return "雷雨";
    }

    public record WeatherResult(Map<String, String> weather, boolean unavailable) {}
}
