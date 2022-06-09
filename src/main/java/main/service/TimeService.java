package main.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class TimeService {
    public long getTimestampFromLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime == null ? 0 : localDateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    public long getNowTimestamp() {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    public LocalDateTime checkDateCreationPost(long time) {
        LocalDateTime localDateTime = Instant.ofEpochSecond(time).atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (localDateTime.isBefore(LocalDateTime.now())) {
            localDateTime = LocalDateTime.now();
        }
        return localDateTime;
    }
}
