# Subatomic Config Server

A [Spring Cloud Config](https://cloud.spring.io/spring-cloud-config/)
implementation with added caching functionality.

> Please read the Spring Cloud Config [documentation](http://cloud.spring.io/spring-cloud-static/spring-cloud-config/1.4.2.RELEASE/single/spring-cloud-config.html)
for all available configuration options.

## SSH properties

Please see the [Git SSH configuration using properties](http://cloud.spring.io/spring-cloud-static/spring-cloud-config/1.4.2.RELEASE/single/spring-cloud-config.html#_git_backend)
section of the Spring Cloud Config documentation for the properties to use when using then SSH Git URI.

### Configuration in a Kubernetes/OpenShift environment

You can make use of a combination of Secrets, Config Maps and environment variables
to provide the SSH properties required to access the remote Git repository.

The [Spring Cloud Kubernetes](https://github.com/spring-cloud-incubator/spring-cloud-kubernetes)
dependency is included and allows using Secrets and Config Maps as a Spring `ProperySource`.
For example, you could keep the `hostKey` and `privateKey` values in a `subatomic-config-server`
Secret and any other configuration in a `subatomic-config-server` Config Map.
These values will be merged into a single `Environment` when the Config Server runs.

## Cache

The Subatomic Config Server has caching enabled by default.

Config Server will fetch the Git config repository every time a configuration is requested (see [these issues](https://github.com/spring-cloud/spring-cloud-config/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aopen%20cache)).
This can place unnecessary load on the remote Git repository, Bitbucket in this case.
Given that configurations should not change all that often, enabling caching should prevent unnecessary requests.

### Implementation

The [Caffeine](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-caching.html#boot-features-caching-provider-caffeine)
cache implementation is used.

> **NOTE:** The cache is _not_ enabled by default. To enable caching as discussed here you must enable the Caffeine cache provider.
`spring.cache.type=caffeine`

There is an AOP advice around the method that fetches the `Environment` from the remote Git repository.
The aspect then checks for previously cached values for the `application`, `profile` and `label`. If an existing
value is present it is returned, completely skipping the Git fetch, otherwise the fetched `Environment` is cached.

The cached values are retained indefinitely by default but can be tuned, or even disabled completely, as per normal [Spring Cache](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-caching.html#boot-features-caching-provider-caffeine)
properties.

### Clearing the cache

To clear the caches and force fetching the Git configuration from the remote repository, you can hit the `/refresh`
web endpoint:

```console
$ curl -X POST localhost:8888/refresh
```

This is the standard [`RefreshEndpoint`](https://github.com/spring-cloud/spring-cloud-commons/blob/master/spring-cloud-context/src/main/java/org/springframework/cloud/endpoint/RefreshEndpoint.java)
from Spring Cloud Commons wrapped with another AOP advice, that first clears the caches and then proceeds with the normal
environment refresh.

The idea would be that we have Web Hooks on our Git repositories, that, on merge, would call the `/refresh` endpoint to
force the next configuration request to get a fresh (up to date) copy.

### Cache controller

You can also list the currently cached `Environment`'s by invoking the Cache controller
at the `/caches` context path.

```console
$ curl localhost:8888/caches
...
``` 

## Docker

To build a Docker image with a runnable build of Gluon, you can use the S2I tool.

### S2I

[Source-to-Image (S2I)](https://github.com/openshift/source-to-image)
is a toolkit and workflow for building reproducible Docker images from source code.

First install S2I by following the [Installation](https://github.com/openshift/source-to-image#installation)
instructions.

Then in the root directory of Gluon run:

```console
$ s2i build . absasubatomic/s2i-jdk8-maven3-subatomic subatomic-config-server
...
```

Alternatively, using the GitHub codebase, in any directory run:

```console
$ s2i build https://github.com/absa-subatomic/subatomic-config-server.git absasubatomic/s2i-jdk8-maven3-subatomic subatomic-config-server
...
```

### Running the Docker image

Once the S2I build has completed, you can run Gluon with:

```console
$ docker run -p 8888 subatomic-config-server
...
```

you will have to additionally provide the relevant Spring Cloud Config properties to connect to a desired backend.
