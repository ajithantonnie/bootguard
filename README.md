# BootGuard 🛡️

A lightning-fast, zero-config CLI tool to scan Spring Boot projects for security risks and misconfigurations. 

Because waiting 10 minutes for your CI/CD pipeline to tell you your Actuator is exposed is 10 minutes too long.

## Features
- **🚀 Instant Local Scanning:** Runs in sub-seconds, designed for local developer workflows.
- **🌱 Spring Boot Specific:** Detects framework-specific nuances that generic static analysis tools miss.
- **🔍 Comprehensive Security Checks:**
  - **Actuator:** Smart Actuator endpoint exposure checks (ignores local-bound endpoints like `localhost`).
  - **Secrets:** Hardcoded credentials and tokens using **Hybrid Detection** (Regex Patterns + Metric Entropy).
  - **Environment:** Debug mode accidentally enabled in production-active profiles.
  - **Database:** Plain-text DB passwords and connections lacking SSL enforcement.
  - **Networking:** Overly permissive CORS configurations (e.g., wildcard allowed origins).
  - **H2 Console:** Unsecured or inadvertently exposed H2 Database web consoles with weak passwords.
  - **VCS Safety:** Detects sensitive files (e.g., `.env`, `application-prod.yml`) that might have been accidentally committed.
- **⚖️ Context-Aware Severity:** Automatically downgrades the severity of issues found within `dev`, `test`, or `local` profile blocks to prevent blocking local development.
- **⚙️ Fully Customizable:** Override security thresholds, regex patterns, and target directories using a simple `bootguard.properties` file.

## Installation

Download the latest executable JAR from the [Releases](#) page.

Requires **Java 17+**.

## Usage

Simply run the tool against your Spring Boot project directory or a specific configuration file (`.properties`, `.yml`, `.yaml`). By default, it will automatically search the target directory (or the file's parent) for a `bootguard.properties` to load custom overrides.

**Scan a whole project:**
```bash
java -jar bootguard.jar /path/to/your/spring-boot-project
```

**Scan a single file:**
```bash
java -jar bootguard.jar /path/to/your/spring-boot-project/src/main/resources/application-prod.yml
```

### Options

| Flag | Description | Default |
|------|-------------|---------|
| `-f, --fail-on` | Fail with non-zero exit code if issues of this severity or higher are found. Valid values: `HIGH`, `MEDIUM`, `LOW` | `HIGH` |
| `-c, --config` | Provide a custom external configuration properties file to override detection thresholds. | *(Project root `bootguard.properties`)* |

### Example

```bash
java -jar bootguard.jar -f MEDIUM ./my-project
```

### Example Output

```text
Scanning project at: my-project

[HIGH] Overly permissive CORS configuration (Wildcard mapping)
  ? spring.web.cors.allowed-origins=*
  ? File: my-project/src/main/resources/application.yml

[MEDIUM] Hardcoded risk finding (Keyword match (weak secret))
  ? spring.datasource.password=admin123 (entropy: 0.92)
  ? File: my-project/src/main/resources/application-prod.yml

[LOW] Debug enabled
  ? debug=true
  ? File: my-project/src/main/resources/application-dev.yml [Profile: dev]

Scan complete: 3 issues found (1 High, 1 Medium, 1 Low)
[FAILED] Issues found exceeding severity threshold: MEDIUM
```

---

## Configuration

BootGuard works out of the box with safe defaults, but can be customized via a `bootguard.properties` file.

### Available Properties

| Property | Description | Default Value |
|----------|-------------|---------------|
| `ignore.directories` | Directories to skip during scanning. | `.git,target,node_modules,build,dist,.idea` |
| `secret.min.length` | Minimum length for a string to be considered a potential secret. | `8` |
| `metric.entropy.threshold` | Threshold for "Medium" risk secrets. | `0.75` |
| `metric.entropy.high.threshold` | Threshold for "High" risk secrets (definite cryptographic keys). | `0.85` |
| `secret.keyword.pattern` | Regex to find sensitive variable names (e.g., `password`, `key`). | *(See default properties)* |
| `secret.provider.pattern` | Regex for known provider signatures (e.g., AWS, GitHub, Stripe). | *(See default properties)* |
| `h2.weak.passwords` | List of passwords considered weak for the H2 console. | `password,root,admin` |
| `sensitive.files.pattern` | Filename patterns to flag for VCS exposure. | `-prod.yml,.env,...` |

---

## How Secret Detection Works

BootGuard uses a **hybrid approach** to detect hardcoded credentials with high accuracy:
1. **Provider Signatures:** Instantly flags known token formats (e.g., GitHub PATs, AWS keys, Stripe).
2. **Context + Metric Entropy:** Searches for suspicious variable names (`secret`, `token`, etc.) and calculates the **Metric Entropy**. Unlike absolute Shannon Entropy which fails on shorter strings, BootGuard normalizes randomness depending on charset (Base64 vs Hex) and string length, scoring from 0.0 to 1.0. 
   - **Score >= 0.85:** High confidence secret.
   - **Score >= 0.75:** Suspiciously high entropy string.

## Git Pre-commit Integration

BootGuard can be integrated into your Git workflow to block commits that contain security risks.

### Option 1: Standalone Git Hook

Copy the provided hook script to your `.git/hooks/` directory:

```bash
cp scripts/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

> [!TIP]
> **Smart Branch Filtering**: The hook automatically detects your active Git branch. It strictly enforces the scan (failing the commit) *only* on `main`, `master`, `production`, and `release/*` branches. On feature branches, it runs in **warning-only mode**.

### Option 2: Pre-commit framework

```yaml
repos:
  - repo: https://github.com/ajithantonnie/bootguard
    rev: v1.0.0
    hooks:
      - id: bootguard-scan
```

## Testing with the Demo App

A deliberate vulnerable application is included in the `demo-app/` directory to demonstrate BootGuard capabilities.

> [!NOTE]
> To prevent secrets from leaking into Git history, the `demo-app/application.yml` file uses placeholders for API keys.
> To fully test the **Secrets Scanner**, you can manually temporarily replace `PLACEHOLDER_STRIPE_KEY_12345` with a dummy Stripe key (e.g., `sk_test_51Mz...`) and BootGuard will flag it!

## Contributing

Pull requests are welcome! If you find a new Spring Boot misconfiguration pattern, feel free to add a new Scanner logic.

## License
MIT License
