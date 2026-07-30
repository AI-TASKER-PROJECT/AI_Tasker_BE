package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;

import java.time.LocalDateTime;
import java.util.Locale;

public record MilestoneReviewSlaDuration(int value, Unit unit) {
    public static final String SETTING_KEY = "milestone_review_sla_duration";
    public static final MilestoneReviewSlaDuration DEFAULT = new MilestoneReviewSlaDuration(3, Unit.DAY);

    public enum Unit { MINUTE, HOUR, DAY }

    public MilestoneReviewSlaDuration {
        if (value <= 0) throw new AppException("THOI GIAN SLA PHAI LON HON 0");
        if (unit == null) throw new AppException("DON VI SLA KHONG HOP LE");
    }

    public static MilestoneReviewSlaDuration parse(String raw) {
        if (raw == null || raw.isBlank()) throw new AppException("THOI GIAN SLA KHONG DUOC DE TRONG");
        String[] parts = raw.trim().toUpperCase(Locale.ROOT).split(":", -1);
        if (parts.length != 2) throw invalidFormat();
        try {
            return new MilestoneReviewSlaDuration(Integer.parseInt(parts[0].trim()), Unit.valueOf(parts[1].trim()));
        } catch (IllegalArgumentException exception) {
            throw invalidFormat();
        }
    }

    public LocalDateTime addTo(LocalDateTime start) {
        if (start == null) throw new AppException("THOI DIEM BAT DAU SLA KHONG HOP LE");
        return switch (unit) {
            case MINUTE -> start.plusMinutes(value);
            case HOUR -> start.plusHours(value);
            case DAY -> start.plusDays(value);
        };
    }

    public String serialize() {
        return value + ":" + unit.name();
    }

    private static AppException invalidFormat() {
        return new AppException("THOI GIAN SLA PHAI CO DINH DANG SO:MINUTE|HOUR|DAY");
    }
}
