<p align="center">
   <img src="12Factor-App-W.png" width="672" height="223" />
</p>

[![Gradle](https://github.com/manquius/12factor/workflows/Java%20CI%20with%20Gradle/badge.svg)]() [![Helm](https://github.com/manquius/12factor/workflows/Lint%20and%20Test%20Charts/badge.svg)]() [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# 12factor 

## Table of Contents
- [Introduction](#introduction)
- [Pre-requisites](#pre-requisites)
- [Recommended utilities](#recommended-utilities)
- [Installation](#installation)
- [Uninstallation](#uninstallation)
- [Usage](#usage)
- [Start your own 12factor project](#start-your-own-12factor-project)
- [Author](#author)
- [Collaboators](#collaboators)
- [License](#license)

## Introduction
12factor demo is a simplified example project to demonstrate the advantages of [twelve-factor](https://12factor.net/) methodology on cloud based applications design.

It consist in two frontend [RESTful](https://restfulapi.net/) services to produce and consume to a [Kafka](https://kafka.apache.org/) backend. It also includes a Redis service as a circuit breaker for High Availability.

It is build to be installed on a [Kubernetes](https://kubernetes.io/) environment using [Helm](https://helm.sh/).

## Pre-requisites
* [Docker](https://www.docker.com/)
* [Kubernetes](https://kubernetes.io/) or [kubectl](https://kubernetes.io/docs/reference/kubectl/overview/) (for remote clusters)
* [Helm](https://helm.sh/)

### Recommended utilities
#### Docker-compose
[docker-compose](https://docs.docker.com/compose/) is a helper utility that simplifies the docker image builds.

It can also help to test integration locally. This example provides a docker-compose.yaml that can be use to verify [twelve-factor](https://12factor.net/) methodology can be easily adapter to many environments and deployments.

However we recommend local Kubernetes implementation like:

* [Kind](https://kubernetes.io/docs/setup/learning-environment/kind/)
* [Minikube](https://kubernetes.io/docs/setup/learning-environment/minikube/)
* [Microk8s](https://microk8s.io/)
* [Docker-desktop kubernates](https://docs.docker.com/docker-for-windows/kubernetes/)

A local Kubernetes implementation will help you to deploy and execute locally in the same way it will execute on production environments.
#### Metrics-server
[metrics-server](https://github.com/helm/charts/tree/master/stable/metrics-server) is necessary to implement horizontal pod autoscaler.
```bash
helm install metrics-server --namespace kube-system stable/metrics-server
```
#### Kubernetes Dashboard
The [kubernetes dashboard](https://github.com/kubernetes/dashboard) will help you to manage your cluster in a local environment.
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.1/aio/deploy/recommended.yaml
```
In order to access Kuberntes dashboard you must execute
```bash
kubectl proxy
```
And you will be able to access using your browser at:

http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/

The Kubernetes Dashboard will ask for a token to access. You can get it executing:
```bash
kubectl -n kubernetes-dashboard describe secret $(kubectl -n kubernetes-dashboard get secret | grep admin-user | awk '{print $1}')
```

## Installation
Before the first time you install the chart:
```bash
cd 12factor
helm dependency update
cd ..
```
And then each time you want to install
```bash
helm install twelvefactor ./12factor
```

## Uninstallation
```bash
helm uninstall twelvefactor
```
## Usage
This usage guidelines are thoght to be used on a local kubernetes environment. It may change if you deployed on diferent environments.

First, let configure the Kubernetes proxy:
```bash
kubectl proxy
```
Now you can call the different endpoints using [cURL](https://en.wikipedia.org/wiki/CURL), or your favourite http client.
### Producing
```bash
curl http://localhost:30001/topic/mytopic -d 'mymesage'
```

### Consuming
```bash
curl http://localhost:30002/topic/mytopic
```

### Health Check
```bash
curl http://localhost:30001/health
curl http://localhost:30002/health
```
### High Availability
In order to test a system failure, Producer and Consumer services have an API that will make Health fail.
```bash
curl http://localhost:30001/health/hurt
curl http://localhost:30002/health/hurt
```
The instances that received those calls will be shutted down and new instances will be created to replace them.

### Autoscaling
Producer and Consumer services can be configured to autoscale if high performance is required.
To acheve this you must execute:
```bash
kubectl autoscale deployment twelvefactor-12factor-producer --cpu-percent=50 --min=2 --max=10
kubectl autoscale deployment twelvefactor-12factor-consumer --cpu-percent=50 --min=2 --max=10
```

Then you can test autoscaling with any Load Test tool. For a simple test you could try:
```bash
while true; do curl http://localhost:30001/topic/mytopic -d 'mymesage'; done
while true; do curl http://localhost:30002/topic/mytopic; done
```

## Start your own 12factor project
1. Indetify your [use cases](https://en.wikipedia.org/wiki/Use_case)
2. Define your [interface](https://en.wikipedia.org/wiki/Behavior-driven_development)
    * Validate usability.
    * Validate accessibility.
3. Define your [architecture](https://www.cloudcomputingpatterns.org/)
4. Create your project
    * Execute [helm create](https://helm.sh/docs/helm/helm_create/)
5. Create your components
    * Write your code in __any__ language :sunglasses: (This example was created using [Micronaut](https://micronaut.io/))
    * Create your [Dockerfile](https://docs.docker.com/engine/reference/builder/)
    * (Optional) Create your [docker-compose.yaml](https://docs.docker.com/compose/compose-file/)
6. Select your [cloud service provider](https://en.wikipedia.org/wiki/Category:Cloud_computing_providers)
7. Choose your backend services (:confused: Yes, in this step)
8. Deploy
    * Execute [kubectl config use-context](https://kubernetes.io/docs/tasks/access-application-cluster/configure-access-multiple-clusters/)
    * Execute [helm install](https://helm.sh/docs/helm/helm_install/)
9. Perform integration tests
    * Execute the tests defined in step 2.
    * Execute performance tests.
    * Expose a beta to a limited public.
10. Publish

## Author
Fernando Ezequiel Mancuso (:bowtie:Manquius)
* https://github.com/manquius
* manquius@gmail.com

## Collaboators
* Guido Martin Cremona
    * https://github.com/gmcremona

## License
[MIT License](https://opensource.org/licenses/MIT)

Copyright (c) 2020 Fernando Ezequiel Mancuso

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


