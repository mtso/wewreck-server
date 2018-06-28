# wehack
===================
<--------------- Write the descriptions here ------------------->

## Requirements

- JDK 8+.
- [Docker on macOS](https://docs.docker.com/docker-for-mac/install/). 
- [Homebrew](http://brew.sh/).

## Kubernetes Configurations

Application is run on Kubernetes (K8S). Required configurations are [here](https://bitbucket.devops.wepay-inc.com/projects/DEVTOOLS/repos/service-configs/browse/wehack).

## Build Instructions

### Run the service

* Run in developement environment:

        bash cli/dev_run.sh

* Run with Docker locally:

        bash cli/start.sh

* Stop and remove a running Docker container:

        bash cli/stop.sh

### Build a new Docker Image

Ensuring that the Docker daemon is running, run the following:

	./gradlew clean dockerBuild
	
### Release process

Every commit on master branch is considered to be a new release. The artifacts are versioned by Teamcity.
     
## Testing

### Testing Endpoints

    bash cli/test_endpoints.sh

## Endpoints

### Default Endpoints

|Endpoint | Description| Sample return value|
|---------|------------|--------------------|
| /ping |Service availability | 200 OK |
| /healthcheck |Health check of dependent services | [{"status":"OK","service":"cloudsql"},{"status":"OK","service":"cassandra"},{"status":"OK","service":"kafka"},{"status":"OK","service":"bigquery"},{"status":"OK","service":"storage"},{"status":"OK","service":"pubsub"},{"status":"OK","service":"redis"}]   |
| /buildinfo | Project Version information | {"version":"0.0.1","service_name":"wehack"} |


### NewRelic Integration

Service is integrated with New Relic to monitor the following:

* App server historical average response time (ms)
* Historical throughput (rpm)
* Transactions
* Error rate
* Recent events
* Server specific information
* …and other default information

### Grafana Monitoring

HTTP requests are logged to the graphite server by default. Graphs can be created using the same data in grafana.
 
## TeamCity

TeamCity project can be found [here](<update project url>).



