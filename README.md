# BootGuard 🛡️

A lightning-fast, zero-config CLI tool to scan Spring Boot projects for security risks and misconfigurations. 

Because waiting 10 minutes for your CI/CD pipeline to tell you your Actuator is exposed is 10 minutes too long.

## Features
- **Instant Local Scanning:** Runs in sub-seconds.
- **Spring Boot Specific:** Detects framework-specific nuances that generic static analysis tools miss.
- **Checks Include:**
  - Exposed Actuator endpoints
  - Hardcoded secrets and database credentials
  - Debug mode enabled in production
  - Sensitive files committed (like `.env`, `application-prod.yml`)

## Installation

Download the latest executable JAR from the [Releases](#) page.

Requires **Java 17+**.

## Usage

Simply run the tool against your Spring Boot project directory:

```bash
java -jar bootguard.jar /path/to/your/spring-boot-project
```

### Example Output

```text
Scanning project at: ../demo-app

[WARNING] Hardcoded database password found in application.yml
[CRITICAL] Actuator endpoints are exposed to the web!
[INFO] Scan completed in 0.8 seconds.
```

## Contributing

Pull requests are welcome! If you find a new Spring Boot misconfiguration pattern, feel free to add a new Scanner logic.

## License
MIT License
