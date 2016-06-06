[![Build Status](https://snap-ci.com/yaravind/kafka-connect-jenkins/branch/master/build_image)](https://snap-ci.com/yaravind/kafka-connect-jenkins/branch/master) [![Coverage Status](https://coveralls.io/repos/github/yaravind/kafka-connect-jenkins/badge.svg?branch=master)](https://coveralls.io/github/yaravind/kafka-connect-jenkins?branch=master) [![Codacy Badge](https://api.codacy.com/project/badge/grade/c6faadd0154740aeb202710fcdea3dfc)](https://www.codacy.com/app/yaravind/kafka-connect-jenkins/dashboard) [![HitCount](https://hitt.herokuapp.com/yaravind/kafka-connect-jenkins.svg)](https://github.com/yaravind/kafka-connect-jenkins)

# Kafka Connect Jenkins

Kafka Connect Connector for Jenkins Open Source Continuous Integration Tool.

## What is it?

kafka-connect-jenkins is a [Kafka Connector](http://kafka.apache.org/0100/documentation.html#connect) for loading data from [Jenkins](https://jenkins.io/) Open Source [Continuous Integration](https://en.wikipedia.org/wiki/Continuous_integration) Tool.

### Details

    +--------+   0..*   +-------+  0..*   +-------+   1      +--------------+
    | Server +--------> |  Job  +-------> | Build +--------> | BuildDetails |
    +--------+          +-------+         +-------+          +--------------+

### Example

We will use Apache Jenkins REST API to demonstrate an example.

| Resource  | Details |
|-----------|---------|
| Get list of all Jobs: https://builds.apache.org/api/json <br/><br/> GET **lastBuild** of `Abdera-trunk` Job: https://builds.apache.org/job/Abdera-trunk/api/json <br/><br/> GET BuildDetails for build `2546` of job `Abdera-trunk` https://builds.apache.org/job/Abdera-trunk/2546/api/json | ![](https://github.com/yaravind/kafka-connect-jenkins/blob/master/src/site/resources/images/jenkins-resource-relationships.png) |


## What can I do with it?

In an organization, there can be one or more CI/CD servers (usually Jenkins) managing the day to day builds
and deployments of various components or services. We usually see each business unit (or vertical) managing
their own Jenkins instance. Any such organization can benefit from treating **build events** same like any other
transactional or master data. [Here are some use-cases](https://github.com/yaravind/kafka-connect-jenkins/wiki/Use-cases) that these **build events** can enable.

## How to use it?

### Standlone mode

While testing, you might want to run the connector in standalone mode. Follow these steps

1. Download Confluent platform 3.0.0 ZIP file from [here](http://www.confluent.io/download)
2. Unzip it `unzip confluent-3.0.0-2.11.zip`
3. Move to Confluent home directory `cd confluent-3.0.0`
4. Start Zookeeper `./bin/zookeeper-server-start etc/kafka/zookeeper.properties`
5. Start Kafka Broker `./bin/kafka-server-start ./etc/kafka/server.properties`
6. Clone https://github.com/yaravind/kafka-connect-jenkins.git
7. Run `mvn clean install`
8. Update the `jenkins.base.url` property in `connect-jenkins-source.properties` file with your jenkins base url
9. Add Jenkins connector to classpath and start the connector in standalone mode `CLASSPATH=./target/kafka-connect-jenkins-0.5.0-SNAPSHOT-package/share/java/kafka-connect-jenkins/* /Users/p0c/tools/confluent-3.0.0/bin/connect-standalone connect-standalone.properties connect-jenkins-source.properties`
   
> If you need to proxy to connect to Jenkins then append these to the above command `-Dhttp.proxyHost=... -Dhttp.proxyPort=... -Dhttps.proxyHost=... -Dhttps.proxyPort=...`

### Dependencies

- Maven 3.x
- JDK 1.8 or newer
- [Spock Framework](https://spockframework.github.io/spock/docs/1.0/index.html)
- [Snap CI](https://snap-ci.com/yaravind/kafka-connect-jenkins/branch/master) for Continuous Integration
- [JaCoCo](https://github.com/jacoco/jacoco) and [Coveralls](https://coveralls.io/github/yaravind/kafka-connect-jenkins) for code coverage
- [Codacy](https://www.codacy.com/app/yaravind/kafka-connect-jenkins/dashboard) to measure code quality

### Configurations

| Property | Description | Mandatory? | Default value | 
|----------|-------------|------------|---------------|
|connector.class|Class implementing source connector for Jenkins.|Yes|org.aravind.oss.kafka.connect.jenkins.JenkinsSourceConnector|
|tasks.max| |Yes|1|
|jenkins.base.url|The URL where jenkins server is running|Yes|None|
|jenkins.username|If your Jenkins is secured, you can provide the username with this property|No|None|
|jenkins.password.or.api.token|If your Jenkins is secured, you can provide the password or api token with this property|No|None|
|topic|Name of the topic where the Build status records are written to. **Make sure you explicitly create this topic using tools provided by Kafka. Do not rely on the default topic creation functionality in PRODUCTION.**|Yes|jenkins.connector.topic|

## Contribute

- Source code: https://github.com/yaravind/kafka-connect-jenkins
- Issue tracker: https://github.com/yaravind/kafka-connect-jenkins/issues

## License

The project is licensed under the Apache 2 license.