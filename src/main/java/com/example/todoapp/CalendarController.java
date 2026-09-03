package com.example.todoapp;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String calendar(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "view", required = false, defaultValue = "month") String view,
            @RequestParam(name = "from", required = false) String from,
            Model model) {
        LocalDate today = LocalDate.now();
        if ("week".equals(view)) {
            LocalDate requested = from == null ? today : LocalDate.parse(from);
            LocalDate weekStart = requested.minusDays(requested.getDayOfWeek().getValue() % 7);
            List<CalendarDay> week = new ArrayList<>();
            for (int index = 0; index < 7; index++) week.add(new CalendarDay(weekStart.plusDays(index)));
            model.addAttribute("year", weekStart.getYear());
            model.addAttribute("month", weekStart.getMonthValue());
            model.addAttribute("monthTitle", weekStart + " の週");
            model.addAttribute("from", weekStart);
            model.addAttribute("to", weekStart.plusDays(6));
            model.addAttribute("weeks", List.of(week));
            model.addAttribute("isWeekView", true);
            model.addAttribute("previousFrom", weekStart.minusDays(7));
            model.addAttribute("nextFrom", weekStart.plusDays(7));
            return "calendar";
        }
        int displayYear = year == null ? today.getYear() : year;
        int displayMonth = month == null ? today.getMonthValue() : month;
        YearMonth target = YearMonth.of(displayYear, displayMonth);

        LocalDate firstDay = target.atDay(1);
        LocalDate lastDay = target.atEndOfMonth();
        int leadingEmptyDays = firstDay.getDayOfWeek().getValue() % 7;
        int cellCount = leadingEmptyDays + target.lengthOfMonth();
        int trailingEmptyDays = (7 - cellCount % 7) % 7;

        List<List<CalendarDay>> weeks = new ArrayList<>();
        List<CalendarDay> week = new ArrayList<>();
        for (int index = 0; index < leadingEmptyDays + target.lengthOfMonth() + trailingEmptyDays; index++) {
            LocalDate date = index < leadingEmptyDays || index >= leadingEmptyDays + target.lengthOfMonth()
                    ? null
                    : firstDay.plusDays(index - leadingEmptyDays);
            week.add(new CalendarDay(date));
            if (week.size() == 7) {
                weeks.add(week);
                week = new ArrayList<>();
            }
        }

        model.addAttribute("year", target.getYear());
        model.addAttribute("month", target.getMonthValue());
        model.addAttribute("monthTitle", target.getYear() + "年" + target.getMonthValue() + "月");
        model.addAttribute("from", firstDay);
        model.addAttribute("to", lastDay);
        model.addAttribute("weeks", weeks);
        model.addAttribute("isWeekView", false);
        YearMonth previous = target.minusMonths(1);
        YearMonth next = target.plusMonths(1);
        model.addAttribute("previousYear", previous.getYear());
        model.addAttribute("previousMonth", previous.getMonthValue());
        model.addAttribute("nextYear", next.getYear());
        model.addAttribute("nextMonth", next.getMonthValue());
        return "calendar";
    }

    public static class CalendarDay {
        private final LocalDate date;

        public CalendarDay(LocalDate date) {
            this.date = date;
        }

        public LocalDate getDate() {
            return date;
        }
    }
}
