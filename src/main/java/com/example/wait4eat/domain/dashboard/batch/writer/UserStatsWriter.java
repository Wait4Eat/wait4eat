package com.example.wait4eat.domain.dashboard.batch.writer;

import com.example.wait4eat.domain.user.entity.User;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@StepScope
public class UserStatsWriter implements ItemWriter<User>, StepExecutionListener {
    private Long totalUserCount = 0L;
    private Long dailyUserCount = 0L;

    @Override
    public void write(Chunk<? extends User> users) {
        for (User user : users) {
            totalUserCount ++;
            if (isLoggedInYesterday(user)) {
                dailyUserCount++;
            }
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        stepExecution.getExecutionContext().putLong("totalUserCount", totalUserCount);
        stepExecution.getExecutionContext().putLong("dailyUserCount", dailyUserCount);
        return ExitStatus.COMPLETED;
    }

    private boolean isLoggedInYesterday(User user) {
        LocalDate lastLoginDate = user.getLastLoginDate();
        return lastLoginDate != null && lastLoginDate.equals(LocalDate.now().minusDays(1));
    }
}
