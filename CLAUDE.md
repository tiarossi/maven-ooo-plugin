# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Maven plugin for building OpenOffice.org and LibreOffice extensions (.oxt files). It automates the compilation of IDL (Interface Definition Language) files, generation of Java bindings, and packaging of extensions.

**Technology Stack**: Java 8, Maven 3.x

## Building and Testing

### Build Commands
```bash
# Build the plugin
mvn clean install

# Run tests
mvn test

# Run a specific test
mvn test -Dtest=ConfigurationManagerTest

# Skip tests during build
mvn clean install -DskipTests
```

### Test Configuration
- Tests require OpenOffice/LibreOffice and SDK installations
- Test paths are hardcoded in `ConfigurationManagerTest` (lines 54-56)
- Tests exclude Abstract* classes via surefire configuration in pom.xml

## Architecture

### Maven Goals (Mojos)

The plugin provides these goals for extension development:

1. **`build-idl`** (IdlBuilderMojo) - Compiles IDL files to Java classes
   - Phase: generate-sources
   - Process: IDL → unoidl-write → types.rdb → javamaker → Java classes

2. **`oxt`** (OxtMojo) - Packages extension into .oxt archive
   - Phase: package
   - Creates JAR from compiled classes, packages with OXT resources

3. **`install`** (OOoInstalMojo) - Installs extension to OpenOffice/LibreOffice
   - Phase: install
   - Uses `unopkg add` command (or `unopkg gui` with showGui=true)

4. **`uninstall`** (OOoUninstalMojo) - Removes installed extension
   - Extracts extension identifier from description.xml
   - Uses `unopkg remove` command

5. **`debug`** (OOoDebugMojo) - Launches OpenOffice/LibreOffice in debug mode
   - Executes install phase first
   - Supports JPDA debugging via jpda.address parameter

### Core Components

**ConfigurationManager** - Static singleton managing build configuration and tool execution
- Stores paths to OpenOffice/LibreOffice, SDK, output directories
- Provides `runCommand()` for executing SDK tools (idlc, javamaker, unopkg, unoidl-write)
- Sets up environment variables (PATH, LD_LIBRARY_PATH, DYLD_LIBRARY_PATH, UserInstallation)
- Handles cross-platform command execution

**Environment** - OS-specific path resolution and detection
- Auto-detects OpenOffice/LibreOffice installation from PATH or common locations
- Handles platform differences (Windows/Mac/Linux) for:
  - Program locations (soffice, unopkg)
  - Library paths (bin vs lib directories)
  - Basis/URE directory structures
- Key methods: `getOfficeHome()`, `getOoSdkHome()`, `getOoSdkUreHome()`

**Visitor Pattern** for file processing:
- `IVisitor` interface with `visit(IVisitable)` method
- `VisitableFile` extends File, implements `IVisitable`
- `IdlcVisitor` processes .idl files using unoidl-write
- Pattern allows recursive directory traversal with custom file operations

### IDL Build Process

1. **IdlcVisitor** walks IDL directory tree, compiles each .idl file:
   - Runs `unoidl-write` with offapi.rdb, udkapi.rdb, project IDL → types.rdb
   - Output: target/urd/{package}/types.rdb files

2. **IdlBuilderMojo.generatesClasses()** generates Java bindings:
   - Runs `javamaker` with types.rdb and OOo type libraries
   - Guesses root module by finding unique child path in IDL directory structure
   - Handles LibreOffice 4+ vs older versions (different javamaker flags)
   - Output: Java classes in target/classes

### Extension Packaging

**AbstractOxtMojo** - Base class for JAR/OXT creation
- Uses Maven Archiver and Plexus JarArchiver
- Handles includes/excludes filtering
- Supports custom manifest files

**OxtMojo** execution flow:
1. Creates JAR from compiled classes (via AbstractOxtMojo.execute())
2. Excludes oxt/** and idl/** from JAR
3. Uses UnoPackage class (from ooo-plugin-packager-core dependency) to build .oxt
4. Adds attached JAR artifacts as components
5. Adds lib/*.jar files as components
6. Adds oxt directory contents from src/main/resources

## Configuration Parameters

Maven plugin parameters (configured in pom.xml):

- `ooo` - Path to OpenOffice/LibreOffice installation
- `sdk` - Path to OpenOffice SDK
- `idlDir` - IDL source directory (default: src/main/resources/idl)
- `oxtDir` - OXT resource directory (default: src/main/resources)
- `libDir` - External JARs to bundle (default: target/lib)
- `userInstallation` - User profile directory for install/debug (default: target/soffice_debug)
- `showGui` - Show Extension Manager GUI during install (default: false)
- `jpda.address` - JPDA debug address for remote debugging

## Important Implementation Details

### Command Execution
- All SDK tool execution goes through `ConfigurationManager.runCommand()`
- Environment setup adds SDK bin paths, URE paths, and library paths to environment
- Commands log output to Maven log (info level for stdout, warn for stderr)
- Returns exit code for error handling

### Path Resolution Priority
1. Explicit configuration parameters (ooo, sdk)
2. Environment variables (OFFICE_HOME, OO_SDK_HOME)
3. System PATH scanning
4. Platform-specific default locations

### Cross-Platform Tool Names
- Windows: `unopkg.com`, `soffice.exe`
- Unix/Mac: `unopkg`, `soffice`
- SDK tools (javamaker, unoidl-write) are platform-agnostic in recent versions

### Recent Changes (from git status)
- RegmergeVisitor removed (regmerge no longer used with unoidl-write approach)
- Updated to support LibreOffice 4+ javamaker syntax
- Install uses UserInstallation path instead of system-wide installation
