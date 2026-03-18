# Project Guidelines: Bacillus

This document provides essential information for developers working on the Bacillus project, a LibGDX-based simulation game written in Kotlin.

### 1. Build and Configuration

The project uses Gradle as its build system and consists of two main modules: `core` (shared logic and assets) and `desktop` (desktop-specific launcher).

#### Key Commands:
- **Run the Application**:
  ```bash
  ./gradlew desktop:run
  ```
- **Build the Project**:
  ```bash
  ./gradlew build
  ```
- **Clean the Project**:
  ```bash
  ./gradlew clean
  ```

#### Requirements:
- JDK 11 or higher is recommended (the project uses Kotlin 1.6.0).
- For macOS ARM64 (Apple Silicon), the `build.gradle` automatically handles the necessary LWJGL natives.

### 2. Testing Information

The project uses **JUnit 5**, **Mockito**, and `kotlin-test` for unit testing. All tests are located in the `core` module.

#### Running Tests:
- **Run All Tests**:
  ```bash
  ./gradlew core:test
  ```
- **Run a Specific Test Class**:
  ```bash
  ./gradlew core:test --tests "com.invenit.bacillus.model.OrganicTest"
  ```

#### Adding New Tests:
New tests should be placed in `core/src/test/com/invenit/bacillus/`.
Follow the existing naming convention: `Test<Feature>Stage.kt` or `<ClassName>Test.kt`.

**Example Test Structure**:
```kotlin
package com.invenit.bacillus

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class SimpleVerificationTest {
    @Test
    fun `test that always passes`() {
        assertTrue(true, "This should always be true")
    }
}
```

### 3. Development Information

#### Code Style:
- **Language**: Kotlin 1.6.0.
- **Naming**: Use standard Kotlin/Java naming conventions (PascalCase for classes, camelCase for methods/variables).
- **Concurrency**: The simulation logic is mostly single-threaded, driven by the LibGDX render loop or stages.
- **Simulation Logic**: The simulation is divided into "Steps" (found in `com.invenit.bacillus.stage`). Each step handles a specific part of the simulation tick (e.g., `ConsumeStep`, `MoveStep`, `ProduceStep`).

#### Key Architectural Components:
- **Environment**: Manages the simulation state and the `Field`.
- **Field**: A 2D grid storing `Something` (Organic or Mineral entities).
- **DNA**: Defines the behavior and traits of an `Organic` entity.
- **Settings**: Centralized configuration for simulation parameters (yields, consumption rates, ranges).

#### Debugging:
- The UI includes several debug stages (`DebugStage`, `CellDetailsStage`, `SlidersStage`) which can be used to inspect and modify the simulation state at runtime.
- Check `BacillusGdxGame.kt` to see how these stages are initialized and toggled.
