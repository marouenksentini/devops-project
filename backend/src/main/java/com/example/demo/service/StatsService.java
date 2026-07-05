package com.example.demo.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;

@Service
public class StatsService {

    private final JdbcTemplate jdbcTemplate;

    public StatsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS clicks (id INTEGER PRIMARY KEY, count INTEGER NOT NULL)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS visitors (id INTEGER PRIMARY KEY AUTOINCREMENT, ip TEXT, visited_at TEXT)");

        Integer rows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM clicks", Integer.class);
        if (rows == null || rows == 0) {
            jdbcTemplate.update("INSERT INTO clicks (id, count) VALUES (1, 0)");
        }
    }

    public synchronized long incrementClicks() {
        jdbcTemplate.update("UPDATE clicks SET count = count + 1 WHERE id = 1");
        return getClicks();
    }

    public long getClicks() {
        Long count = jdbcTemplate.queryForObject("SELECT count FROM clicks WHERE id = 1", Long.class);
        return count == null ? 0 : count;
    }

    public long recordVisit(String ip) {
        jdbcTemplate.update("INSERT INTO visitors (ip, visited_at) VALUES (?, ?)", ip, Instant.now().toString());
        return getVisitorCount();
    }

    public long getVisitorCount() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM visitors", Long.class);
        return count == null ? 0 : count;
    }
}
