# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).


## [1.1.2] - 2018-07-04
### Changed
- Moved RX classes under version named packages to prevent collision (#42) `Thanks Cody Henthorne`
- Commands now use generics also and become strongly typed for a better Kotlin support (#39) `Thanks Cain Wong`'

## [Unreleased]
## [1.1.1] - 2017-11-22
### Changed
- Add maven badge to README (#21)
- Fix snapshot upload mechanism for Travis. (#23) `Thanks to Mihaly Nagy`
- Add RxJava2 support (#26)

## [1.1.0] - 2017-09-07
### Changed
- Use KeepAChangelog v1.0.0 for CHANGELOG.md. (#18)
- Make Store emit actions sequentially. (#13) `Thanks to Yichen Wu for the bugfixes she contributed to.`

## [1.0.1] - 2017-06-29
### Changed
- Commands should return an Observable<? extends Action>. (#9)

## [1.0.0] - 2017-06-20
- Initial release

[Unreleased]: https://github.com/groupon/grox/compare/1.1.0...HEAD
[1.1.0]: https://github.com/groupon/grox/compare/1.0.1...1.1.0
[1.0.1]: https://github.com/groupon/grox/compare/1.0.0...1.0.1
[1.0.0]: https://github.com/groupon/grox/compare/4aa91c522a32ef6aed0a3d994df88f31b0613893...1.0.0
