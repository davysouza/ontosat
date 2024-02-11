# TBox Saturator
An application to saturate a TBox based on role assertions of a given ontology.

Given the role assertion `relation(A, B)` and the class assertion `Politician(B)` the logical inference $\exists$ `relation.Politician` naturally follows. However, existing reasoners such as ELK or HermiT only make inferences in terms of known classes, limiting their ability to deduce such axioms in their set of consequences.

This application aims to saturate the ontology and add these logical inferences into the ontology.

For each role assertion `r(A, B)` from the **original ontology**, the aplication retrieves all the classes `C` associated with the **object** individual and add a new axiom of the form $\exists$ `r.C` into the **subject** indvidual. Alternativelly, users have the option to create a new class instead of adding axioms to the **subject** individual. At this case, a new class named `rC` equivalent to $\exists$ `r.C` is added to the ontology.

> [!NOTE]
> Given a role assertion `r(A, B)`, the individual `A` is known as the *subject individual* while `B` is known as the *object individual*.

## Requirements

- **JDK version**: 20.0.2
- **Gradle version**: 8.5

## Build and Run

To build the project, run the following command at the project's directory:

```Batchfile
./gradlew build
```

> [!NOTE]
> The first time you run the build, Gradle will check whether or not you already have the required dependencies in your cache under your ~/.gradle directory. If not, the libraries will be downloaded and stored there. The next time you run the build, the cached versions will be used. The build task compiles the classes, runs the tests, and generates a test report.

To generate a JAR file with all dependencies, run: 

```Batchfile
./gradlew jar
```

The generated JAR file will be available at: `build/libs/tbox-saturator-1.0-SNAPSHOT.jar`

Now, run:

```Batchfile
java -jar build/libs/tbox-saturator-1.0-SNAPSHOT.jar
```
to execute the application.

### Running options

|Parameters                   | Description                                          |
|-----------------------------|------------------------------------------------------|
|`-h`, `--help`               | Prints the help message                              |
|`-o`, `--ontology`           | Path of the ontology to be saturated                 |
|`-O`, `--saturated-ontology` | Saturated ontology path                              |
|`-a`, `--add-class`          | Creates a named class for each new restriction added |

## Test

To run the existing unit tests, execute:

```Batchfile
./gradlew test
```