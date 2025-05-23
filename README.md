# Penalty Payment Web

Web application for handling payments of penalties. This application is written using
the [Spring Boot](http://projects.spring.io/spring-boot/) Java framework.

- Retrieves payable penalties from the Penalty Payment Service
- Displays payable penalties and redirects to the Payments Service to take payment.

### Requirements
In order to run this Web App locally you will need to install:

- [Java 21](https://www.oracle.com/java/technologies/downloads/?er=221886#java21)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)
- [Payments API](https://github.com/companieshouse/payments.api.ch.gov.uk)
- [Penalty Payment API](https://github.com/companieshouse/penalty-payment-api)

### Getting Started

1. [Configure your service](#configuration) if you want to override any of the defaults.
1. Run `make`
1. Run `./start.sh`


### Configuration

 Key                                                                               | Description
-----------------------------------------------------------------------------------|----------------------------------------------------------------------------------
 `PPS_PAY_WEB_PORT`                                                                | The port of the penalty-payment-web application
 `HUMAN_LOG`                                                                       | For human readable logs
 `CH_BANK_ACC_NAME`                                                                | Bacs payments - Account name (late filing penalty: A)
 `CH_BANK_SORT_CODE`                                                               | Bacs payments - Sort code (late filing penalty: A)
 `CH_BANK_ACC_NUM`                                                                 | Bacs payments - Account number (late filing penalty: A)
 `CH_BANK_IBAN`                                                                    | Overseas payments - IBAN (late filing penalty: A)
 `CH_BANK_SWIFT_CODE`                                                              | Overseas payments - SWIFT code (late filing penalty: A)
 `CH_SANCTIONS_BANK_ACC_NAME`                                                      | Bacs payments - Account name (sanction: P)
 `CH_SANCTIONS_BANK_SORT_CODE`                                                     | Bacs payments - Sort code (sanction: P)
 `CH_SANCTIONS_BANK_ACC_NUM`                                                       | Bacs payments - Account number (sanction: P)
 `CH_SANCTIONS_BANK_IBAN`                                                          | Overseas payments - IBAN (sanction: P)
 `CH_SANCTIONS_BANK_SWIFT_CODE`                                                    | Overseas payments - SWIFT code (sanction: P)
 `FEATURE_FLAG_PENALTY_REF_ENABLED_SANCTIONS_191224`                               | Feature flag to enable Penalty Payment for Sanctions
 `PENALTY_PAYMENT_MATOMO_START_NOW_BUTTON_GOAL_ID`                                 | Matomo Goal Id: PAY A PENALTY - Start Now Button
 `PENALTY_PAYMENT_MATOMO_PAY_ANOTHER_PENALTY_GOAL_ID`                              | Matomo Goal Id: PAY A PENALTY - Pay another penalty
 `PENALTY_PAYMENT_MATOMO_PENALTY_REF_STARTS_WITH_LFP_GOAL_ID`                      | Matomo Goal Id: PAY A PENALTY - Penalty ref starts with LFP A
 `PENALTY_PAYMENT_MATOMO_PENALTY_REF_STARTS_WITH_SANCTIONS_GOAL_ID`                | Matomo Goal Id: PAY A PENALTY - Penalty ref starts with Sanctions P
 `PENALTY_PAYMENT_MATOMO_PENALTY_DETAILS_CONTINUE_LFP_GOAL_ID`                     | Matomo Goal Id: PAY A PENALTY - Penalty details continue button A
 `PENALTY_PAYMENT_MATOMO_PENALTY_DETAILS_CONTINUE_SANCTIONS_GOAL_ID`               | Matomo Goal Id: PAY A PENALTY - Penalty details continue button P
 `PENALTY_PAYMENT_MATOMO_PENALTY_IN_DCA_STOP_SCREEN_GOAL_ID`                       | Matomo Goal Id: PAY A PENALTY - Penalty in DCA Stop Screen
 `PENALTY_PAYMENT_MATOMO_UNSCHEDULED_SERVICE_DOWN_STOP_SCREEN_GOAL_ID`             | Matomo Goal Id: PAY A PENALTY - Unscheduled Service Down Stop Screen
 `PENALTY_PAYMENT_MATOMO_SCHEDULED_SERVICE_DOWN_STOP_SCREEN_GOAL_ID`               | Matomo Goal Id: PAY A PENALTY - Scheduled Service Down Stop Screen
 `PENALTY_PAYMENT_MATOMO_ONLINE_PAYMENT_UNAVAILABLE_LFP_STOP_SCREEN_GOAL_ID`       | Matomo Goal Id: PAY A PENALTY - Online Payment Unavailable LFP Stop Screen
 `PENALTY_PAYMENT_MATOMO_ONLINE_PAYMENT_UNAVAILABLE_SANCTIONS_STOP_SCREEN_GOAL_ID` | Matomo Goal Id: PAY A PENALTY - Online Payment Unavailable Sanctions Stop Screen
 `GOV_UK_PAY_PENALTY_URL`                                                          | GOV.UK service banner URL: Pay a penalty to Companies House

### Web Pages

 Page                                        | Address
---------------------------------------------|--------------------------------
 Start page for Penalty Payment Service      | `/pay-penalty`
 What does the penalty reference start with? | `/pay-penalty/ref-starts-with`

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.

 Application specific attributes | Value                                                                                                                                                                                                                                                      | Description                                         
:--------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------
 **ECS Cluster**                 | company-requests                                                                                                                                                                                                                                           | ECS cluster (stack) the service belongs to          
 **Load balancer**               | {env}-chs-chgovuk                                                                                                                                                                                                                                          | The load balancer that sits in front of the service 
 **Concourse pipeline**          | [Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/penalty-payment-web) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/penalty-payment-web) | Concourse pipeline link in shared services          

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
