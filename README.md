# Advanced Record Utils

[![codecov](https://codecov.io/github/cbarlin/advanced-record-utils/graph/badge.svg?token=KAGP71F0JH)](https://codecov.io/github/cbarlin/advanced-record-utils)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/io/github/cbarlin/aru/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/io/github/cbarlin/aru/README.md)
[![Build](https://github.com/cbarlin/advanced-record-utils/actions/workflows/build.yml/badge.svg)](https://github.com/cbarlin/advanced-record-utils/actions/workflows/build.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.cbarlin/advanced-record-utils-processor?style=flat&color=dark-green&link=https%3A%2F%2Fgithub.com%2Fcbarlin%2Fadvanced-record-utils)](https://central.sonatype.com/artifact/io.github.cbarlin/advanced-record-utils-annotations)
[![javadoc](https://javadoc.io/badge2/io.github.cbarlin/advanced-record-utils-annotations/javadoc.svg)](https://javadoc.io/doc/io.github.cbarlin/advanced-record-utils-annotations)
[![GitHub License](https://img.shields.io/github/license/cbarlin/advanced-record-utils)](https://github.com/cbarlin/advanced-record-utils?tab=MIT-1-ov-file#readme)

## What is Advanced Record Utils?

Advanced Record Utils is an annotation-processor based code generator that creates a companion `*Utils` class (e.g., `PersonUtils` for a `Person` record) that contains:
 * A "Builder" for the record
 * A "With"er interface
 * A "Merger" utility and interface
 * An "XML" utility and interface for serialisation to XML
 * A "Diff" interface and result class
 * An "All" interface that bundles all the other interfaces together

It's configurable, and does away with a lot of boilerplate. It can also import records from libraries (in case you can't control their source code), and can work recursively down a tree of records that reference other records/interfaces.

For more details, see the [documentation](https://cbarlin.github.io/advanced-record-utils)!

## Goals, non-goals

Goals include:
 * Making working with deeply nested data structures easy
 * Moving work to compile-time where possible
 * Wrap serialisation/de-serialisation into XML and (eventually) JSON
 * JPMS support
 * Opt-in dependencies based on the settings chosen
 * Readable generated source code
 * Reproducible builds
    * The tests that get run by GitHub validate that not only our published JARs are reproducible, but that the code we generate is also reproducible too!

Secondary goals:
 * Keep the dependency tree small - some settings would either require a lot of code... or just delegate to a library that's intended for that use
 * Make generated code generate debug/trace logs where appropriate - if you are debugging something in prod at 3am, you want to be able to easily see what's happening

Non-goals:
 * Actually implement de/serialisation - delegate to a dedicated library (e.g. Java's own StAX)
 * ORM/JPA/JDO etc
 * Schema generation

# Quick start

Add the following maven or gradle dependency for the annotations:

```xml
<dependency>
    <groupId>io.github.cbarlin</groupId>
    <artifactId>advanced-record-utils-annotations</artifactId>
    <version>${aru.version}</version>
</dependency>
<!-- JSpecify is included as a transitive dependency -->
```

And the following to your annotation processor paths (note: if using integrations with e.g. `avaje-json`, make sure this processor is first):

```xml
<path>
    <groupId>io.github.cbarlin</groupId>
    <artifactId>advanced-record-utils-processor</artifactId>
    <version>${aru.version}</version>
</path>
```

If you use java modules, you will need to add: 

```java
requires io.github.cbarlin.aru.annotations;
// No explicit 'requires org.jspecify' needed; it's re-exported transitively by the annotations module
```

Annotate your record like so:

```java
@AdvancedRecordUtils
public record Person(String name, int age, List<String> favouriteColours) { }
```

And then you have access to a builder!

```java
Person personA = PersonUtils.builder()
  .name("Conrad")
  .age(conradsAge)
  .addFavouriteColours("blue")
  .addFavouriteColours("purple")
  .build();
Person personB = PersonUtils.builder()
  .name("Fred")
  .favouriteColours(List.of("red", "orange"))
  .build();
```

For more details, see the [documentation](https://cbarlin.github.io/advanced-record-utils)!


## Where did it come from

This project was greatly inspired by [Randgalt's Record Builder project](https://github.com/Randgalt/record-builder), which provides a fantastic foundation for generating record builders and withers. I initially explored contributing to it, aiming to add support for fluent builders in related records. However during development, I realized that my other main need of adding mergers required a significantly different architectural approach. 

`Advanced Record Utils` expands on the core idea of `Record Builder` by introducing these new features, although it does so by allowing less configuration over the features that `Record Builder` also has. `Advanced Record Utils` aims to go "wider" with features, rather than "deeper" like `Record Builder` has (although there is no reason that ARU can't encompass more features in the future!).

# Versioning

Versioning will follow SemVer - with a `major.minor.patch` structure, and `major` pre-1 being prone to breaking external code.

Patch:
 * Bug fixes (except those that impact serialisation)
 * Dependency updates

Minor:
 * Additional features (including optional integrations)
 * Refactoring of the processor
 * Different implementations with the same external interface
 * Serialisation bug fixes
 * Added support for new types in serialisation

Major:
 * Different external interfaces
 * Changes to default settings
 * Additional required dependencies
 * Changes to generated serialisations (not just semantic changes)
     * A change to support a dependency that would change serialisations would be minor, not major 

With the same settings, changing the minor or patch version of the processor should be completely transparent to a consumer, even if the code the processor generates completely changes. A major version would only be needed if the end-user would be required to change their code or if it could break items external to that consumer (e.g. serialisation). Alterations that do change "internal" implementation details are marked as "minor" as a way of flagging that, while it *should* externally be the same there is a non-zero risk it isn't.

## Desired features:

Some of these may be quite large:
 * Avaje JSONB support (as in, generate their handlers by using Jakarta XML annotations)
    * This includes hooking into e.g. Eclipse Collection support
 * Jackson POJO builder support
 * Memoized operations (using `vavr` maybe?)
 * XML Deserialisation
 * `Map` support - the plumbing is there, just need it to be done!

# Contributing

Contributions are welcome! Both in the form of raising an issue for ideas, and in the form of code! If you are going to contribute code, please read the [Contributing guide](CONTRIBUTING.md) as that covers how the code for the processor is designed.

# Thanks to

Massive thanks to:
 * Avaje's Prism generation and SPI utils
 * Micronaut's JavaPoet fork
 * The Apache libraries
