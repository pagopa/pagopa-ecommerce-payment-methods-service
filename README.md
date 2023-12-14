## pagopa-ecommerce-payment-methods-service

## What is this?

This is a PagoPA microservice that handles payment methods' lifecycle and workflow.


### Environment variables

These are all environment variables needed by the application:

| Variable name                      | Description                                                                                      | type   | default |
|------------------------------------|--------------------------------------------------------------------------------------------------|--------|---------|
| MONGO_USERNAME                     | Username used for connecting to MongoDB instance                                                 | string |         |
| MONGO_PASSWORD                     | Password used for connecting to MongoDB instance                                                 | string |         |
| MONGO_HOST                         | Host where MongoDB instance used to persise events and view resides                              | string |         |
| MONGO_PORT                         | Port where MongoDB instance used to persise events and view resides                              | string |         |
| REDIS_HOST                         | Host where the redis instance used to persist idempotency keys can be found                      | string |         |
| REDIS_PASSWORD                     | Password used for connecting to Redis instance                                                   | string |         |
| REDIS_PORT                         | Port used for connecting to Redis instance                                                       | string |         |
| AFM_URI                            | Host used for call AFM for retrieve fields                                                       | string |         |
| AFM_KEY                            | AFM api key                                                                                      | string |         |
| AFM_READ_TIMEOUT                   | Timeout for establishing connections towards AFM                                                 | string |         |
| AFM_CONNECTION_TIMEOUT             | AFM connection timeout for http call                                                             | string |         |
| NPG_URI                            | Host used for call NPG for retrieve fields                                                       | string |         |
| NPG_READ_TIMEOUT                   | Timeout for requests towards NPG                                                                 | string |         |
| NPG_CONNECTION_TIMEOUT             | Timeout for establishing connections towards NPG                                                 | string |         |
| NPG_API_KEY                        | NPG api key                                                                                      | string |         |
| NPG_SESSIONS_TTL                   | NPG TTL in second for npg session object                                                         | number |         |
| NPG_NOTIFICATION_JWT_VALIDITY_TIME | Validity time in second used for generate token jwt used into notification url                   | number |         |
| NPG_NOTIFICATION_JWT_SECRET        | Secret for generate jwt used into notification url                                               | number |         |
| SESSION_URL_BASEPATH               | Url used into npg order build request to enhance the merchantUrl field                           | string |         |
| SESSION_URL_OUTCOME_SUFFIX         | Suffix concatenated to the merchant url to enhance the resultUrl field in the order build to NPG | string |         |
| SESSION_URL_CANCEL_SUFFIX          | Suffix concatenated to the merchant url to enhance the cancelUrl field in the order build to NPG | string |         |
| SESSION_URL_NOTIFICATION_URL       | Url used into npg order build request to enhance the notificationUrl field                       | string |         |

## Run the application with `springboot-plugin`

Create your environment:

```sh
export $(grep -v '^#' .env.local | xargs)
```

Then from current project directory run :

```sh
mvn validate # --> used to perform ecommerce-commons library checkout from git repo and install throught maven plugin
mvn spring-boot:run
```

For testing purpose the commons reference can be change from a specific release to a branch by changing the following
configurations tags:

FROM:

```sh
<scmVersionType>tag</scmVersionType>
<scmVersion>${pagopa-ecommerce-commons.version}</scmVersion>
```

TO:

```sh
<scmVersionType>branch</scmVersionType>
<scmVersion>name-of-a-specific-branch-to-link</scmVersion>
```

updating also the commons library version to the one of the specific branch

## Code formatting

Code formatting checks are automatically performed during build phase.
If the code is not well formatted an error is raised blocking the maven build.

Helpful commands:

```sh
mvn spotless:check # --> used to perform format checks
mvn spotless:apply # --> used to format all misformatted files
```
## CI

Repo has Github workflow and actions that trigger Azure devops deploy pipeline once a PR is merged on main branch.

In order to properly set version bump parameters for call Azure devops deploy pipelines will be check for the following
tags presence during PR analysis:

| Tag                | Semantic versioning scope | Meaning                                                           |
|--------------------|---------------------------|-------------------------------------------------------------------|
| patch              | Application version       | Patch-bump application version into pom.xml and Chart app version |
| minor              | Application version       | Minor-bump application version into pom.xml and Chart app version |
| major              | Application version       | Major-bump application version into pom.xml and Chart app version |
| ignore-for-release | Application version       | Ignore application version bump                                   |
| chart-patch        | Chart version             | Patch-bump Chart version                                          |
| chart-minor        | Chart version             | Minor-bump Chart version                                          |
| chart-major        | Chart version             | Major-bump Chart version                                          |
| skip-release       | Any                       | The release will be skipped altogether                            |

For the check to be successfully passed only one of the `Application version` labels and only ones of
the `Chart version` labels must be contemporary present for a given PR or the `skip-release` for skipping release step
