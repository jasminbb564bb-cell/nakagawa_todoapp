package com.example.todoapp.api;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolidayApiController {

    private final HolidayClient holidayClient;

    public HolidayApiController(HolidayClient holidayClient) {
        this.holidayClient = holidayClient;
    }

    @GetMapping("/api/holidays")
    public Map<String, String> holidays(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return holidayClient.getHolidays(from, to);
    }
}
