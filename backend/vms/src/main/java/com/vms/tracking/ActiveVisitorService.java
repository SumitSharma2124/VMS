package com.vms.tracking;

import com.vms.tracking.dto.ActiveVisitorDto;
import com.vms.visit.Visit;
import com.vms.visit.VisitRepository;
import com.vms.visit.VisitStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@Service
public class ActiveVisitorService {
    private final VisitRepository visitRepository;
    private final StringRedisTemplate redisTemplate;

    public ActiveVisitorService(VisitRepository visitRepository, StringRedisTemplate redisTemplate) {
        this.visitRepository = visitRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional(readOnly = true)
    public List<ActiveVisitorDto> listActive(Long orgId) {
        return visitRepository.findByOrgIdAndStatusIn(orgId, List.of(VisitStatus.CHECKED_IN, VisitStatus.OVERDUE))
                .stream()
                .sorted(Comparator.comparing(Visit::getCheckInTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(visit -> new ActiveVisitorDto(
                        visit.getId(), visit.getVisitCode(), visit.getVisitor().getFullName(),
                        visit.getHostName(), visit.getCheckInTime(), visit.getExpectedEndTime(), visit.getStatus()))
                .toList();
    }

    @Transactional(readOnly = true)
    public long count(Long orgId) {
        try {
            String value = redisTemplate.opsForValue().get("active:" + orgId);
            if (value != null) {
                return Long.parseLong(value);
            }
            long count = databaseCount(orgId);
            redisTemplate.opsForValue().set("active:" + orgId, Long.toString(count), Duration.ofSeconds(60));
            return count;
        } catch (RuntimeException exception) {
            return databaseCount(orgId);
        }
    }

    public void evict(Long orgId) {
        try {
            redisTemplate.delete("active:" + orgId);
        } catch (RuntimeException ignored) {
            // Redis is only a cache; the database remains authoritative.
        }
    }

    private long databaseCount(Long orgId) {
        return visitRepository.countByOrgIdAndStatusIn(
                orgId, List.of(VisitStatus.CHECKED_IN, VisitStatus.OVERDUE));
    }
}
