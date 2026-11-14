# MrTJPCore Rescaled

Shared library for ProjectRed on Minecraft 1.12.2, Cleanroom, Java 25 and Scala 3.
The mod ID is `mrtjpcore`; packages remain under `mrtjp.core`.

## Requirements

- Java 25
- Cleanroom 0.7.14-alpha or newer
- CodeChickenLib CRE 3.3.10 or newer
- Scalar 3.9.0

Scala 2 consumers must be ported and recompiled for Scala 3.

## Development

Use JDK 25 as JAVA_HOME.

```sh
./gradlew build
./gradlew runClient
./gradlew runServer
```

On Windows, use `.\gradlew.bat`. The build uses CleanroomDevEnv and CleanroomGradle.
Scala sources are adapted from kappa-maintainer/MrTJPCore at `9bb386d`.

## License

See COPYING and COPYING.LESSER for LGPL v3. The template MIT notice is in
gradle/TEMPLATE-LICENSE.md.
