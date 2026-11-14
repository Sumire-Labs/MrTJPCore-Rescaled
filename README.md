# MrTJPCore Rescaled

Shared library for ProjectRed, targeting Minecraft 1.12.2 on Cleanroom, Java 25 and Scala 3.
The mod ID remains `mrtjpcore` and packages remain `mrtjp.core`.

## Requirements

- Java 25
- Cleanroom 0.7.14-alpha or newer
- [CodeChickenLib CRE](https://github.com/Refitbench/CodeChickenLib) 3.3.10 or newer
- [Scalar](https://www.curseforge.com/minecraft/mc-mods/scalar) 3.9.0 (Scala 3 language adapter)

Install the distribution jar and these runtime dependencies in your Cleanroom instance.
Scala 2 builds of ProjectRed and other consumers must be ported/recompiled for Scala 3;
keeping package names does not provide Scala 2 binary compatibility.

## Development

The build is based on [CleanroomDevEnv](https://github.com/Sumire-Labs/CleanroomDevEnv)
at `6b24e5c`, using CleanroomGradle 0.23.0 and Gradle 9.9.0.
Cleanroom userdev supplies the mappings, replacing ForgeGradle 2.3 and snapshot_20171003.
Dependencies and the Scala version are pinned in `gradle.properties`.
Use JDK 25 as JAVA_HOME when launching Gradle.

```sh
./gradlew build
./gradlew runClient
./gradlew runServer
```

On Windows, use `.\gradlew.bat`. Sources use the standard `src/main/scala`,
`src/main/java` and `src/main/resources` layout.
Versions come from Git tags; do not introduce a separate mod_version property.
Check the current version with `./gradlew -q printVersion`.
The version source is generated before compilation so annotations and TASTy agree.

## Verification

```sh
./gradlew clean build
./gradlew runServer -Psmoke_test=true --no-configuration-cache
./gradlew runClient -Pclient_smoke=true --no-configuration-cache
bash .github/scripts/production-smoke.sh
```

The build checks Enum collection behavior, Java 25 class versions, metadata,
license notices, unresolved tokens and the absence of test classes in the runtime jar.
The server checks inventory bounds and NBT round trips, key state cleanup and C2S
packet dispatch. The client checks main-menu startup and textbox keyboard behavior.
The production script installs an isolated Cleanroom server under `run/production-smoke`
and repeats the server checks with reobfuscated distribution jars and runtime dependencies.
It requires Bash, curl, timeout, Java 25 and network access.
CI runs the build, development server and production server checks; the graphical client check is manual.

## Migration and fixes

Scala 3 sources are adapted from [kappa-maintainer/MrTJPCore](https://github.com/kappa-maintainer/MrTJPCore/tree/1.12.x)
at `9bb386d`. This preserves its no-argument tile-constructor reflection fix.
CleanroomDevEnv replaces its Unimined/Blossom build.
The port also includes these fixes:

- Fix reversed Enum.ValSet incl/excl operations and implement iteratorFrom.
- Preserve registered inventory wrappers, enforce insertion stack limits and reject invalid container slots.
- Add inventory slot bounds checks, based on [ACGaming's fix](https://github.com/ACGaming/MrTJPCore/commit/b5ff77f05575f1ad596c703825cc4615a018724e), including removal/extraction and nonpositive extraction counts.
- Replace player references in key state with UUIDs, following [GTNH's fix](https://github.com/GTNewHorizons/MrTJPCore/commit/7fc156b5201d6ae4bb59f14bf388fc4c57acb587); release states on logout/server shutdown, ignore unknown tracker IDs and synchronize access between client and server threads.
- Restrict client tile packets to loaded blocks within eight blocks and reject description packets, adapted from [GTNH's fix](https://github.com/GTNewHorizons/MrTJPCore/commit/6cbdbe819c08fbdc537a08f5640e89af3f89e73b).
- Preserve the textbox constructor's initial text and support Enter/Backspace using the LWJGL 2 key codes supplied by the 1.12.2 GUI event API.

Client actions now explicitly override `MTBlockTile.readFromClient(in, key, player)`.
The default ignores actions; `read(in, key)` remains for server-to-client updates.
Consumers that previously sent tile actions to `read` must move those actions to the new
hook and validate permissions and payloads there. Description key 0 is never a client action.

This port does not include GTNH's 1.7.10 rendering patches or ProjectRed 1.21.1 features.
Those changes need matching 1.12.2 implementations and consumer integration tests.

## JEI-compatible overlays

When JEI or HEI is installed, NodeGui reports visible child areas outside its main
window so the ingredient overlay and cheat-mode cursor handling stay out of those
areas. Hidden nodes and content outside ClipNode masks are excluded. This addresses
the extra-area handling reported in [ProjectRed #1739](https://github.com/MrTJP/ProjectRed/issues/1739).

The integration is optional and is tested with HEI 4.35. A screen that draws controls
outside the node tree can override `NodeGui.getExtraGuiAreas`.
Run `./gradlew runClient -Pclient_smoke=true -Pjei_smoke=true --no-configuration-cache`
to check the integration with the pinned JEI-compatible implementation.

## Publishing

GitHub Actions builds every push/PR. Pushing a version tag such as `2.2.0` or
`2.2.0-alpha.1` runs the release workflow after verification. An existing tag can
also be selected through workflow_dispatch. Release notes come from a matching
CHANGELOG.md version section if present, otherwise GitHub-generated notes.
The alpha, beta and release-candidate tags create GitHub prereleases.
Before tagging a release, move the changes from `[Unreleased]` in CHANGELOG.md to a
`## [version] - YYYY-MM-DD` section and update its comparison links. The workflow
requires one nonempty section that matches the tag.
Local `build` tasks do not create release tags or publish artifacts.

Set these GitHub Actions secrets:

| Destination | Secrets | Configuration |
| --- | --- | --- |
| Maven | MAVEN_USERNAME, MAVEN_PASSWORD | maven_url and maven_name in gradle.properties |
| CurseForge | CURSEFORGE_TOKEN | curseforge_project_id in gradle.properties |
| Modrinth | MODRINTH_TOKEN | modrinth_project_id in gradle.properties |

CF/MR project IDs are deliberately blank until the projects are configured; those
destinations are skipped while blank. Maven targets
`https://artifact.sumirelabs.com/maven/releases`. Enabled destinations require their secrets.
Scalar has no confirmed Modrinth project: Modrinth users must install it from the link above.

Maven coordinates: `mrtjp.core:MrTJPCore-Rescaled:<git-version>`.
The main artifact is the SRG runtime jar, `dev` is MCP-named and `sources` contains sources.
The POM intentionally omits build-only userdev and remapping attributes.
Declare CCL and Scalar explicitly in consumer builds, using CleanroomGradle's deobfuscation for runtime jars.

```sh
./gradlew publishToMavenLocal -Ppublish_to_local_maven=true
./gradlew publish
./gradlew publishMods
```

The Publish Maven workflow can retry Maven publication for an existing release tag
without republishing to CF/MR. Coordinate any retries with the destination's duplicate-version rules.

## License and credits

MrTJPCore is distributed under LGPL v3; see COPYING and COPYING.LESSER.
Original work by MrTJP; Scala migration by kappa_maintainer; additional fixes from
ACGaming and GTNewHorizons; Cleanroom build and integration by Sumire Labs.
The CleanroomDevEnv template's MIT notice is preserved in gradle/TEMPLATE-LICENSE.md.
