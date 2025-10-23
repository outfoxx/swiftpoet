# Change log

## [Unreleased]

## [1.6.8] - 2025-10-23

### Fixed

- Fix import generation with attributes (#119)
- Fix optional generic qualified declared types generation (#121)
- Fix disambiguating different types with the same simple name (#123)

Note that `1.6.7` has been skipped.

## [1.6.6] - 2024-10-29

### Fixed

- Fix shortening of nested names in generated hierarchies (#118)

## [1.6.5] - 2024-02-21

### Added

- Add lazy modifier (#116)
- Support generic qualifiers for `TypeName` (#115)

## [1.6.4] - 2024-01-15

### Fixed

- Correctly disambiguate conflicting type names between the module itself and external modules (#111)
- Always qualify ambiguous types (#110)

## [1.6.3] - 2023-12-20

### Fixed

- Fix incorrect importing of local types (#105)

## [1.6.2] - 2023-12-19

### Added

- Add codable, equatable and sendable Swift declared type names (#103)

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

[Unreleased]: https://github.com/outfoxx/swiftpoet/compare/1.6.8...HEAD
[1.6.8]: https://github.com/outfoxx/swiftpoet/releases/tag/1.6.8
[1.6.6]: https://github.com/outfoxx/swiftpoet/releases/tag/1.6.6
[1.6.5]: https://github.com/outfoxx/swiftpoet/releases/tag/1.6.5
[1.6.4]: https://github.com/outfoxx/swiftpoet/releases/tag/1.6.4
[1.6.3]: https://github.com/outfoxx/swiftpoet/releases/tag/1.6.3
[1.6.2]: https://github.com/outfoxx/swiftpoet/releases/tag/1.6.2
[1.6.1]: https://github.com/outfoxx/swiftpoet/releases/tag/1.6.1
[1.6.0]: https://github.com/outfoxx/swiftpoet/releases/tag/1.6.0
[1.5.0]: https://github.com/outfoxx/swiftpoet/releases/tag/1.5.0
