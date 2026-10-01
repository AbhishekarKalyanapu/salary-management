\# Employee Salary Management



A full-stack employee salary management application designed for HR Managers to manage employee records, salary changes, salary history, CSV data exchange, and salary analytics.



The application is designed to handle an organization with approximately 10,000 employees and provides a searchable, validated, auditable alternative to managing salary information through spreadsheets.



\## Features



\### Employee Management



\* View employees with server-side pagination.

\* Search and filter employees.

\* Add new employees.

\* Edit employee information.

\* Delete employees.

\* Validate required fields and reference data.

\* Prevent duplicate employee email addresses.



\### Salary Management



\* Select an employee and update their salary.

\* Record an effective date and reason for every salary change.

\* Maintain salary history.

\* Display previous salary, new salary, percentage change, reason, and timestamp.



\### Dashboard \& Insights



\* Total employee count.

\* Average salary.

\* Minimum and maximum salary.

\* Salary breakdown by country and department.

\* Salary distribution/histogram.

\* Salary outlier detection.



\### CSV Import \& Export



\* Export employee salary data to CSV.

\* Import employee data from CSV.

\* Row-level validation and error reporting.

\* Duplicate email validation.

\* Successfully validated rows are created while invalid rows are reported.



\## Technology Stack



| Layer              | Technology                               |

| ------------------ | ---------------------------------------- |

| Frontend           | Angular                                  |

| Backend            | Java 21, Spring Boot                     |

| Database           | Microsoft SQL Server 2022                |

| ORM                | Spring Data JPA / Hibernate              |

| Database Migration | Flyway                                   |

| API                | REST                                     |

| Containerization   | Docker / Docker Compose                  |

| Testing            | JUnit / Spring Boot Test / Angular Tests |



\## Architecture



```text

&#x20;                   ┌─────────────────────┐

&#x20;                   │   Angular Frontend  │

&#x20;                   │      Port 4200      │

&#x20;                   └──────────┬──────────┘

&#x20;                              │ REST API

&#x20;                              ▼

&#x20;                   ┌─────────────────────┐

&#x20;                   │   Spring Boot API   │

&#x20;                   │      Port 8080      │

&#x20;                   └──────────┬──────────┘

&#x20;                              │

&#x20;         ┌────────────────────┼────────────────────┐

&#x20;         │                    │                    │

&#x20;         ▼                    ▼                    ▼

&#x20;  Employee Module      Salary Module       Insights Module

&#x20;         │                    │                    │

&#x20;         └────────────────────┼────────────────────┘

&#x20;                              ▼

&#x20;                   ┌─────────────────────┐

&#x20;                   │    SQL Server 2022  │

&#x20;                   │      Port 1433      │

&#x20;                   └─────────────────────┘



&#x20;                   ┌─────────────────────┐

&#x20;                   │ Deterministic Seed  │

&#x20;                   │     10,000 records  │

&#x20;                   └─────────────────────┘

```



Detailed architecture and design decisions are documented in:



\* `docs/REQUIREMENTS.md`

\* `docs/DESIGN.md`

\* `docs/AI\_PROMPTS.md`



\## Project Structure



```text

salary-management/

├── backend/

│   ├── src/

│   │   ├── main/

│   │   └── test/

│   ├── Dockerfile

│   └── pom.xml

│

├── frontend/

│   ├── src/

│   ├── Dockerfile

│   ├── nginx.conf

│   └── package.json

│

├── docs/

│   ├── REQUIREMENTS.md

│   ├── DESIGN.md

│   └── AI\_PROMPTS.md

│

├── docker-compose.yml

├── .gitignore

└── README.md

```



\## Prerequisites



For Docker-based execution:



\* Docker Desktop

\* Docker Compose



For local development:



\* Java 21

\* Maven 3.9+

\* Node.js 22+

\* npm

\* SQL Server 2022



\## Running with Docker



From the project root:



```powershell

docker compose up -d

```



Check the containers:



```powershell

docker compose ps

```



The application is available at:



```text

Frontend: http://localhost:4200

Backend:  http://localhost:8080

```



Stop the application:



```powershell

docker compose down

```



The SQL Server data is stored in the Docker volume `mssql-data`.



\## Seed 10,000 Employees



The application includes a deterministic seed runner for generating 10,000 employees.



After the database and backend containers are available:



```powershell

docker compose run --rm backend java -jar app.jar --spring.profiles.active=seed

```



The seed runner uses deterministic data generation so that test/demo data can be reproduced consistently.



\## Running Backend Locally



Navigate to the backend:



```powershell

cd backend

```



Run:



```powershell

mvn spring-boot:run

```



