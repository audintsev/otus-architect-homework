# Homework for the lesson #6 (Kubernetes basics) of the [Otus "Software Architect" course](https://otus.ru/lessons/arhitektor-po/) 

## Deploying as Helm release

Deploying:
```
git clone https://github.com/audintsev/otus-architect-homework6.git
cd otus-architect-homework6
helm install homework6 ./chart
```

Undeploying:
```
helm uninstall homework6
```

Optionally, to delete also PVC: `kubectl get pvc`, followed by: `kubectl delete pvc data-homework6-postgresql-0`

## Deploying with manifests

Deploying:
```
helm repo add bitnami https://charts.bitnami.com/bitnami
git clone https://github.com/audintsev/otus-architect-homework6.git
cd otus-architect-homework6/manifests
helm install -f values.yaml postgres bitnami/postgresql
kubectl apply -f manifests
```

Undeploying:
```
kubectl delete -f manifests
helm delete postgres
```

Optionally, to delete also PVC: `kubectl get pvc`, followed by: `kubectl delete pvc data-postgres-postgresql-0`

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
* A lot of R2DBC foundation stuff has being moved from Spring Data R2DBC to Spring Framework 5.3. I decided to go ahead
and use the new stuff from Soring 5.3. But at the time of this writing only a pre-release version of Spring Boot (2.4-RC1)
uses Spring Framework 5.3. That's how I ended up using pre-release versions of Spring Boot.

### App: building and running

Building:

```
./gradlew bootJar
```

Running (assuming Postgres is available on localhost:5432):

```
java -jar otus-architect-homework6-0.0.1-SNAPSHOT.jar --spring.r2dbc.url=r2dbc:postgresql://localhost/test --spring.r2dbc.username=test --spring.r2dbc.password=test
```

### Image: building and pushing

```
./gradlew bootBuildImage --imageName=udintsev/otus-architect-homework6:0.2
docker push udintsev/otus-architect-homework6:0.2
```

### Useful links

* [R2DBC on Spring Framework](https://docs.spring.io/spring-framework/docs/5.3.0-RC2/reference/html/data-access.html#r2dbc)
* [WebFllux on Spring Framework](https://docs.spring.io/spring-framework/docs/5.3.0-RC2/reference/html/web-reactive.html#spring-webflux)
* [R2DBC support in Testcontainers](https://www.testcontainers.org/modules/databases/r2dbc/)
* [Spring REST Docs](https://docs.spring.io/spring-restdocs/docs/current/reference/html5/) 

## Gathering pod metrics for CPU and memory usage

https://prometheus.io/docs/guides/cadvisor/


[Prometheus stack](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack)
installs [stable/kube-state-metrics](https://github.com/helm/charts/tree/master/stable/kube-state-metrics) and
[stable/prometheus-node-exporter](https://github.com/prometheus-community/helm-charts/tree/main/charts/prometheus-node-exporter)

https://coreos.com/blog/monitoring-kubernetes-with-prometheus.html
https://itnext.io/k8s-monitor-pod-cpu-and-memory-usage-with-prometheus-28eec6d84729
https://github.com/kubernetes/kube-state-metrics/blob/master/docs/pod-metrics.md

## Gathering Postgres metrics

https://github.com/bitnami/charts/tree/master/bitnami/postgresql#metrics

Postgres exporter for Prometheus

https://github.com/prometheus-community/helm-charts/tree/main/charts/prometheus-postgres-exporter
https://github.com/wrouesnel/postgres_exporter
https://habr.com/ru/post/480902/

