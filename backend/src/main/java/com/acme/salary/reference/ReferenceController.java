package com.acme.salary.reference;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reference")
public class ReferenceController {

    private final CountryRepository countryRepository;
    private final CurrencyRepository currencyRepository;
    private final DepartmentRepository departmentRepository;
    private final JobTitleRepository jobTitleRepository;

    public ReferenceController(
            CountryRepository countryRepository,
            CurrencyRepository currencyRepository,
            DepartmentRepository departmentRepository,
            JobTitleRepository jobTitleRepository) {

        this.countryRepository = countryRepository;
        this.currencyRepository = currencyRepository;
        this.departmentRepository = departmentRepository;
        this.jobTitleRepository = jobTitleRepository;
    }

    @GetMapping("/countries")
    public List<CountryResponse> countries() {
        return countryRepository.findAll()
                .stream()
                .map(country -> new CountryResponse(
                        country.getCode(),
                        country.getName(),
                        country.getCurrency().getCode()))
                .toList();
    }

    @GetMapping("/currencies")
    public List<CurrencyResponse> currencies() {
        return currencyRepository.findAll()
                .stream()
                .map(currency -> new CurrencyResponse(
                        currency.getCode(),
                        currency.getRateToUsd(),
                        currency.getAsOf()))
                .toList();
    }

    @GetMapping("/departments")
    public List<DepartmentResponse> departments() {
        return departmentRepository.findAll()
                .stream()
                .map(department -> new DepartmentResponse(
                        department.getId(),
                        department.getName()))
                .toList();
    }

    @GetMapping("/job-titles")
    public List<JobTitleResponse> jobTitles() {
        return jobTitleRepository.findAll()
                .stream()
                .map(jobTitle -> new JobTitleResponse(
                        jobTitle.getId(),
                        jobTitle.getTitle(),
                        jobTitle.getDepartment().getId()))
                .toList();
    }

    public record CountryResponse(
            String code,
            String name,
            String currencyCode) {
    }

    public record CurrencyResponse(
            String code,
            java.math.BigDecimal rateToUsd,
            java.time.LocalDate asOf) {
    }

    public record DepartmentResponse(
            Integer id,
            String name) {
    }

    public record JobTitleResponse(
            Integer id,
            String title,
            Integer departmentId) {
    }
}