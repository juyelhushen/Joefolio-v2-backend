# Release and deployment guide

## CI and quality gates

The repository uses three GitHub workflows:

- `.github/workflows/ci.yml` for PRs and branch pushes to `main`
- `.github/workflows/quality.yml` as the reusable Maven quality gate
- `.github/workflows/deploy-render.yml` for tag-triggered production deploys

The reusable quality workflow is invoked by both CI and deployment jobs. It runs the same Maven verification lifecycle for every branch and release candidate and blocks deployment if any required check fails.

## Required local quality commands

```bash
./mvnw -B -ntp clean verify
./mvnw spotless:apply
./mvnw -B -ntp test
./mvnw -B -ntp -DskipTests compile
```

PowerShell equivalent:

```powershell
.\mvnw.cmd -B -ntp clean verify
.\mvnw.cmd spotless:apply
.\mvnw.cmd -B -ntp test
```

## Coverage policy

JaCoCo enforces a minimum line coverage threshold of 70% and branch coverage threshold of 60% for the application bundle. The generated report is available under `target/site/jacoco/` after verification.

## Tag release policy

Release tags must use the format `vMAJOR.MINOR.PATCH`, for example `v0.1.0`.

The workflow rejects:

- malformed tags such as `v1.2`
- a tag whose version does not match the Maven `project.version`
- a tag whose commit is not reachable from `main`

The deployment workflow checks out the exact commit referenced by the tag and sends that SHA to the Render API as `commitId`.

## GitHub secrets required

Create these repository secrets in the GitHub project settings:

- `RENDER_API_KEY`
- `RENDER_SERVICE_ID`

The workflow fails safely if either is missing.

## Render configuration required in the dashboard

Before relying on GitHub tag-based deployment, set the Render service to disable automatic deploys:

- Render Dashboard -> Service -> Settings -> Auto-Deploy -> Off

Also configure these environment variables in the Render service settings:

- `CONTACT_EMAIL_ENABLED`
- `RESEND_API_KEY`
- `RESEND_FROM_EMAIL`
- `CONTACT_TO_EMAIL`

If the service uses a health check, configure `/actuator/health`.

## Deployment flow

1. Ensure the service is configured correctly in Render and Auto-Deploy is Off.
2. Confirm the project version in `pom.xml` is correct.
3. Create a release tag locally, for example:
   ```bash
   git tag v0.1.0
   ```
4. Push the tag to the remote:
   ```bash
   git push origin v0.1.0
   ```
5. GitHub Actions validates the tag format, project version, and main-branch ancestry.
6. The reusable quality workflow runs Maven verification.
7. The deployment job calls the Render API with the exact commit SHA.
8. The job polls Render until the deployment reaches `live` or fails.

## Recovery and review

- Check the GitHub Actions run logs for tag validation, quality failures, or deployment status.
- Check the Render deployment logs for build/runtime issues.
- If a quality gate fails, fix the underlying issue locally and rerun the Maven verification commands.
- If deployment fails, inspect the Render deployment payload and logs before retrying a new tag.

## Manual GitHub configuration

Some features are optional or require repository settings outside the code:

- Required PR checks in branch protection must be selected manually.
- Dependency review and CodeQL are configured in YAML, but branch protection still needs to mark them as required checks if the repository policy requires it.
- GitHub security features that are unavailable on a plan or org policy must be enabled manually by the repository owner.

This repository does not create any production deployment or GitHub Release during local validation.
