# BootGuard 🛡️

A lightning-fast, zero-config CLI tool to scan Spring Boot projects for security risks and misconfigurations. 

Because waiting 10 minutes for your CI/CD pipeline to tell you your Actuator is exposed is 10 minutes too long.

## Features
- **Instant Local Scanning:** Runs in sub-seconds.
- **Spring Boot Specific:** Detects framework-specific nuances that generic static analysis tools miss.
- **Checks Include:**
  - **Actuator:** Smart Actuator endpoint exposure checks (ignores local-bound endpoints)
  - **Secrets:** Hardcoded credentials and tokens (Hybrid Detection: Regex + Metric Entropy)
  - **Environment:** Debug mode accidentally enabled in production
  - **Database:** Plain-text DB passwords and connections lacking SSL enforcement
  - **VCS Checks:** Highly sensitive files committed to Git (like `.env`, `application-prod.yml`)
  - **Networking:** Overly permissive CORS configurations (e.g., wildcard allowed origins)
  - **Consoles:** Unsecured or inadvertently exposed H2 Database web consoles
- **Context-Aware Downgrading:** Automatically lowers the severity of issues found within `dev`/`test`/`local` profile blocks or specific development files.
- **Fully Customizable:** Easily override security thresholds, regex detection patterns, weak passwords, and target ignore-directories using a custom `bootguard.properties`.

## Installation

Download the latest executable JAR from the [Releases](#) page.

Requires **Java 17+**.

## Usage

Simply run the tool against your Spring Boot project directory. By default, it will automatically search the target directory for a `bootguard.properties` to load custom overrides, or strictly adhere to its safe defaults if not found.

```bash
java -jar bootguard.jar /path/to/your/spring-boot-project
```

To explicitly provide a custom property file from another location:

```bash
java -jar bootguard.jar -c /path/to/shared-org-rules/bootguard.properties /path/to/your/spring-boot-project
```

### Testing with the Demo App

A deliberate vulnerable application is included in the `demo-app/` directory to demonstrate BootGuard capabilities.

> [!NOTE]
> To prevent secrets from leaking into Git history, the `demo-app/application.yml` file uses placeholders for API keys.
> To fully test the **Secrets Scanner**, you can manually temporarily replace `PLACEHOLDER_STRIPE_KEY_12345` and `PLACEHOLDER_GITHUB_PAT_12345` with standard string formats that BootGuard recognizes, such as a dummy Stripe key (`sk_test_<your_dummy_key>`) or a GitHub token (`ghp_<your_dummy_token>`).

### Options

| Flag | Description | Default |
|------|-------------|---------|
| `-f, --fail-on` | Fail with non-zero exit code if issues of this severity or higher are found. Valid values: `HIGH`, `MEDIUM`, `LOW` | `HIGH` |
| `-c, --config` | Provide a custom external configuration properties file to override detection thresholds. | *(Project root `bootguard.properties`)* |



### Example Output

```text
Scanning project at: ../demo-app

[WARNING] Hardcoded risk finding (Keyword match (weak secret))
  ? spring.datasource.password=admin123 (entropy: 1.00)
  ? File: demo-app\application.yml
[CRITICAL] Actuator endpoints are exposed to the web!
[INFO] Scan completed in 0.8 seconds.
```

### How Secret Detection Works
BootGuard uses a **hybrid approach** to detect hardcoded credentials with high accuracy:
1. **Provider Signatures:** Instantly flags known token formats (e.g., GitHub PATs, AWS keys, Stripe).
2. **Context + Metric Entropy:** Searches for suspicious variable names (`secret`, `token`, `key`, etc.) and calculates the Metric Entropy. Unlike absolute Shannon Entropy which fails on shorter strings, BootGuard normalizes randomness depending on charset (Base64 vs Hex) and string length, scoring from 0.0 to 1.0. A score of `>= 0.85` indicates a definite cryptographic secret.

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

> [!TIP]
> **Smart Branch Filtering**: To avoid disrupting local development, the hook automatically detects your active Git branch. It strictly enforces the scan (failing the commit) *only* on `main`, `master`, `production`, and `release/*` branches. If you are on a feature or `dev` branch, BootGuard will scan your project in warning-only mode, showing you security risks without blocking your commit!

## Contributing

Pull requests are welcome! If you find a new Spring Boot misconfiguration pattern, feel free to add a new Scanner logic.

## License
MIT License
