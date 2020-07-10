# Kapuchin

## Basic Commands
- Compile &mdash; `./gradlew :twenty:jar`
- Deploy &mdash; `./gradlew :twenty:deploy`
- Test `twenty` &mdash; `./gradlew :twenty:check`
- Test `architecture` &mdash; `./gradlew :simulated:check`
- Generate documentation &mdash; `./gradlew :architecture:dokka`

## Project Structure
```
├── architecture/src    Multiplatform module for general year-to-year code
│   ├── commonMain      Platform independent code (e.g. math, logging)
│   ├── commonTest      Platform independent unit tests
│   ├── jvmfrcMain      WPILib implementation of `commonMain` + extra WPILib specific code
│   ├── jvmsimMain      Desktop implementation of `commonMain`
│   └── jvmsimTest      Desktop unit tests for `jvmsimMain`
├── eighteen            2018 code module
├── nineteen            2019 code module
├── twenty              2020 + 2021? code module
└── uoms                Units of measure
    └── libs            Precompiled JARs
```

## Updating units of measure
- Update `uoms/units-of-measure.gradle`
- Run `./gradlew :uoms:build` (will take a long time)
- Copy `uoms/build/libs/*.jar` to `uoms/libs`
