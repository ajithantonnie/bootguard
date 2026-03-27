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

### Options

| Flag | Description | Default |
|------|-------------|---------|
| `-f, --fail-on` | Fail with non-zero exit code if issues of this severity or higher are found. Valid values: `HIGH`, `MEDIUM`, `LOW` | `HIGH` |

### Example Output

```text
Scanning project at: ../demo-app

[WARNING] Hardcoded database password found in application.yml
[CRITICAL] Actuator endpoints are exposed to the web!
[INFO] Scan completed in 0.8 seconds.
```

## Git Pre-commit Integration

BootGuard can be integrated into your Git workflow to block commits that contain security risks.

### Option 1: Using the `pre-commit` framework

Add BootGuard to your `.pre-commit-config.yaml`:

```yaml
repos:
  - repo: https://github.com/ajithantonnie/bootguard
    rev: v1.0.0 # Use the latest version
    hooks:
      - id: bootguard-scan
```

### Option 2: Standalone Git Hook

Copy the provided hook script to your `.git/hooks/` directory:

```bash
cp scripts/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

By default, the hook will automatically download the latest BootGuard release to `~/.bootguard/bootguard.jar` if not present, and run it with `--fail-on HIGH` to check your project.

## Contributing

Pull requests are welcome! If you find a new Spring Boot misconfiguration pattern, feel free to add a new Scanner logic.

## License
MIT License
