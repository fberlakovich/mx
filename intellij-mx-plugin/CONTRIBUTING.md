# Contributing to MX IntelliJ Plugin

Thank you for your interest in contributing to the MX IntelliJ IDEA plugin!

## Development Setup

1. **Prerequisites**:
   - IntelliJ IDEA 2023.3+ (Community or Ultimate)
   - JDK 17+
   - Gradle (included via wrapper)

2. **Clone and Build**:
   ```bash
   git clone <repository-url>
   cd intellij-mx-plugin
   ./gradlew buildPlugin
   ```

3. **Run in Development**:
   ```bash
   ./gradlew runIde
   ```
   This will start IntelliJ in a sandbox with the plugin installed.

## Project Structure

- `src/main/java/` - Java source code
  - `model/` - Data models for MX suite, projects, libraries
  - `parser/` - Suite.py parser using Jython
  - `project/` - Project detection and import
  - `build/` - Build command execution
  - `run/` - Run configurations for build and test
  - `actions/` - IDE actions (menu items)
  - `settings/` - Settings UI
  - `toolwindow/` - MX tool window
  - `external/` - External system integration
  - `lang/` - File type definitions

- `src/main/resources/META-INF/plugin.xml` - Plugin descriptor

## Testing

Run automated tests:
```bash
./gradlew test
```

Manual testing checklist:
- [ ] Open an MX project
- [ ] Verify project structure is imported
- [ ] Run build action
- [ ] Run test action
- [ ] Create and run build configuration
- [ ] Create and run test configuration
- [ ] Modify settings

## Code Style

- Follow standard Java conventions
- Use IntelliJ's default code style
- Add JavaDoc for public APIs
- Keep lines under 120 characters

## Submitting Changes

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## Debugging Tips

- Use `./gradlew runIde` to run IntelliJ with the plugin
- Check `idea.log` in the sandbox for errors
- Use IntelliJ's internal mode for additional debugging tools

## Resources

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/)
- [MX Documentation](https://github.com/graalvm/mx/blob/master/README.md)
- [IntelliJ Plugin Development Forum](https://intellij-support.jetbrains.com/hc/en-us/community/topics/200366979-IntelliJ-IDEA-Open-API-and-Plugin-Development)
