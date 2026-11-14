# Changelog

All notable changes to this project are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and release versions follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- Migrate to CleanroomDevEnv, Java 25 and Scala 3.9.0.
- Use Cleanroom userdev mappings and standard source directories.
- Keep the `mrtjpcore` mod ID and `mrtjp.core` packages. Scala 2 consumers must be ported and recompiled.

### Fixed

- Correct Enum set operations and sorted iteration.
- Preserve registered inventory wrappers and enforce insertion stack limits.
- Ignore nonpositive insert/extract amounts and invalid inventory or container slots.
- Preserve the textbox constructor text.
