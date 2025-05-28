# usgs-request

`usgs-request` is a library for interacting with the USGS REST API services. It retrieves site and time-series data from the following endpoints:

- **[Site Service](https://waterservices.usgs.gov/docs/site-service/)** – Retrieve metadata about monitoring locations
- **[Instantaneous Values Service](https://waterservices.usgs.gov/docs/instantaneous-values/instantaneous-values-details/)** – Access near real-time measurements
- **[Daily Values Service](https://waterservices.usgs.gov/docs/dv-service/daily-values-service-details/)** – Access daily summaries of hydrologic data

## Features

- Query and retrieve data from USGS water services
- Support for site, instantaneous values, and daily values services
- Time-series data fetching for analysis or storage
- Parse rdb and json strings from USGS

## Getting Started

### Prerequisites

- [Java 17+](https://adoptopenjdk.net/)
- [Gradle](https://gradle.org/)
- Internet access (to access USGS services)

### Installation

Clone the repository:

```bash
git clone https://github.com/HydrologicEngineeringCenter/usgs-request.git
cd usgs-request
```

Build the project:
```bash
./gradlew build
```
## Versioning

See [SemVer](http://semver.org/).

## License

This project is licensed under the MIT License. See the [LICENSE.MD](LICENSE.MD) file for details.