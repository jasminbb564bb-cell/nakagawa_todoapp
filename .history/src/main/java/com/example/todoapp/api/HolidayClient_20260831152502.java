package com.example.todoapp.api;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HolidayClient {

    private static final String HOLIDAYS_URL =
            "https://holidays-jp.github.io/api/v1/date.json";

    private final RestClient restClient = RestClient.create();

    public Map<String, String> getHolidays(LocalDate from, LocalDate to) {
        Map<String, String> holidays = restClient.get()
                .uri(HOLIDAYS_URL)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, String>>() {});

        if (from == null && to == null) {
            return holidays;
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
        return filteredHolidays;
    }
}
