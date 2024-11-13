# Companies House Penalties Payment Web Service
The Companies House Web Service for handling payments of penalties. This application is written using the [Spring Boot](http://projects.spring.io/spring-boot/) Java framework.

- Retrieves Payable penalties from the Penalty-Payment-API service
- Displays payable penalties and redirects to the Payments Service to take payment.

### Requirements
In order to run this Web App locally you will need to install:

- [Java 21](https://www.oracle.com/java/technologies/downloads/?er=221886#java21)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)
- [Payments API](https://github.com/companieshouse/payments.api.ch.gov.uk)
- [PPS API](https://github.com/companieshouse/pps-pay-api)

### Getting Started

1. [Configure your service](#configuration) if you want to override any of the defaults.
1. Run `make`
1. Run `./start.sh`


### Configuration

Key                | Description
-------------------|------------------------------------
`Penalty_Payment_WEB_PORT` |The port of the Penalty-Payment-WEB service
`HUMAN_LOG`        |For human readable logs
`CH_BANK_ACC_NUM`  |Companies House Bank account number (for penalty start page)
`CH_BANK_SORT_CODE`|Companies House Bank sort code (for penalty start page)
`CH_BANK_ACC_NAME` |Companies House Bank Bank account name (for penalty start page)


### Web Pages

Page                                     | Address
-----------------------------------------|-----------------------------
Start page for Penalty Service           | `/late-filing-penalty`
Tell us your penalty details             | `/late-filing-penalty/enter-details`

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.


Application specific attributes | Value                                                                                                                                                                                                                                      | Description
:---------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-----------
**ECS Cluster**        | company-requests                                                                                                                                                                                                                           | ECS cluster (stack) the service belongs to
**Load balancer**      | {env}-chs-chgovuk                                                                                                                                                                                                                          | The load balancer that sits in front of the service
**Concourse pipeline**     | [Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/penalty-payment-web) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/penalty-payment-web) | Concourse pipeline link in shared services


### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)
