package com.gracemann.recon.matchengine.dto;

import java.time.LocalDateTime;

public class BatchWindowConfig {
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;

    // Getters & Setters
    public LocalDateTime getWindowStart() { return windowStart; }
    public void setWindowStart(LocalDateTime windowStart) { this.windowStart = windowStart; }

    public LocalDateTime getWindowEnd() { return windowEnd; }
    public void setWindowEnd(LocalDateTime windowEnd) { this.windowEnd = windowEnd; }
}
