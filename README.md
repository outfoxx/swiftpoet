
SwiftPoet
==========

[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/outfoxx/swiftpoet/ci.yml?branch=main)][action]
[![Maven Central](https://img.shields.io/maven-central/v/io.outfoxx/swiftpoet.svg)][dl]
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/io.outfoxx/swiftpoet.svg)][snap]
[![codebeat badge](https://codebeat.co/badges/b6f3870d-84b8-4149-9fbd-b328bfb0302b)](https://codebeat.co/projects/github-com-outfoxx-swiftpoet-develop)

`SwiftPoet` is a Kotlin and Java API for generating `.swift` source files.

Source file generation can be useful when doing things such as annotation processing or interacting
with metadata files (e.g., database schemas, protocol formats). By generating code, you eliminate
the need to write boilerplate while also keeping a single source of truth for the metadata.


### Example

Here's a `HelloWorld` file:

```swift
import RxSwift


class Greeter {

  private let name: String

  init(name: String) {
    self.name = name
  }

  func greet() -> Observable<String> {
    return Observable.from("Hello \(name)")
  }

}
```

And this is the code to generate it with SwiftPoet:

```kotlin
val observableTypeName = DeclaredTypeName.typeName("RxSwift.Observable")

val testClass = TypeSpec.classBuilder("Greeter")
   .addProperty("name", STRING, Modifier.PRIVATE)
   .addFunction(
      FunctionSpec.constructorBuilder()
         .addParameter("name", STRING)
         .addCode("self.name = name\n")
         .build()
   )
   .addFunction(
      FunctionSpec.builder("greet")
         .returns(observableTypeName.parameterizedBy(STRING))
         .addCode("return %T.from(\"Hello \\(name)\")\n", observableTypeName)
         .build()
   )
   .build()

val file = FileSpec.builder("Greeter")
   .addType(testClass)
   .build()

val out = StringWriter()
file.writeTo(out)
```

The [KDoc][kdoc] catalogs the complete SwiftPoet API, which is inspired by [JavaPoet][javapoet].


Download
--------

Download [the latest .jar][dl] or depend via Maven:

```xml
<dependency>
  <groupId>io.outfoxx</groupId>
  <artifactId>swiftpoet</artifactId>
  <version>1.6.5</version>
</dependency>
```

or Gradle:

```groovy
implementation 'io.outfoxx:swiftpoet:1.6.5'
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].


License
-------

    Copyright 2017 Outfox, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


 [action]: https://github.com/outfoxx/swiftpoet/actions?query=branch%3Adevelop
 [dl]: https://search.maven.org/remote_content?g=io.outfoxx&a=swiftpoet&v=LATEST
 [snap]: https://oss.sonatype.org/content/repositories/snapshots/io/outfoxx/swiftpoet/
 [kdoc]: https://outfoxx.github.io/swiftpoet/1.6.5/swiftpoet/io.outfoxx.swiftpoet/
 [javapoet]: https://github.com/square/javapoet/
