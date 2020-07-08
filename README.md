# Kapuchin

## Basic Commands
- Compile: `./gradlew :twenty:assemble`
- Deploy: `./gradlew :twenty:deploy`
- Test `twenty`: `./gradlew :twenty:check`
- Test `architecture`: `./gradlew :simulated:jvmsimTest`
- Generate documentation: `./gradlew :architecture:dokka`

## File Structure
- `architecture/src`    Multiplatform module for general year-to-year code
    - `commonMain`      Platform independent code (e.g. math, logging)
    - `commonTest`      Unit tests for `commonMain`
    - `jvmfrcMain`      WPILib implementation of `commonMain` + extra WPILib specific code
    - `jvmsimMain`      Desktop implementation of `commonMain`
    - `jvmsimTest`      Unit tests for `jvmsimMain`
- `eighteen/src/main`   2018 code module
- `nineteen/src/main`   2019 code module
- `twenty/src/main`     2020 (2021?) code module
- `uoms`                Units of measure module
    - `libs`            Precompiled jars for uoms

## Updating uoms
- Update `uoms/units-of-measure.gradle`
- Run `./gradlew :uoms:build`
- Copy `uoms/build/libs/*.jar` to `uoms/libs`

