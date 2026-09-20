# ACME Salary Management: Requirements (v1)

## Goal
Replace the Excel workflow for ~10,000 employees across multiple countries with a web app that lets the HR Manager (a) maintain salary records reliably and (b) answer "how do we pay people?" in seconds, without building pivot tables.

**Success test:** the HR Manager can answer each of these in under 30 seconds:
- What is the median salary of Software Engineers in Germany?
- Which departments have the widest pay spread?
- Who is paid far above or below peers in the same role and country?
- What is our total annual payroll, and how is it split by country?

## Persona
One user: the HR Manager. Full read/write access to all salary data. Comfortable with Excel, not with SQL.

## In scope

**1. Employee directory (P0)**
- Server-side pagination, search (name/email), filters (country, department, job title, level) and sorting. It must stay fast at 10k rows.
- Create, edit and delete an employee, with validation (required fields, positive salary, valid country and currency, unique email).

**2. Salary changes with history (P0)**
- Every salary change is recorded (old, new, effective date, reason) and visible on the employee's page. HR routinely needs "what was it before, and when did it change?"

**3. Pay insights dashboard (P0)**
- Headline: headcount, total payroll, median salary.
- By country, department and job title: headcount, min, median, mean, p90, max.
- Salary distribution histogram (filterable).
- Outlier list: employees more than ±X% from the median of the same job title and country.

**4. Multi-currency handling (P0)**
- Salaries are stored in local currency, as the source of truth.
- Cross-country aggregates are shown in USD using a small, versioned FX table with an "as of" date. Per-country views stay in local currency.

**5. CSV export and import (P1)**
- Export the current filtered view.
- Import with row-level validation and an error report. This is the migration path off Excel.

**6. Seed script (P0)**
- Deterministic (fixed random seed) generation of 10,000 realistic employees across ~8 countries, ~8 departments and ~40 job titles, with country-adjusted salary ranges.

## Deliberately left out (and why)

| Left out | Reason |
|---|---|
| Authentication / RBAC / audit of *who* changed data | Single persona, synthetic demo data. **Real deployment with real salaries would need SSO and audit logging first**; this is the biggest production gap. |
| Live FX rates | Adds an external dependency and non-deterministic numbers. A static, dated table is enough to answer the questions. |
| Payroll, tax, benefits, bonus, equity | Different problem domain. Base annual salary is the question being asked. |
| Demographics / pay-equity analysis (e.g. gender gap) | Valuable, but sensitive data with legal implications; it needs its own design. Listed as the natural v2. |
| Org chart / managers, approval workflows, comp review cycles | Multi-user features with no second persona in v1. |
| Terminated-employee history | Directory covers current employees only, to keep the data model small. |
| i18n, notifications, mobile-specific UI | Not needed to answer the core questions. |

## Non-functional requirements
- List and insight queries respond in < 500 ms at 10k rows (indexes on country, department, job title, salary; aggregates computed in the database).
- Unit tests cover validation, salary-change history, FX conversion, and percentile/outlier logic. They are fast and deterministic: domain logic is tested without a database, and a small set of Testcontainers SQL Server integration tests covers the queries.
- One-command setup: install, migrate, seed, run.

## Key trade-offs
- **Stack: Java (Spring Boot) + Angular + SQL Server:** as specified for the role. The data is tabular and the questions are aggregations, so a relational DB is the natural fit; 10k rows is small for SQL Server.
- **Percentiles in SQL:** SQL Server offers `PERCENTILE_CONT` only as a window function, not an aggregate, so grouped medians need a specific query shape. It is isolated in one repository and covered by integration tests.
- **Server-side pagination over client-side:** keeps the UI responsive and scales past 10k.
