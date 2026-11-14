# Changelog

All notable changes to this project are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and release versions follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- GitHub Actions checks for builds, regression tests and dedicated-server startup.
- Tag-based publishing to Maven, CurseForge and Modrinth, with a separate Maven retry workflow.
- Regression checks for Enum collections, inventory operations, key state cleanup, tile packets and textbox input.

### Changed

- Sync build support with CleanroomDevEnv at `6b24e5c`.

- Build with CleanroomDevEnv, CleanroomGradle 0.23.0, Gradle 9.9.0 and Java 25.
- Port the library to Scala 3.9.0 using kappa-maintainer's 1.12.x sources.
- Use Cleanroom userdev mappings and standard source directories.
- Require Cleanroom 0.7.14-alpha, CodeChickenLib CRE 3.3.10 and Scalar 3.9.0 or newer.
- Generate version constants before compilation and derive release versions from Git tags.
- Publish an SRG runtime jar, an MCP-named `dev` jar and a sources jar under `mrtjp.core:MrTJPCore-Rescaled`.
- Mark alpha, beta and release-candidate tags as GitHub prereleases.
- Keep the `mrtjpcore` mod ID and `mrtjp.core` packages. Scala 2 consumers must be ported and recompiled for Scala 3.

### Fixed

- Record only completed structure generation when saving chunks and retain pending work across saves.
- Deduplicate retrogen requests, respect configuration changes and discard unloaded-world queues without loading distant chunks.

- Handle missing or unregistered multi-tile entities in block state and event callbacks.

- Place cuboids in the correct rows when building rectangular grids.

- Handle empty or fully hidden GUI children and keep tab sizes, hit regions and icon positions aligned.

- Order delayed work by its completion time and prevent extreme delays from overflowing.

- Check project bytecode versions without rejecting libraries relocated into the project namespace.

- Resolve embedded dependencies as runtime jars and apply token replacement to all compiled classes.
- Check class-file string constants for unresolved tokens without interpreting binary constants as text.

- Support weighted random totals above the Int limit and reject negative or all-zero weights.

- Stop consumed GUI mouse events from reaching the backing container.
- Detect crossing rectangle intersections even when no corners are contained.

- Respect slot insertion rules when merging into existing stacks.

- Copy source NBT when creating or copying item keys so later tag changes do not alter existing keys.
- Include the item ID in hashes for undamaged items.

- Keep mutable vectors independent between particle actions and beam targets.
- Interpolate particle changes over the full duration, including the final partial tick.
- Preserve particle positions before movement for render interpolation.
- Remove copied particle actions through their original handles and reject incompatible composite actions.

- Return the correct least-significant bit position for even and negative bit masks.

- Center NodeGui windows using their configured size on first display and after resizing.

- Match biome filters against Scala sets and scan tree canopies from the correct world height.
- Apply decorator placement rules when checkStay is enabled and honor the underTree filter.

- Read and save empty configuration arrays without throwing or changing their value type.

- Honor hidden-item settings in capability inventory counts and inventory availability checks.
- Treat overfull inventory slots as having no free insertion space.

- Correct the reversed `Enum.ValSet.incl` and `excl` operations and implement `iteratorFrom`.
- Preserve custom inventory wrappers registered through `InvWrapper.register`.
- Enforce item stack limits during insertion and ignore nonpositive insert/extract amounts.
- Check inventory and container slot bounds before accessing storage.
- Store tracked key states by UUID and clear them on logout and server shutdown.
- Synchronize key state access between client and server threads in singleplayer.
- Reset client key tracking after disconnection and ignore unknown tracker IDs.
- Clear stored client key state when the tracked player disconnects or changes.
- Preserve textbox constructor text and handle Enter and Backspace on Cleanroom.
- Preserve the no-argument tile-constructor reflection fix from kappa-maintainer.
- Exclude Scala test sources when tests are disabled.

### Security

- Reject client tile-description packets and limit tile actions to loaded blocks within eight blocks of the sender.
- Dispatch client tile actions through the opt-in `MTBlockTile.readFromClient` hook. Consumers must move client action handlers from `read` to this hook and validate permissions and payloads there.

[Unreleased]: https://github.com/Sumire-Labs/MrTJPCore-Rescaled/compare/6f8a17c5d20de0db58f408edaa140a301035cf9e...cleanroom
