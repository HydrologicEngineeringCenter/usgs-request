# AGENTS.md

This file provides guidance to AI agents when working with code in this repository.

## Standards

Refer to @STANDARDS.md for all coding standards and conventions.
Follow everything defined there in addition to the instructions in this file.

## Project Overview

usgs-request is a Java library for retrieving and parsing hydrologic data from the USGS Water Services APIs. It provides request builders, parsers, and data models for working with USGS monitoring locations, time series metadata, and continuous/daily values.

## Build Commands

```bash
# Run all tests
gradlew test

# Build distribution
gradlew build
```

## Architecture

### Key Patterns
- **Builder pattern**: Request objects use builders for configuration (e.g., `UsgsContinuousValuesRequest`, `UsgsDailyValuesRequest`)
- **Parser pattern**: Dedicated parsers translate USGS JSON responses into domain objects (e.g., `UsgsValuesParser`, `UsgsMonitoringLocationParser`)
- **Immutable data models**: Domain objects like `UsgsGageRecord`, `UsgsMonitoringLocation` are immutable

### Main Source Structure
```
src/main/java/
└── mil/army/usace/hec/usgs/io/
    ├── Usgs*Request.java        # API request builders
    ├── Usgs*Parser.java         # JSON response parsers
    ├── UsgsGageRecord.java      # Time series data model
    ├── UsgsMonitoringLocation.java  # Site/gage metadata
    └── UsgsParameter.java       # USGS parameter codes
```