# MrTJPCore Rescaled

A shared library used by [ProjectRed Rescaled](https://github.com/Sumire-Labs/ProjectRed-Rescaled) and other mods.
This fork of [MrTJPCore](https://github.com/MrTJP/MrTJPCore) includes new features, bug fixes, and a migration to [Cleanroom](https://github.com/CleanroomMC/Cleanroom), Java 25, and Scala 3.

## Requirements

- [Cleanroom](https://github.com/CleanroomMC/Cleanroom) 0.7.14-alpha or newer
- [Scalar](https://github.com/CleanroomMC/Scalar) 3.10.0 or newer
- [CodeChicken Lib CRE](https://github.com/Refitbench/CodeChickenLib) 3.3.10 or newer

## Changes

- Migrate to Cleanroom, Java 25, and Scala 3.
- Add [JEI-compatible GUI integration](https://github.com/MrTJP/ProjectRed/issues/1739). Nodes outside the main window count as GUI areas, preventing the item list from overlapping them and causing unintended interactions.
- Add the `MTBlockTile.readFromClient` hook for tile actions sent by clients.
- Respect slot insertion rules and stack limits when inserting items into inventories.
- Fix hidden-item counts, [out-of-bounds slot access](https://github.com/ACGaming/MrTJPCore/commit/b5ff77f05575f1ad596c703825cc4615a018724e), and custom inventory wrapper registration.
- Copy NBT when creating `ItemKey` instances so changes to the original data cannot alter existing keys. Fix hash calculation as well.
- [Track key states by UUID](https://github.com/GTNewHorizons/MrTJPCore/commit/7fc156b5201d6ae4bb59f14bf388fc4c57acb587) and clear them on logout, disconnection, and server shutdown. Fix synchronization between threads and handling of unknown tracker IDs.
- [Validate client tile packets](https://github.com/GTNewHorizons/MrTJPCore/commit/6cbdbe819c08fbdc537a08f5640e89af3f89e73b) to prevent unauthorized state changes through description packets. Restrict actions to loaded blocks near the sender.
- Stop mouse input already handled by the GUI from reaching the backing container.
- Fix GUI centering on first display and after resizing, hidden-node handling, and the positions and hit regions of moved tabs.
- Correctly intersect nested GUI clipping regions and restore the previous clipping state after drawing, including when an exception occurs.
- Make item lists follow changes to their position and size, and prevent division by zero when collapsed.
- Fix textbox initial text and Enter and Backspace handling.
- Fix particle interpolation, final frames, previous positions, vector sharing between actions, and action removal.
- Fix world generation biome filters, tree canopy scanning positions, and decorator placement conditions.
- Fix duplicate retrogen requests, progress tracking across saves and retries, and handling of configuration changes and world unloads.
- Prevent block update hooks from forcing unloaded chunks to load.
- Fix block callbacks when tile entities are missing or their types are unregistered.
- Read and write empty configuration arrays while preserving their types.
- Fix enum set operations, rectangle intersection checks, rectangular grid coordinates, and least-significant bit position calculations.
- Support weighted random totals above the `Int` limit and reject invalid weights.
- Fix delayed task ordering and overflow with extremely long delays.

## For Developers

Releases use these coordinates on [Sumire Labs Maven](https://artifact.sumirelabs.com/maven/releases):

```text
mrtjp.core:MrTJPCore-Rescaled:<version>
```

## Credits

- [MrTJPCore](https://github.com/MrTJP/MrTJPCore): The original mod.
- [MrTJPCore by kappa_maintainer](https://github.com/kappa-maintainer/MrTJPCore): The first fork to migrate to Cleanroom, Java 25, and Scala 3.
- [MrTJPCore by ACGaming](https://github.com/ACGaming/MrTJPCore): Contributed some of the bug fixes included in this fork.
- [MrTJPCore by GTNHTeams](https://github.com/GTNewHorizons/MrTJPCore): Contributed many of the bug fixes and performance patches included in this fork.
