# Changelog

All notable changes to this project are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and release versions follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [3.0.0-beta] - 2026-11-17

### Added

- Add optional JEI-compatible GUI integration for visible nodes outside the main window, including clipped and hidden-node handling.

### Changed

- Migrate to Cleanroom, Java 25 and Scala 3.10.0, using kappa-maintainer's Scala 3 port.
- Require Cleanroom 0.7.14-alpha, CodeChickenLib CRE 3.3.10 and Scalar 3.10.0 or newer.
- Provide runtime, development and source JARs.
- Keep the `mrtjpcore` mod ID and `mrtjp.core` packages. Scala 2 consumers must be ported and recompiled for Scala 3.

### Fixed

- Prevent block update hooks from forcing unloaded chunks to load.
- Fix retrogen progress tracking across saves and retries, duplicate requests, configuration changes and world unloads.
- Fix world generation biome filters, tree canopy scanning and decorator placement conditions.
- Handle missing or unregistered tile entities in block callbacks.
- Respect slot insertion rules and stack limits, validate slot bounds and reject nonpositive inventory operations.
- Fix hidden-item counts and preserve registered inventory wrappers.
- Copy NBT when creating or copying item keys so changes to the original data cannot alter existing keys. Include item IDs in hashes for undamaged items.
- Track key states by UUID and clear them on logout, disconnection and server shutdown. Synchronize access between threads and ignore unknown tracker IDs.
- Prevent handled GUI mouse input from reaching the backing container.
- Fix GUI centering, hidden-node handling, tab positions and hit regions.
- Correctly intersect nested clipping regions and restore the previous clipping state after drawing or errors.
- Make item lists follow changes to their position and size, and prevent division by zero when collapsed.
- Preserve textbox initial text and fix Enter and Backspace handling.
- Fix particle interpolation, final partial ticks, previous positions, shared mutable vectors and action removal. Reject incompatible composite actions.
- Fix enum set operations, crossing rectangle intersections, rectangular cuboid grid coordinates and least-significant bit calculations.
- Support weighted random totals above the `Int` limit and reject negative or all-zero weights.
- Fix delayed task ordering and overflow with extremely long delays.
- Preserve the types of empty configuration arrays when reading and saving them.
- Preserve the no-argument tile-constructor reflection fix from kappa-maintainer.

### Security

- Reject client tile-description packets and restrict tile actions to loaded blocks within eight blocks of the sender.
- Dispatch client tile actions through the opt-in `MTBlockTile.readFromClient` hook. Consumers must move client action handlers from `read` to this hook and validate permissions and payloads there.

[Unreleased]: https://github.com/Sumire-Labs/MrTJPCore-Rescaled/compare/3.0.0-beta...cleanroom
[3.0.0-beta]: https://github.com/Sumire-Labs/MrTJPCore-Rescaled/compare/6f8a17c5d20de0db58f408edaa140a301035cf9e...3.0.0-beta
