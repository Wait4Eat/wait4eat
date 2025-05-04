package com.example.wait4eat.domain.dashboard.batch.writer;

import com.example.wait4eat.domain.payment.entity.Payment;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@StepScope
public class PaymentStatsWriter implements ItemWriter<Payment>, StepExecutionListener {
    private BigDecimal dailyTotalSales = BigDecimal.ZERO;

    @Override
    public void write(Chunk<? extends Payment> payments) {
        for (Payment payment : payments) {
            dailyTotalSales = dailyTotalSales.add(payment.getAmount());
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        stepExecution.getExecutionContext().put("dailyTotalSales", dailyTotalSales);
        return ExitStatus.COMPLETED;
    }
}
