# Change log

## [Unreleased]

## [1.6.1] - 2023-12-18

### Fixed

- Fix outstanding cases with importing (#97)

## [1.6.0] - 2023-12-15

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

[Unreleased]: https://github.com/outfoxx/swiftpoet/compare/1.6.1...HEAD
[1.6.1]: https://github.com/outfoxx/swiftpoet/releases/tag/1.6.1
[1.6.0]: https://github.com/outfoxx/swiftpoet/releases/tag/1.6.0
[1.5.0]: https://github.com/outfoxx/swiftpoet/releases/tag/1.5.0