The backend runs on:



```text

http://localhost:8080

```



\## Running Frontend Locally



Open another terminal and navigate to:



```powershell

cd frontend

```



Install dependencies:



```powershell

npm install

```



Start Angular:



```powershell

npm start

```



The frontend runs on:



```text

http://localhost:4200

```



\## API Overview



\### Employee APIs



```text

GET    /api/employees

GET    /api/employees/{id}

POST   /api/employees

PUT    /api/employees/{id}

DELETE /api/employees/{id}

```



\### Salary APIs



```text

POST /api/employees/{id}/salary

GET  /api/employees/{id}/salary-history

```



\### Insights APIs



The backend provides endpoints for:



\* Salary summary

\* Salary breakdown by dimension

\* Salary distribution

\* Salary outlier detection



\### Reference Data APIs



```text

GET /api/reference/countries

GET /api/reference/currencies

GET /api/reference/departments

GET /api/reference/job-titles

```



\## Database



The application uses Microsoft SQL Server with Flyway database migrations.



Main tables include:



```text

country

currency

department

job\_title

employee

salary\_history

flyway\_schema\_history

```



Hibernate schema validation is enabled so that application startup validates the database schema rather than automatically modifying it.



\## Validation \& Error Handling



The application validates:



\* Required employee fields.

\* Email format.

\* Duplicate employee email addresses.

\* Country and currency reference data.

\* Department and job title reference data.

\* Salary values.

\* Effective dates.

\* Salary change reasons.

\* CSV rows individually.



Backend validation errors use structured HTTP error responses, including RFC 7807-style problem details.



\## Testing



\### Backend



Run:



```powershell

cd backend

mvn test

```



The backend test suite covers employee management, salary management, validation, insights, CSV processing, and related service/repository behavior.



\### Frontend



Run:



```powershell

cd frontend

npm test -- --watch=false --browsers=ChromeHeadless

```



Current verified test status:



```text

Angular: 11 tests passing

Backend: 62 tests passing

```



\## Performance Considerations



The application is designed for approximately 10,000 employees.



Key considerations include:



\* Server-side employee pagination.

\* Database-side analytical queries.

\* Indexed/searchable employee fields.

\* Batched processing for large ID collections.

\* Deterministic seed generation.

\* Avoiding loading unnecessary data for individual operations.



\## Key Design Decisions



\### Server-side pagination



Employee records are paginated by the backend rather than loading the complete employee table for the main employee list.



\### Database-side analytics



Salary aggregations and analytical calculations are performed close to the data using SQL/database queries rather than transferring large datasets to the frontend.



\### Deterministic seed data



A fixed seed value allows repeatable generation of representative employee data.



\### Flyway migrations



Database structure and reference data are managed through versioned migrations.



\### Separate salary history



Salary changes are stored in a dedicated history table so salary changes remain traceable rather than overwriting historical information.



\### Dockerized application



Docker Compose provides a consistent local environment containing SQL Server, the backend, and the frontend.



\## Deliberately Out of Scope



The following are intentionally outside the assessment scope:



\* Production SSO/identity integration.

\* Advanced role-based access control.

\* Real-time foreign exchange rate integration.

\* Payroll processing.

\* Employee authentication.

\* Email notification workflows.

\* Full production monitoring infrastructure.

\* Cloud-specific deployment configuration.



These can be added in a production system depending on business requirements.



\## Documentation



Additional project documentation:



```text

docs/REQUIREMENTS.md

docs/DESIGN.md

docs/AI\_PROMPTS.md

```



`REQUIREMENTS.md` describes the business goal, scope, features, validation, testing, architecture, performance considerations, and deliberate out-of-scope items.



`DESIGN.md` contains architecture, API design, data model decisions, performance considerations, testing strategy, and trade-offs.



`AI\_PROMPTS.md` documents how AI assistance was used during architecture, development, debugging, testing, and code review.



\## Development Approach



The project was developed incrementally with focused Git commits for individual features and improvements.



The implementation emphasizes:



\* Maintainable feature-based organization.

\* Clear separation between frontend, backend, and database responsibilities.

\* Validation at appropriate layers.

\* Testable business logic.

\* Deterministic seed data.

\* Documentation of architectural trade-offs.

\* Small, reviewable Git commits.



\## Application Verification



The following end-to-end functionality has been manually verified:



\* Employee creation.

\* Employee editing.

\* Employee deletion.

\* Salary updates.

\* Salary history.

\* CSV import.

\* CSV export.

\* Dashboard analytics.

\* Insights and outlier detection.

\* Docker-based application startup.



\## License



This project was developed as part of a technical assessment.



