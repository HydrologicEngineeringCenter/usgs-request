usgs-request Codebase Standards
=======================
Codebase standards for the usgs-request library.

# Branching and Commits

## Branching
All branches should initiate from a Jira issue. The branch name should include the Jira issue number in the name so
that branch can be linked to Jira.

Branches should be lightweight units of work that stand on their own. Code changes on a branch are typically finalized 
with one commit. If the number of commits in a branch goes beyond one, the changes should probably be submitted on 
multiple branches, or the commit history should be squashed.

The base branch is `main`. All branches must be created from `main`
and all pull requests must target `main`.

The main branch ***must*** remain deployable at all times. No pull request should be made to main with partially
complete work that is dependent on another branch.

## Commit History
Commit histories should read like a logical list of steps performed to implement a new feature or bug fix.
If your commit history reads like an initial commit followed by a series of minor changes and modifications to the
initial commit, squash your minor changes and modifications into the initial commit. There is no value in storing
changes that should have been a part of the initial commit, as their own commits. This clutters the commit history.

All commits must stand on their own. Group like changes into a single commit.

***Never*** make a commit that does not compile.

## Commit Messages

For commit message format, see [How to Write a Git Commit Message](https://cbea.ms/git-commit/).

Commit message requirements:
1. Separate subject from body with a blank line
2. Limit the subject line to 50 characters
3. Capitalize the subject line
4. Do not end the subject line with a period
5. Do not put issue tags or ticket numbers in the subject line
6. Use the imperative mood in the subject line
7. Do not wrap the body. Write the body as a single continuous paragraph with no manual line breaks
8. Use the body to explain what and why vs. how

## Your Identity

For commits made to the repo, your Git username must follow the form < First > < Last >. Capitalize the first letter of 
your first and last name. For more information on establishing your Git identity, see 
[First-Time Git Setup](https://git-scm.com/book/en/v2/Getting-Started-First-Time-Git-Setup).

# Dependencies

## Adding New Libraries
- Do **not** add new libraries, packages, or dependencies without explicit permission.
- Before introducing any new dependency, ask and wait for approval.
- This applies to all package managers and dependency files, including but not limited to:
    - `build.gradle` (Java)

- If a task seems to require a new library, first attempt to solve it using:
    1. Existing installed dependencies.
    2. Standard library / built-in language features.
- If a new library is genuinely necessary, **propose it explicitly** — state the library name, its purpose, and why existing dependencies cannot fulfill the need — then wait for approval before installing or importing it.

# Mandatory Code Inspections
Before submitting a pull request, run the following code inspections in IntelliJ on your diff. Since the inspection
will be run against uncommitted changes, you may need to use Git reset soft to uncommit changes that need to be 
inspected.

In IntelliJ, code inspections can be launched from Code | Analyze Code | Run Inspection by Name.

Note: If you use a different IDE, perform equivalent inspections or use IntelliJ simply for running inspections.

## Declaration access can be weaker
Order: Of the mandatory code inspections, run this one first.\
Code inspection name: "Declaration access can be weaker, Java | Declaration redundancy".\
Inspection scope: "Custom scope: Only changes uncommitted to the VCS in all files".\
Instructions: Run the inspection. Apply suggestions. Because each iteration of the inspection is dependent
on the previous, run the inspection until no issues are reported.

## Unused declaration
Order: Of the mandatory code inspections, run this after "Declaration access can be weaker".\
Code inspection name: "Unused declaration, Java | Declaration redundancy".\
Inspection scope: "Custom scope: Only changes uncommitted to the VCS in all files".\
Instructions: Run the inspection. Apply suggestions. Because each iteration of the inspection is dependent 
on the previous, run the inspection until no issues are reported.

## Unused import
Order: Of the mandatory code inspections, run this after "Unused declaration".\
Code inspection name: "Unused import, Java | Imports".\
Inspection scope: "Custom scope: All changed files".\
Instructions: Run the inspection. Apply suggestions.

# Style

## Naming Instance Variables
Do not use preceding underscores for instance variables.
In modern IDEs it is easy to distinguish instance variables while the underscores diminish readability.

## Curly Brace Style

Use K&R brace style.

All control flow statements MUST use curly braces,
even for single statements.

### Example
```
if (valid) {
    process();
} else {
    handleError();
}
```

### Enforcement

Agents MUST ensure:

1. All control statements use curly braces
2. Brace placement follows K&R style
3. No single-line unbraced statements exist

## Naming Classes and Variables

Agents MUST use camelCase naming conventions for all classes and variables.

### Rules

1. Use camelCase naming.
2. Avoid multiple capital letters in a row.
3. Acronyms MUST be treated as normal words (only first letter capitalized).

### Correct

```java
UsgsSedimentCalculator calculator;
RiverFlowAnalyzer analyzer;
```

## Logical Operators
For multi-line logical operators, put logical operators at the beginning of the new line.
```
//good
if (maskArray[i] == 1 
    && (i / nx >= ny - 1 || maskArray[i + nx] == 0)) {
        //do something
}
```

## Commenting code
Always comply with this Javadoc standard whenever you generate Javadoc comments:
[Javadoc Coding Standards](https://blog.joda.org/2012/11/javadoc-coding-standards.html).
If there is any conflict with other instructions, ask before proceeding.

Before finalizing, verify Javadoc against the Joda standard:
- Summary line is a single sentence, third-person, ends with a period.
- Blank line between description and block tags.
- Tags are complete and accurate: `@param`, `@return`, `@throws`.
- Inline formatting uses `{@code}` and `{@link}` appropriately.

## Deprecating classes or methods
Annotate the method with `@Deprecated`. Add a javadoc that 
* tags the method as `@deprecated` 
* indicates the version that the class or method was deprecated
* indicates the class or method that should be used instead

Deprecated objects and methods should not 
persist in the code base for very long but should be refactored out.
```java
/**
 * @deprecated since 0.10.16, replaced by {@link #options}
 * @param geoOptions  the geographic options
 * @return the builder
 */
@Deprecated
public Builder geoOptions(final Options geoOptions){
    Optional.ofNullable(geoOptions).ifPresent(o -> this.geoOptions.putAll(o.getOptions()));
    return this;
}
```

# Programming Principles

## Object-Oriented Programming (OOP)
Follow OOP principles in all code written for this project:

- **Encapsulation:** Bundle data and the methods that operate on it within classes. Keep internal state private; expose only what is necessary via public interfaces.
- **Abstraction:** Hide implementation details behind well-defined interfaces or abstract base classes. Consumers should depend on abstractions, not concrete implementations.
- **Inheritance:** Use inheritance to model true "is-a" relationships and promote code reuse. Prefer shallow inheritance hierarchies to avoid complexity.
- **Polymorphism:** Design classes so that objects of different types can be used interchangeably through a common interface (method overriding, interfaces, generics).

## General OOP Guidelines
- Prefer composition over inheritance when appropriate.
- Follow the **SOLID** principles:
    - **S**ingle Responsibility — each class should have one reason to change.
    - **O**pen/Closed — open for extension, closed for modification.
    - **L**iskov Substitution — subtypes must be substitutable for their base types.
    - **I**nterface Segregation — no class should be forced to implement methods it doesn't use.
    - **D**ependency Inversion — depend on abstractions, not concretions.
- Keep classes small and focused.
- Avoid deeply nested logic; extract behavior into well-named methods.

# Code
## Encapsulating Objects

### Rules

- All **data/model classes must be immutable** by default.
- **Do not write setters** for data/model classes.
- If state changes are required, encapsulate them fully within the
  class — never expose mutable state directly.

### Guidelines

- Prefer `final` fields in model classes.
- Use constructors or static factory methods to set initial state.
- If a "modified" version of a model object is needed, return a new instance
  rather than mutating the existing one.
- Never expose internal mutable collections directly — return copies or
  unmodifiable views (e.g., `Collections.unmodifiableList(...)`).

## Access Levels

### Rules

- Always use the **most restrictive access level** possible for every member
  variable and method.
- Prefer access levels in this order: `private` → package-private → `protected` → `public`.
- Any `public` declaration is part of the **public API** and must be intentional
  and justified.
- Organize classes into packages so the resulting public API is minimal and deliberate.

## Unit Testing

### Rules

- All code must be unit testable and must have unit tests.
- Unit tests must be written alongside code — do not defer them.

### Guidelines

- Design model classes for testability: prefer constructor injection over
  hard-coded dependencies so tests can supply their own inputs.
- Each test should cover a single behavior or code path.
- Test edge cases explicitly (nulls, empty collections, boundary values).
- Tests must not depend on external systems (no file I/O, no network, no
  database) — mock or stub dependencies as needed.
- Use descriptive test method names that state what is being tested and what
  the expected outcome is.

## Creating Objects

### Rules

- Use a **builder** when a constructor or factory method would require **3 or
  more arguments**.
- Use a **constructor or static factory method** when fewer than 3 arguments
  are needed.

### Guidelines

- Builders should enforce required fields and validate state before constructing
  the object.
- Static factory methods are preferred over constructors when the method name
  adds clarity (e.g., `of(...)`, `from(...)`, `create(...)`).
- Do not mix builders and setters — if a class uses a builder, its fields
  should be `final` and set only at construction time (see Encapsulating
  Objects).

### Examples

```java
// < 3 arguments — use a constructor or static factory method
Point point = new Point(x, y);
Duration duration = Duration.ofSeconds(30);

// >= 3 arguments — use a builder
HttpRequest request = HttpRequest.builder()
        .url(url)
        .method(method)
        .timeout(timeout)
        .build();
```

## Operating on Lists

### Rules

- Prefer the **Java 8 Streams API** for common list operations such as
  filtering, mapping, sorting, and reducing.
- Avoid manual `for` loops when an equivalent stream operation is clear and
  readable.

### Guidelines

- Use `stream()`, `filter()`, `map()`, `sorted()`, `reduce()`, `collect()`,
  and related methods instead of imperative loop logic.
- Prefer `Collectors.toList()`, `Collectors.toMap()`, or `Collectors.groupingBy()`
  over manually building collections in a loop.
- Use method references (`Class::method`) instead of lambdas where they improve
  readability.
- Do not use parallel streams unless there is a demonstrated performance need
  and thread safety has been verified.

### Examples

```java
// Filtering
List<User> activeUsers = users.stream()
        .filter(User::isActive)
        .collect(Collectors.toList());

// Mapping
List<String> names = users.stream()
        .map(User::getName)
        .collect(Collectors.toList());

// Sorting
List<User> sorted = users.stream()
        .sorted(Comparator.comparing(User::getName))
        .collect(Collectors.toList());

// Reducing
int total = orders.stream()
        .mapToInt(Order::getAmount)
        .sum();
```        

## File I/O: Use Java NIO (java.nio.file.*)
Always use the Java NIO file API. Never use `java.io.File` for new code.

| Instead of (`java.io.File`) | Use (`java.nio.file.*`) |
|---|---|
| `new File(path)` | `Paths.get(path)` |
| `File.exists()` | `Files.exists(path)` |
| `File.mkdirs()` | `Files.createDirectories(path)` |
| `File.delete()` | `Files.delete(path)` |
| `File.listFiles()` | `Files.list(path)` / `Files.walk(path)` |
| `FileInputStream` / `FileOutputStream` | `Files.newInputStream(path)` / `Files.newOutputStream(path)` |
| `FileReader` / `FileWriter` | `Files.newBufferedReader(path)` / `Files.newBufferedWriter(path)` |

**Rules:**
- Import from `java.nio.file.*` (`Path`, `Paths`, `Files`, `FileSystem`).
- Never import or instantiate `java.io.File` in new code.
- When modifying legacy code that uses `java.io.File`, migrate it to NIO
  in the same change if it is within scope. If not, flag it with a
  `// TODO: migrate to java.nio.file` comment.
- Prefer `Files.walk()` or `Files.find()` over manual directory recursion.
- Always handle `IOException` explicitly — do not silently swallow it.

## Error Handling

### Checked vs. Unchecked Exceptions

**Checked Exceptions (`throws` / `try-catch`)**
- Use for **recoverable, expected failure conditions** that callers should
  explicitly handle (e.g., file not found, invalid user input, network
  timeout).
- All public API methods that perform I/O, parsing, or external calls must
  declare or handle checked exceptions explicitly.
- Never declare `throws Exception` or `throws Throwable` — always use the
  most specific exception type available.

**Unchecked Exceptions (`RuntimeException` and subclasses)**
- Use for **programming errors** that should never occur in correct code
  (e.g., null arguments, illegal state, out-of-bounds index).
- Throw `IllegalArgumentException` for invalid method arguments.
- Throw `IllegalStateException` for invalid object state.
- Do not catch `RuntimeException` broadly unless at a top-level boundary
  (e.g., a global UI handler).

**Never:**
- Swallow exceptions silently:
  ```java
  // BAD
  catch (IOException e) { }

  // GOOD
  catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to read config file: " + path, e);
      throw new RuntimeException("Failed to read config file: " + path, e);
  }
  ```

## Logging Messages

### Rules

- Use the **Java Logging API** (`java.util.logging.Logger`) for all log output.
- **Never use `System.out`** or `System.err` for logging or debugging output.

### Guidelines

- Declare a logger as a `private static final` field on each class:
  ```java
  private static final Logger LOGGER = Logger.getLogger(MyClass.class.getName());
  ```

# Testing

## Using Test Resources

### Rules

- **Avoid disk I/O** in tests as much as possible.
- If test resource files are required, place them in `src/test/resources`.
- Use **JSON** to serialize and deserialize complex test data (arrays, lists,
  objects, etc.).

### Guidelines

- Keep resource files small and focused on the specific test case.
- Prefer in-memory data construction over loading files when the data is simple
  enough to express directly in code.
- For JSON usage in tests, use the json-simple library already included as a dependency.

## Mocking Behavior

### Rules

- Use **Mockito** for all mocking in unit tests.

### Guidelines

- Use `@Mock` and `@InjectMocks` annotations with `MockitoExtension` to reduce
  boilerplate:
  ```java
  @ExtendWith(MockitoExtension.class)
  class MyServiceTest {
      @Mock
      MyDependency dependency;

      @InjectMocks
      MyService service;
  }
  ```