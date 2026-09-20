package com.acme.salary.insights;

import com.acme.salary.domain.OutlierDetector;
import com.acme.salary.insights.dto.DimensionStatsResponse;
import com.acme.salary.insights.dto.DistributionBucketResponse;
import com.acme.salary.insights.dto.OutlierResponse;
import com.acme.salary.insights.dto.SummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/** The pay-insights dashboard API, matching DESIGN.md's API table under {@code /api/insights}. */
@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping("/summary")
    public SummaryResponse summary() {
        return insightsService.summary();
    }

    @GetMapping("/by/{dimension}")
    public List<DimensionStatsResponse> byDimension(@PathVariable String dimension) {
        return insightsService.byDimension(dimension);
    }

    @GetMapping("/distribution")
    public List<DistributionBucketResponse> distribution() {
        return insightsService.distribution();
    }

    @GetMapping("/outliers")
    public List<OutlierResponse> outliers(
            @RequestParam(defaultValue = "20") BigDecimal thresholdPct,
            @RequestParam(defaultValue = "" + OutlierDetector.DEFAULT_MIN_GROUP_SIZE) int minGroupSize) {
        return insightsService.outliers(thresholdPct, minGroupSize);
    }
}
