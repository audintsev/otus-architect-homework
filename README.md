# Homework for lessons 6, 7 and 11 (see tags) of the [Otus "Software Architect" course](https://otus.ru/lessons/arhitektor-po/) 

## Deploying as Helm release

Deploying:
```
git clone https://github.com/audintsev/otus-architect-homework.git
cd otus-architect-homework
helm install udintsev-hw ./chart
```

Undeploying:
```
helm uninstall udintsev-hw
```

Optionally, to delete also PVC: `kubectl get pvc`, followed by: `kubectl delete pvc data-udintsev-hw-postgresql-0`


## Invoking the postman collection

After deploying, wait till `http://arch.homework/otusapp/audintsev/actuator/health/readiness`
starts to report readiness and then `cd` to the root of the cloned repo and execute:
```
newman run postman_collection.json 
```

## Invoking with curl

Assuming `arch.homework` resolves to the IP address where Ingress controller listens,

List people:
```
curl http://arch.homework/otusapp/audintsev/person
```

Add a person:
```
curl -H 'Content-Type: application/json' http://arch.homework/otusapp/audintsev/person -d '{"first": "Some", "last": "Other"}'
```

Get a specific person:
```
curl http://arch.homework/otusapp/audintsev/person/1
```

Update a person:
```
curl -H 'Content-Type: application/json' -X PUT http://arch.homework/otusapp/audintsev/person/2 -d '{"first": "Yet", "last": "Another"}'
```

Delete a person:
```
curl -X DELETE http://arch.homework/otusapp/audintsev/person/2
```

## Application

Application-wise, this repository showcases the following technologies being used in one simple web application:

* Reactive stack: Spring WebFlux and R2DBC
* Testcontainers to run integration tests with Postgresql
* Spring REST Docs to generate API documentation out of API tests

TODO (?):

* Building native image
* CI with Github actions (possibly publishing generated API docs as a Github pages site)
  
Comments:

* I don't use embedded DB migrations (managed by Liquibase or Flyway). Tests create schema defined in
[schema.sql](src/test/resources/schema.sql). When running _productively_ schema needs to be pre-created
by a Kubernetes job. The reasons for choosing this design are:
  * I'd like to experiment with Kubernetes jobs
  * neither Liquibase nor Flyway can natively work R2DBC connections
  * most importantly, my current impression is that app-managed DB migrations feel alien in the new
microservices/cloud native world: with app-managed migrations an application typically refuses to run if actual schema
version doesn't match what application expects; but that's exactly what seems to happen more often than not:
a new application version makes some additions to the schema (e.g. a new column), and the two versions - the old
one and the new one - co-exist in the same deployment. 

### App: building and running

Building:

```
./gradlew bootJar
```

Running (assuming Postgres is available on localhost:5432):

```
java -jar otus-architect-homework-0.0.1-SNAPSHOT.jar --spring.r2dbc.url=r2dbc:postgresql://localhost/test --spring.r2dbc.username=test --spring.r2dbc.password=test
```

### Image: building and pushing

```
./gradlew bootBuildImage --imageName=udintsev/hw:latest
docker push udintsev/hw:latest
```

### Useful links

* [R2DBC on Spring Framework](https://docs.spring.io/spring-framework/docs/5.3.0-RC2/reference/html/data-access.html#r2dbc)
* [WebFllux on Spring Framework](https://docs.spring.io/spring-framework/docs/5.3.0-RC2/reference/html/web-reactive.html#spring-webflux)
* [R2DBC support in Testcontainers](https://www.testcontainers.org/modules/databases/r2dbc/)
* [Spring REST Docs](https://docs.spring.io/spring-restdocs/docs/current/reference/html5/) 

## Gathering pod metrics for CPU and memory usage

Container metrics can be provided by [cAdvisor](https://github.com/google/cadvisor),
available metrics are documented [here](https://github.com/google/cadvisor/blob/master/docs/storage/prometheus.md).

Prometheus has a corresponding [guide](https://prometheus.io/docs/guides/cadvisor/)

When using [prometheus stack](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack),
collecting container metrics is enabled out of the box.

Some further links:
* https://coreos.com/blog/monitoring-kubernetes-with-prometheus.html
* https://itnext.io/k8s-monitor-pod-cpu-and-memory-usage-with-prometheus-28eec6d84729
* https://github.com/kubernetes/kube-state-metrics/blob/master/docs/pod-metrics.md

## Gathering Postgres metrics

[Postgres exporter](https://github.com/wrouesnel/postgres_exporter) can be used to export metrics.

There is a [helm chart](https://github.com/prometheus-community/helm-charts/tree/main/charts/prometheus-postgres-exporter)
for prometheus to install the exporter.

We're using a bitnami chart to install postgresql, and this chart can also
[enable the exporter](https://github.com/bitnami/charts/tree/master/bitnami/postgresql#metrics),
we only need to add this to [values.yaml](chart/values.yaml):

```yaml
postgresql:
  metrics:
    enabled: true
    serviceMonitor:
      enabled: true
      interval: 15s
```

The exporter exposes internal DB views, so available
metrics are essentially documented [here](https://www.postgresql.org/docs/9.2/monitoring-stats.html)

[Sample Grafana dashboard](https://grafana.com/grafana/dashboards/9628)
