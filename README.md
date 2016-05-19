[![Build Status](https://snap-ci.com/yaravind/kafka-connect-jenkins/branch/master/build_image)](https://snap-ci.com/yaravind/kafka-connect-jenkins/branch/master) [![Coverage Status](https://coveralls.io/repos/github/yaravind/kafka-connect-jenkins/badge.svg?branch=master)](https://coveralls.io/github/yaravind/kafka-connect-jenkins?branch=master) [![Codacy Badge](https://api.codacy.com/project/badge/grade/c6faadd0154740aeb202710fcdea3dfc)](https://www.codacy.com/app/yaravind/kafka-connect-jenkins/dashboard) [![HitCount](https://hitt.herokuapp.com/yaravind/kafka-connect-jenkins.svg)](https://github.com/yaravind/kafka-connect-jenkins)

# Kafka Connect Jenkins

Kafka Connect Connector for Jenkins Open Source Continuous Integration Tool.

## What is it?

To do

## What can I do with it?

In an organization, there can be one or more CI/CD servers (usually Jenkins) managing the day to day builds
and deployments of various components or services. We usually see each business unit (or vertical) managing
their own Jenkins instance. Any such organization can benefit from treating **build events** same like any other
transactional or master data.

These **build events** enable the following use-cases

- Management of Value Streams of all its products
   - Frequency of builds and deployments
        - Once per day
        - Many times a day
        - Once per week
        - Several per week
        - Once per month
        - Less often
   - Cycle times of deployments
   - Build times
   - Build statuses
- Portfolio demographics enabling consolidation opportunities thus reducing technical debt
    - Diversity of technology stack (versions, libraries etc.)
    - Diversity of source control systems
    - Diversity of testing libraries
    - Diversity if build systems (Ant, Maven etc.)
    - Diversity of usage
        - Build
        - Test
        - Deploy
        - SCA
        - Batch tasks
        - Operations
    - Diversity of build infrastructure deployments
        - Number of Masters, Build Agents and Executors
    - Which teams have higher turnover?
- Enterprise wide Release Health Scorecards
    - Trends on bugs
    - Trends on security checks
- Cost saving opportunities
    - Is Jenkins usage growing?
    - Proper reuse of existing infrastructure across business units an teams
    - Data driven forecasting of future infrastructural needs
- Other
    - Plug-ins being used in Jenkins
    - Etc.

## How to use it?

To do

### Dependencies

- Maven 3.x
- JDK 1.8 or newer
- [Spock Framework](https://spockframework.github.io/spock/docs/1.0/index.html)
- [Snap CI](https://snap-ci.com/yaravind/kafka-connect-jenkins/branch/master) for Continuous Integration
- [JaCoCo](https://github.com/jacoco/jacoco) and [Coveralls](https://coveralls.io/github/yaravind/kafka-connect-jenkins) for code coverage

### Configurations

To do

## Contribute

- Source code: https://github.com/yaravind/kafka-connect-jenkins
- Issue tracker: https://github.com/yaravind/kafka-connect-jenkins/issues

## License

The project is licensed under the Apache 2 license.