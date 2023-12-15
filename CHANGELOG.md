# Changelog

## [Unreleased]

## [1.6.0] - 2023-12-14

### Added

- Output of AttributeSpec to now accept CodeBlocks (#91).
- `CONVENIENCE` modifier has been added (#92).

### Fixed

- No more errors when formatting strings (#85).
- Imports are now correctly emitted for subtypes within external extensions (#88).
- Fix generation functions with `set` and `get` names (#82).

### Changed

- `CodeWriter` is now a `Closeable`, be sure to close it after usage, which can be done with the `.use {}` helper method from Kotlin. (#89)

## [1.5.0]

### Remark

There are no changelog for versions 1.5.0 and before. 