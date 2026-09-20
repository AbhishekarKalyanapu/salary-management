-- V2: reference data the application needs to work (not demo data).
-- FX rates are illustrative fixed values as of the date below, not live rates.

INSERT INTO currency (code, rate_to_usd, as_of) VALUES
    ('USD', 1.000000, '2026-09-01'),
    ('GBP', 1.270000, '2026-09-01'),
    ('EUR', 1.090000, '2026-09-01'),
    ('INR', 0.012000, '2026-09-01'),
    ('CAD', 0.730000, '2026-09-01'),
    ('AUD', 0.660000, '2026-09-01'),
    ('SGD', 0.740000, '2026-09-01'),
    ('BRL', 0.190000, '2026-09-01');

INSERT INTO country (code, name, currency_code) VALUES
    ('US', 'United States', 'USD'),
    ('IN', 'India', 'INR'),
    ('GB', 'United Kingdom', 'GBP'),
    ('DE', 'Germany', 'EUR'),
    ('BR', 'Brazil', 'BRL'),
    ('CA', 'Canada', 'CAD'),
    ('AU', 'Australia', 'AUD'),
    ('SG', 'Singapore', 'SGD');

INSERT INTO department (name) VALUES
    ('Engineering'),
    ('Product'),
    ('Design'),
    ('Data & Analytics'),
    ('Sales'),
    ('Marketing'),
    ('Finance'),
    ('Human Resources');

INSERT INTO job_title (title, department_id)
SELECT v.title, d.id
FROM (VALUES
    ('Software Engineer', 'Engineering'),
    ('QA Engineer', 'Engineering'),
    ('DevOps Engineer', 'Engineering'),
    ('Engineering Manager', 'Engineering'),
    ('Security Engineer', 'Engineering'),
    ('Product Manager', 'Product'),
    ('Technical Program Manager', 'Product'),
    ('Business Analyst', 'Product'),
    ('Product Operations Specialist', 'Product'),
    ('Director of Product', 'Product'),
    ('UX Designer', 'Design'),
    ('UI Designer', 'Design'),
    ('UX Researcher', 'Design'),
    ('Design Manager', 'Design'),
    ('Content Designer', 'Design'),
    ('Data Analyst', 'Data & Analytics'),
    ('Data Scientist', 'Data & Analytics'),
    ('Data Engineer', 'Data & Analytics'),
    ('BI Developer', 'Data & Analytics'),
    ('ML Engineer', 'Data & Analytics'),
    ('Account Executive', 'Sales'),
    ('Sales Development Representative', 'Sales'),
    ('Sales Manager', 'Sales'),
    ('Solutions Consultant', 'Sales'),
    ('Customer Success Manager', 'Sales'),
    ('Marketing Manager', 'Marketing'),
    ('Content Marketer', 'Marketing'),
    ('SEO Specialist', 'Marketing'),
    ('Brand Manager', 'Marketing'),
    ('Growth Marketer', 'Marketing'),
    ('Accountant', 'Finance'),
    ('Financial Analyst', 'Finance'),
    ('Controller', 'Finance'),
    ('Payroll Specialist', 'Finance'),
    ('Procurement Specialist', 'Finance'),
    ('Recruiter', 'Human Resources'),
    ('HR Business Partner', 'Human Resources'),
    ('Compensation Analyst', 'Human Resources'),
    ('Talent Acquisition Manager', 'Human Resources'),
    ('HR Coordinator', 'Human Resources')
) AS v (title, department_name)
JOIN department d ON d.name = v.department_name;
