package com.example.todoapp.api;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

@Component
public class HolidayClient {

    private static final String HOLIDAYS_URL = "https://holidays-jp.github.io/v1/date.json";

    private final RestClient restClient;

    public HolidayClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3_000);
        requestFactory.setReadTimeout(5_000);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public HolidayResult getHolidays(LocalDate from, LocalDate to) {
        Map<String, String> holidays;
        try {
            holidays = restClient.get()
                    .uri(HOLIDAYS_URL)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, String>>() {
                    });
        } catch (RestClientException e) {
            return new HolidayResult(Map.of(), true);
        }

        if (holidays == null) {
            return new HolidayResult(Map.of(), true);
        }

        if (from == null && to == null) {
            return new HolidayResult(holidays, false);
        }

        Map<String, String> filteredHolidays = new LinkedHashMap<>();
        holidays.forEach((date, name) -> {
            LocalDate holidayDate = LocalDate.parse(date);
            boolean afterFrom = from == null || !holidayDate.isBefore(from);
            boolean beforeTo = to == null || !holidayDate.isAfter(to);
            if (afterFrom && beforeTo) {
                filteredHolidays.put(date, name);
            }
        });
        return new HolidayResult(filteredHolidays, false);
    }

    public record HolidayResult(Map<String, String> holidays, boolean unavailable) {
    }
}
