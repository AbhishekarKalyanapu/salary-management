# Salary Management — Requirements

## 1. Goal

Build a web-based Employee Salary Management system for an organization with approximately 10,000 employees. The application enables an HR Manager to manage employee salary information and understand how the organization pays people through salary analytics and insights.

The solution replaces spreadsheet-based salary management with a structured, searchable, validated, and maintainable application.

## 2. User Persona

**Primary User:** HR Manager

The HR Manager should be able to:

* View and manage employee information.
* Create, update, and delete employee records.
* View salary information and salary history.
* Search and filter employee records.
* Understand salary distribution and organizational salary patterns.
* Import and export salary data using CSV files.
* Identify invalid or inconsistent salary data.

## 3. Scope & Features

### Employee Management

* Display employee records in a paginated/list-based UI.
* Add new employees.
* Edit existing employees.
* Delete employees.
* Search and filter employees.
* Validate employee and reference-data fields.
* Maintain employee creation and update timestamps.

### Salary Management

* Store employee salary information.
* Record salary changes.
* Maintain salary history.
* View salary information associated with employees.
* Validate salary-related data.

### Dashboard & Insights

Provide HR-focused salary analytics including:

* Total employee count.
* Total and average salary information.
* Salary distribution.
* Salary breakdown by relevant dimensions such as department.
* Salary distribution histogram.
* Salary insights that help HR understand organizational pay patterns.
* Outlier analysis with a clear empty state when no outliers are detected.

### CSV Import & Export

* Export employee/salary data to CSV.
* Import employee/salary data from CSV.
* Validate imported rows.
* Report row-level validation errors.
* Allow valid records to be processed while clearly reporting invalid data.

### Navigation & UI

The application provides dedicated navigation for:

* Employees
* Dashboard
* Insights
* Salary
* CSV

The application uses a responsive layout with persistent navigation while employee content can scroll independently.

## 4. Technical Requirements

### Backend

* Java
* Spring Boot
* Spring Data JPA / Hibernate
* REST APIs
* Relational database
* Structured error responses using RFC 7807 Problem Details
* Validation of request and reference data

### Frontend

* Angular
* TypeScript
* HTML/CSS
* Responsive UI
* REST API integration
* Component-based architecture

### Database

The application uses a relational database to persist:

* Employee information
* Salary information
* Salary history
* Reference data

## 5. Data & Seeding

The assessment targets an organization with approximately **10,000 employees**.

The system should support large employee datasets while maintaining acceptable usability and API performance.

Seed/sample data should be deterministic and suitable for development, testing, and demonstration.

## 6. Validation & Error Handling

The application should:

* Validate required fields.
* Validate reference-data values.
* Validate salary-related values.
* Return meaningful API errors.
* Use structured error responses.
* Provide clear validation feedback for CSV imports.
* Avoid silently accepting invalid data.

## 7. Testing

The solution should include meaningful automated tests covering core functionality.

Tests should be:

* Fast.
* Deterministic.
* Easy to understand.
* Focused on important business logic and API behavior.

## 8. Architecture & Maintainability

The solution should follow a clear separation of responsibilities between:

* Angular UI
* REST API layer
* Business/service layer
* Data-access layer
* Relational database

Code should be readable, maintainable, and organized for future enhancements.

## 9. Performance Considerations

The application is designed with the target of approximately 10,000 employees in mind.

Important considerations include:

* Efficient database queries.
* Pagination for employee records.
* Appropriate filtering and searching.
* Efficient API responses.
* Avoiding unnecessary frontend rendering.
* Page-wise/independent UI scrolling where appropriate.

## 10. Deliberately Out of Scope

To keep the assessment focused, the following are deliberately excluded unless required later:

* Payroll processing and salary payment execution.
* Tax calculation and statutory payroll compliance.
* Employee attendance management.
* Leave management.
* Recruitment workflows.
* Employee self-service functionality.
* Complex role/permission administration beyond the HR Manager use case.
* Advanced forecasting or machine-learning-based salary prediction.
* Integration with external payroll providers.

## 11. Development & Delivery Expectations

The project should demonstrate:

* Incremental development through meaningful Git commits.
* Clean and maintainable code.
* Automated tests for core functionality.
* Clear engineering and product decisions.
* Intentional use of AI tools during development.
* Supporting artifacts such as requirements, design notes, architecture information, and trade-off decisions where useful.

## 12. Success Criteria

The solution is considered successful when an HR Manager can:

1. Manage employee records through the web application.
2. Manage and review employee salary information.
3. View salary history and salary changes.
4. Analyze salary distribution and organizational salary patterns.
5. Import and export salary data using CSV.
6. Receive clear validation and error feedback.
7. Work with a dataset representing approximately 10,000 employees.
8. Use the application through a responsive and maintainable UI.

The primary objective is not to build the most complex system, but to demonstrate sound product thinking, clear architecture, good engineering practices, and a fully functional end-to-end solution.
