package com.acme.salary.csv;

import com.acme.salary.csv.dto.ImportResultResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** {@code GET /api/employees/export} and {@code POST /api/employees/import} (REQUIREMENTS.md P1). */
@RestController
@RequestMapping("/api/employees")
public class CsvController {

    private final CsvExportService exportService;
    private final CsvImportService importService;

    public CsvController(CsvExportService exportService, CsvImportService importService) {
        this.exportService = exportService;
        this.importService = importService;
    }

    /** Exports the current filtered view: the same filters the directory search accepts. */
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer department,
            @RequestParam(required = false) Integer jobTitle,
            @RequestParam(required = false) Integer level,
            HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"employees.csv\"");
        exportService.export(response.getWriter(), q, country, department, jobTitle, level);
    }

    @PostMapping("/import")
    public ImportResultResponse importCsv(@RequestParam("file") MultipartFile file) throws IOException {
        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            return importService.importCsv(reader);
        }
    }
}
