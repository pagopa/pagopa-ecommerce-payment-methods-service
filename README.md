## pagopa-ecommerce-payment-methods-service

## What is this?

This is a PagoPA microservice that handles payment methods' lifecycle and workflow.

### Environment variables

These are all environment variables needed by the application:

| Variable name                      | Description                                                                                                                                                | type   | default |
|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| MONGO_USERNAME                     | Username used for connecting to MongoDB instance                                                                                                           | string |         |
| MONGO_PASSWORD                     | Password used for connecting to MongoDB instance                                                                                                           | string |         |
| MONGO_HOST                         | Host where MongoDB instance used to persise events and view resides                                                                                        | string |         |
| MONGO_PORT                         | Port where MongoDB instance used to persise events and view resides                                                                                        | string |         |
| MONGO_PORT                         | Port used for connecting to MongoDB instance                                                                                                               | string |         |
| MONGO_MIN_POOL_SIZE                | Min amount of connections to be retained into connection pool. See docs *                                                                                  | string |         |
| MONGO_MAX_POOL_SIZE                | Max amount of connections to be retained into connection pool.See docs *                                                                                   | string |         |
| MONGO_MAX_IDLE_TIMEOUT_MS          | Max timeout after which an idle connection is killed in milliseconds. See docs *                                                                           | string |         |
| MONGO_CONNECTION_TIMEOUT_MS        | Max time to wait for a connection to be opened. See docs *                                                                                                 | string |         |
| MONGO_SOCKET_TIMEOUT_MS            | Max time to wait for a command send or receive before timing out. See docs *                                                                               | string |         |
| MONGO_SERVER_SELECTION_TIMEOUT_MS  | Max time to wait for a server to be selected while performing a communication with Mongo in milliseconds. See docs *                                       | string |         |
| MONGO_WAITING_QUEUE_MS             | Max time a thread has to wait for a connection to be available in milliseconds. See docs *                                                                 | string |         |
| MONGO_HEARTBEAT_FREQUENCY_MS       | Hearth beat frequency in milliseconds. This is an hello command that is sent periodically on each active connection to perform an health check. See docs * | string |         |
| REDIS_HOST                         | Host where the redis instance used to persist idempotency keys can be found                                                                                | string |         |
| REDIS_PASSWORD                     | Password used for connecting to Redis instance                                                                                                             | string |         |
| REDIS_PORT                         | Port used for connecting to Redis instance                                                                                                                 | string |         |
| AFM_URI                            | Host used for call AFM for retrieve fields                                                                                                                 | string |         |
| AFM_URI_V2                         | Host used for call AFM for calculate fees for Cart                                                                                                         | string |         |
| AFM_KEY                            | AFM api key                                                                                                                                                | string |         |
| AFM_READ_TIMEOUT                   | Timeout for establishing connections towards AFM                                                                                                           | string |         |
| AFM_CONNECTION_TIMEOUT             | AFM connection timeout for http call                                                                                                                       | string |         |
| NPG_URI                            | Host used for call NPG for retrieve fields                                                                                                                 | string |         |
| NPG_READ_TIMEOUT                   | Timeout for requests towards NPG                                                                                                                           | string |         |
| NPG_CONNECTION_TIMEOUT             | Timeout for establishing connections towards NPG                                                                                                           | string |         |
| NPG_API_KEY                        | NPG api key                                                                                                                                                | string |         |
| NPG_SESSIONS_TTL                   | NPG TTL in second for npg session object                                                                                                                   | number |         |
| NPG_NOTIFICATION_JWT_VALIDITY_TIME | Validity time in second used for generate token jwt used into notification url                                                                             | number |         |
| WARMUP_PAYMENT_METHOD_ID           | Payment method ID used for warm-up call                                                                                                                    | string |         |
| SESSION_URL_BASEPATH               | Url used into npg order build request to enhance the merchantUrl field                                                                                     | string |         |
| SESSION_URL_OUTCOME_SUFFIX         | Suffix concatenated to the merchant url to enhance the resultUrl field in the order build to NPG                                                           | string |         |
| SESSION_URL_CANCEL_SUFFIX          | Suffix concatenated to the merchant url to enhance the cancelUrl field in the order build to NPG                                                           | string |         |
| SESSION_URL_NOTIFICATION_URL       | Url used into npg order build request to enhance the notificationUrl field                                                                                 | string |         |
| NPG_SO_KEEPALIVE                   | Whether tcp keepalive is enabled for payment gateway connections                                                                                           | string |         |
| NPG_TCP_KEEPIDLE                   | Configures the idle time after tcp starts sending keepalive probes, in seconds                                                                             | string |         |
| NPG_TCP_KEEPINTVL                  | Configures the time between individual keepalive probes, in seconds                                                                                        | string |         |
| NPG_TCP_KEEPCNT                    | Configures the maximum number of TCP keepalive probes                                                                                                      | string |         |
| JWT_ISSUER_URI                     | JWT Issuer URI                                                                                                                                             | string |         |
| JWT_ISSUER_READ_TIMEOUT            | Timeout for requests towards JWT Issuer                                                                                                                    | string |         |
| JWT_ISSUER_CONNECTION_TIMEOUT      | Timeout for establishing connections towards JWT Issuer                                                                                                    | string |         |
| JWT_ISSUER_API_KEY                 | Jwt issuer service API key                                                                                                                                 | string |         |
| SECURITY_API_KEY_PRIMARY           | Primary API Key used to secure payment-requests service's APIs                                                                                             | string |         |
| SECURITY_API_KEY_SECONDARY         | Secondary API Key used to secure payment-requests service's APIs                                                                                           | string |         |
| SECURITY_API_KEYS_SECURED_PATHS    | Comma-separated list of secured API paths                                                                                                                  | string |         |
| GITHUB_TOKEN                       | GitHub Personal Access Token with packages:read permission for accessing pagopa-ecommerce-commons from GitHub Packages                                     | string |         |
(*): for Mongo connection string options
see [docs](https://www.mongodb.com/docs/drivers/java/sync/v4.3/fundamentals/connection/connection-options/#connection-options)

## Run the application with `springboot-plugin`

Create your environment:

```sh
export $(grep -v '^#' .env.local | xargs)
```

Set up GitHub authentication for packages:

```sh
export GITHUB_TOKEN=your_github_token_with_packages_read_permission
```

Then from current project directory run :

```sh
mvn spring-boot:run
```

**Note:** The application now uses pagopa-ecommerce-commons library directly from GitHub Packages. Make sure your GitHub token has `packages:read` permission for the `pagopa/pagopa-ecommerce-commons` repository.

## Run locally with Docker

### Prerequisites
Set up GitHub authentication for packages:
```sh
export GITHUB_TOKEN=your_github_token_with_packages_read_permission
```

### Build Docker Image
```sh
docker build --secret id=GITHUB_TOKEN,env=GITHUB_TOKEN -t pagopa-ecommerce-payment-methods-service .
```

### Run Container
```sh
docker run -p 8080:8080 pagopa-ecommerce-payment-methods-service
```

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
