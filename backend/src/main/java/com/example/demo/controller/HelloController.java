package com.example.demo.controller;

import com.example.demo.service.StatsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HelloController {

    private static final ZoneId TUNIS_ZONE = ZoneId.of("Africa/Tunis");
    private final StatsService statsService;

    public HelloController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot + Jenkins + Docker!";
    }

    @GetMapping("/health")
    public String health() {
        return "UP";
    }

    @GetMapping("/time")
    public Map<String, String> time() {
        ZonedDateTime now = ZonedDateTime.now(TUNIS_ZONE);
        Map<String, String> body = new LinkedHashMap<>();
        body.put("timezone", "Africa/Tunis");
        body.put("iso", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        body.put("display", now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy - HH:mm:ss")));
        return body;
    }

    @GetMapping("/click")
    public Map<String, Long> getClicks() {
        return Map.of("count", statsService.getClicks());
    }

    @PostMapping("/click")
    public Map<String, Long> click() {
        return Map.of("count", statsService.incrementClicks());
    }

    @GetMapping("/visit")
    public Map<String, Long> visit(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return Map.of("visitors", statsService.recordVisit(ip));
    }

    @GetMapping("/visit/count")
    public Map<String, Long> visitCount() {
        return Map.of("visitors", statsService.getVisitorCount());
    }
}
