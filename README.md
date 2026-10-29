# Joefolio Backend

This project is a Spring Boot 4.1.2 service for the Joefolio portfolio backend. It exposes a public contact API and integrates with Resend for outbound email delivery.

## Local development

- Java 25
- Maven wrapper

Run the project locally with:

```bash
./mvnw -B -ntp clean verify
```

On Windows PowerShell:

```powershell
.\mvnw.cmd -B -ntp clean verify
```

## Quality gates

The following tools are enforced in the Maven verify lifecycle:

- Maven Enforcer for Java 25 and stable dependency constraints
- Spotless with Google Java Format
- Checkstyle
- PMD
- SpotBugs
- JaCoCo coverage enforcement
- OWASP Dependency-Check

Formatting can be applied locally with:

```bash
./mvnw spotless:apply
```

Unit tests can be run with:

```bash
./mvnw -B -ntp test
```

Integration and full verification use:

```bash
./mvnw -B -ntp clean verify
```

The JaCoCo report is generated under: `target/site/jacoco/`

## Secrets and runtime configuration

The application expects the following environment variables at runtime:

- CONTACT_EMAIL_ENABLED
- RESEND_API_KEY
- RESEND_FROM_EMAIL
- CONTACT_TO_EMAIL

No Resend credentials are required for ordinary local Maven verification or unit tests. The build must not print, log, or commit production secrets.

## Release flow

Release deployment is tag-based and intentionally separated from branch CI.

- CI workflow runs on pull requests to `main` and pushes to `main`.
- The Deploy workflow runs only for version tags like `v0.1.0`.
- The tag must match the Maven project version exactly after the leading `v`.
- The tag commit must be reachable from `main` before deployment is allowed.
- The deployment job calls the Render API with the tagged commit SHA via the `commitId` field.

See `docs/releasing.md` for the full release and Render instructions.
