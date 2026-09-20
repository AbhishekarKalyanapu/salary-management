package com.acme.salary.insights.dto;

import java.math.BigDecimal;

/** One bar of the salary distribution histogram, in USD. */
public record DistributionBucketResponse(BigDecimal rangeStart, BigDecimal rangeEnd, int count) {
}
