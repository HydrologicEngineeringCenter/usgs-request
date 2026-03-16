# usgs-request

`usgs-request` is a library for interacting with the [USGS OGC API](https://api.waterdata.usgs.gov/ogcapi/v0/). It retrieves monitoring location and time-series data from the following collections:

- **[Monitoring Locations](https://api.waterdata.usgs.gov/ogcapi/v0/collections/monitoring-locations/)** – Retrieve metadata about monitoring locations
- **[Time Series Metadata](https://api.waterdata.usgs.gov/ogcapi/v0/collections/time-series-metadata/)** – Access metadata about available time series
- **[Daily Values](https://api.waterdata.usgs.gov/ogcapi/v0/collections/daily/)** – Access daily summaries of hydrologic data
- **[Continuous Values](https://api.waterdata.usgs.gov/ogcapi/v0/collections/continuous/)** – Access continuous (instantaneous) measurements

## Features

- Query and retrieve data from USGS OGC API services
- Support for monitoring locations, time-series metadata, daily values, and continuous values collections
- Time-series data fetching for analysis or storage
- JSON response parsing

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