![ontosat logo](OntoSat.png "OntoSat")

# OntoSat

![example workflow](https://github.com/davysouza/ontosat/actions/workflows/gradle.yml/badge.svg)

## Welcome to OntoSat   

**OntoSat** is an application designed to saturate an ontology based on its assertional axioms.

For a given ontology with the axioms `r(a, b)` and `P(b)`, the axiom `∃r.P(a)` naturally follows. However, existing reasoners such as **ELK** or **HermiT** only make inferences in terms of known classes, limiting their ability to deduce such axioms in their set of consequences.

This application aims to saturate the ontology adding these kinds of logical inferences.

For each axiom of the form `r(A, B)` from the **original ontology**, the application retrieves all the classes `C` associated with the **object** individual and adds a new axiom of the form `∃r.C` into the **subject** individual. If the **object** individual has no classes associated, the saturation process will add the axiom `∃r.⊤`.

Users can choose whether the saturation process adds assertional or terminological axioms. Assertional axiom saturation is selected by default. If terminological saturation is selected, a named class `rC ≡ ∃r.C` is added to the ontology.

> [!NOTE]
> Given a role assertion `r(a, b)`, the `a` is known as the *subject individual* while `b` is the *object individual*.

## Getting started

### Requirements

There exist some environment requirements if you want to run the **OntoSat**. Make sure you have your environment set with softwares listed below.
- **Java Development Kit (JDK)** at version: 20.0.2
- **Gradle** at version: 8.5

### Build and Run

To build the project, run the following command at the project's directory:

```Batchfile
./gradlew build
```

> [!NOTE]
> The first time you run the build, Gradle will check whether you already have the required dependencies in your cache under your `~/.gradle` directory or not. If not, the libraries will be downloaded and stored there. The next time you run the build, the cached versions will be used. The build task compiles the classes, runs the tests, and generates a test report.

To generate a `.jar` file with all dependencies, run: 

```Batchfile
./gradlew jar
```

The generated JAR file will be available at: `build/libs/ontosat-1.0-SNAPSHOT.jar`

Now, run:

```Batchfile
java -jar build/libs/ontosat-1.0-SNAPSHOT.jar
```
to execute the application.

### Running options

| Parameters                   | Description                                                                                                                            |
|------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `-h`, `--help`               | Prints the help message.                                                                                                               |
| `-i`, `--ontology`           | Specifies the path to the input ontology.                                                                                              |
| `-o`, `--saturated-ontology` | Specifies the path where the saturated ontology will be stored                                                                         |
| `-m`, `--mode`               | Defines the saturation mode, which can be either `assertional` or `terminological`. The `assertional` mode is selected by **default**. |

### Test

To run the existing unit tests, execute:

```Batchfile
./gradlew test
```

## Documentation
 - [Master's Thesis - Davy Souza](docs/Davy_Masters_thesis.pdf)
