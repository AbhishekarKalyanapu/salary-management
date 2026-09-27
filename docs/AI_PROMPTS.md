# AI Prompts & Development Instructions

## Purpose

AI assistance was used as a development aid for implementation, debugging, testing, documentation, and review. The developer remained responsible for reviewing generated suggestions, running the application, validating API behavior, and deciding which changes were accepted.

## Architecture & Planning Prompt

> Act as a senior Java full-stack developer. Design an employee salary management application for an HR Manager managing approximately 10,000 employees across multiple countries. Use Java 21, Spring Boot, SQL Server, Flyway, and Angular. Prioritize maintainability, validation, auditability, server-side pagination, SQL-based analytics, and deterministic seed data. Identify deliberate out-of-scope features and explain the main architectural trade-offs.

## Backend Implementation Prompt

> Implement the requested backend feature using Spring Boot and existing project conventions. Keep business logic in testable service/domain classes, validate reference data, use DTOs at API boundaries, return appropriate HTTP status codes and RFC 7807 errors, and preserve existing database and migration behavior. Do not modify unrelated functionality.

## Database & Performance Prompt

> Review the SQL Server implementation for a dataset of approximately 10,000 employees. Use appropriate indexes, server-side filtering/pagination, SQL aggregation for analytics, and avoid loading the complete employee dataset into application memory. Identify potential SQL Server limitations and propose practical fixes.

## Frontend Implementation Prompt

> Implement the requested Angular feature using standalone components and the existing application structure. Keep the sidebar/navigation available while only the relevant content area scrolls. Use services for HTTP communication, provide loading and error states, validate user input, and preserve the existing visual structure.

## Debugging Prompt

> Diagnose the reported error from the exact compiler/runtime output. Identify the root cause before changing code. Make the smallest targeted change possible, preserve existing behavior, and provide the exact file and command needed to verify the fix.

## Testing Prompt

> Add or correct focused tests for the requested functionality. Tests should be deterministic, easy to understand, and independent of external services where possible. For Angular services that depend on HttpClient, configure the appropriate HTTP provider in the test environment. Verify the complete test suite after changes.

## Data Seeding Prompt

> Create deterministic seed data for 10,000 employees. Keep reference data synchronized with the database migrations, constrain generated values according to business rules, generate salary history consistently, and ensure repeated runs with the same parameters produce reproducible results.

## Code Review Prompt

> Review the implementation against the documented requirements. Check correctness, validation, error handling, maintainability, performance, test coverage, security considerations, and unintended scope expansion. Report concrete issues and recommend only changes that are justified by the requirements.

## AI Usage Principles

- AI-generated code was reviewed before being accepted.
- Application behavior was verified through builds, automated tests, API calls, and manual UI testing.
- AI suggestions were not treated as authoritative.
- Existing working functionality was preserved wherever possible.
- Changes were made incrementally and committed to Git.