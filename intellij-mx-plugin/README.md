# MX Build Tool Plugin for IntelliJ IDEA

An IntelliJ IDEA plugin that provides first-class support for projects using the [MX build tool](https://github.com/graalvm/mx), similar to Maven or Gradle integration.

## Features

### Project Structure
- **Automatic Detection**: Automatically detects MX projects by finding `mx.*/suite.py` files
- **Project Import**: Imports MX suite structure into IntelliJ modules
- **Dependency Resolution**: Maps MX projects and libraries to IntelliJ modules and libraries
- **Source Roots**: Automatically configures source directories from suite.py

### Build Integration
- **Build Tasks**: Run MX build commands directly from the IDE
- **Clean Support**: Clean build artifacts with mx clean
- **Build Output**: View build output in IntelliJ's build console
- **Error Navigation**: Click on errors to navigate to source

### Test Runner
- **Unit Tests**: Run MX unit tests with `mx unittest`
- **Test Patterns**: Filter tests by pattern
- **Verbose Output**: Optional verbose test output
- **Test Results**: View test results in IntelliJ's test runner UI

### Debug Support
- **Debug Configurations**: Create debug configurations for MX builds and tests
- **Breakpoints**: Set breakpoints and debug Java code
- **Remote Debugging**: Attach debugger to MX processes

### IDE Actions
- **Tools Menu**: MX menu in Tools with Build, Clean, Test, and Sync actions
- **Tool Window**: Dedicated MX tool window for build operations
- **Configuration**: Project settings for MX path and build options

## Installation

### From Source

1. Clone this repository
2. Build the plugin:
   ```bash
   cd intellij-mx-plugin
   ./gradlew buildPlugin
   ```
3. Install in IntelliJ:
   - Go to Settings → Plugins → ⚙️ → Install Plugin from Disk
   - Select `build/distributions/intellij-mx-plugin-1.0.0.zip`
   - Restart IntelliJ

### Requirements

- IntelliJ IDEA 2023.3 or later (Community or Ultimate)
- Java 17 or later
- MX build tool installed and in PATH (or configure path in settings)

## Usage

### Opening an MX Project

1. **File → Open** and select a directory containing an MX suite
2. The plugin will automatically detect the `mx.*/suite.py` file
3. Project structure will be imported automatically

### Building

**From Menu:**
- Tools → MX → Build Project

**From Run Configuration:**
- Run → Edit Configurations → + → MX Build
- Set command (e.g., "build", "build --force")
- Run the configuration

### Running Tests

**From Menu:**
- Tools → MX → Run Tests

**From Run Configuration:**
- Run → Edit Configurations → + → MX Test
- Set test pattern (optional)
- Enable verbose output (optional)
- Run the configuration

### Debugging

1. Create an MX Build or MX Test run configuration
2. Click the Debug button (🐞) instead of Run
3. Debugger will attach to the MX process

### Syncing Project Structure

If you modify `suite.py`, sync the project structure:
- Tools → MX → Sync Project Structure

## Configuration

### Project Settings

Settings → Build, Execution, Deployment → Build Tools → MX

- **MX Executable Path**: Path to mx binary (leave empty to use PATH)
- **Default JDK**: Default JDK for MX builds
- **Verbose Build**: Enable verbose build output

## Architecture

The plugin consists of several key components:

### Model (`com.oracle.graalvm.mx.model`)
- `MxSuite`: Represents an MX suite from suite.py
- `MxProject`: Represents a Java project in the suite
- `MxLibrary`: Represents external library dependencies
- `MxDistribution`: Represents distribution artifacts

### Parser (`com.oracle.graalvm.mx.parser`)
- `MxSuiteParser`: Parses suite.py using Jython

### Project (`com.oracle.graalvm.mx.project`)
- `MxProjectStructureDetector`: Detects MX projects
- `MxProjectImporter`: Imports suite structure into IntelliJ
- `MxModuleType`: Custom module type for MX

### Build (`com.oracle.graalvm.mx.build`)
- `MxCommandExecutor`: Executes MX commands
- `MxBuildTask`: Background task for builds
- `MxBuildSettings`: Persistent build settings

### Run (`com.oracle.graalvm.mx.run`)
- `MxBuildConfigurationType`: Run configuration for builds
- `MxTestConfigurationType`: Run configuration for tests
- `MxDebugRunner`: Debug support

## Development

### Building from Source

```bash
# Build the plugin
./gradlew buildPlugin

# Run IntelliJ with the plugin in a sandbox
./gradlew runIde

# Run tests
./gradlew test
```

### Project Structure

```
intellij-mx-plugin/
├── src/main/
│   ├── java/com/oracle/graalvm/mx/
│   │   ├── model/          # Data models for MX concepts
│   │   ├── parser/         # suite.py parser
│   │   ├── project/        # Project detection and import
│   │   ├── build/          # Build execution
│   │   ├── run/            # Run configurations
│   │   ├── actions/        # IDE actions
│   │   ├── settings/       # Settings UI
│   │   ├── toolwindow/     # Tool window
│   │   ├── external/       # External system integration
│   │   └── lang/           # File types
│   └── resources/
│       └── META-INF/
│           └── plugin.xml  # Plugin descriptor
├── build.gradle.kts        # Build configuration
└── README.md              # This file
```

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This plugin is licensed under the same license as the MX build tool.

## Support

For issues and feature requests, please use the [GitHub issue tracker](https://github.com/graalvm/mx/issues).

## Related Projects

- [MX Build Tool](https://github.com/graalvm/mx)
- [GraalVM](https://github.com/oracle/graal)

## Acknowledgments

This plugin integrates with the MX build tool developed by the GraalVM team at Oracle.
