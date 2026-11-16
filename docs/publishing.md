# Publishing

Publishing uses [CleanroomDevEnv](https://github.com/Sumire-Labs/CleanroomDevEnv)'s
release workflow, Gradle Maven Publish, and the
[Mod Publish Plugin](https://github.com/modmuss50/mod-publish-plugin).

## GitHub settings

Add these values under **Settings > Secrets and variables > Actions**:

| Name | Type | Purpose |
| --- | --- | --- |
| `MODRINTH_TOKEN` | Secret | Upload releases to Modrinth. |
| `CURSEFORGE_TOKEN` | Secret | Upload releases to CurseForge. |
| `MAVEN_USERNAME` | Secret | Authenticate to Sumire Labs Maven. |
| `MAVEN_PASSWORD` | Secret | Authenticate to Sumire Labs Maven. |
| `MODRINTH_PROJECT_ID` | Optional variable or secret | Override the Modrinth project ID. |
| `CURSEFORGE_PROJECT_ID` | Optional variable or secret | Override the numeric CurseForge project ID. |

The project IDs are already set in `gradle.properties`: `qteD8tkC` for Modrinth
and `1804843` for CurseForge. Only the four authentication secrets are required.

If an ID is set as both a variable and a secret, the variable takes precedence.
Nonblank IDs from GitHub settings override `modrinth_project_id` and
`curseforge_project_id` in `gradle.properties`. A destination with no ID is skipped.

Maven publishing is enabled in `gradle.properties` and targets
`https://artifact.sumirelabs.com/maven/releases`. The workflow checks credentials
for every enabled destination before building. GitHub supplies `GITHUB_TOKEN`
for GitHub Releases automatically.

## Release a version

1. Add a dated section for the chosen version to `CHANGELOG.md`, following
   [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
2. Commit the release notes on `cleanroom` and push the branch.
3. Create and push the matching version tag. Both `0.1.0` and `v0.1.0` formats
   are accepted; these are examples, not a selected release version.

The Release workflow builds the mod, runs tests and dedicated-server checks,
publishes to Maven and the configured mod platforms, and creates a GitHub Release.
The matching changelog section supplies the release notes for GitHub, Modrinth,
and CurseForge. A missing or empty version section stops publication.

Alpha tags publish alpha files. Beta and release-candidate tags publish beta files.
All three create GitHub prereleases. Ordinary branch pushes only run build checks.

To release an existing tag, run **Release** from GitHub Actions and enter the tag.
The separate **Publish Maven** workflow can retry Maven publication for an existing
tag without uploading to Modrinth or CurseForge. Check which uploads succeeded
before retrying a partially completed release; destinations may reject duplicates.

## Maven artifacts

Coordinates: `mrtjp.core:MrTJPCore-Rescaled:<version>`.

- The main JAR uses SRG names and is intended for installation.
- The `dev` JAR uses MCP names for development.
- The `sources` JAR contains the source code.

The mod requires Cleanroom, CodeChickenLib CRE, and Scalar. CurseForge uploads
declare CodeChickenLib CRE and Scalar as dependencies. Modrinth uploads declare
CodeChickenLib CRE; install Scalar separately from
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/scalar/files/9145663).
