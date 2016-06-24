# Kafka Connect Jenkins

[![Build Status](https://snap-ci.com/yaravind/kafka-connect-jenkins/branch/master/build_image)](https://snap-ci.com/yaravind/kafka-connect-jenkins/branch/master) [![Coverage Status](https://coveralls.io/repos/github/yaravind/kafka-connect-jenkins/badge.svg?branch=master)](https://coveralls.io/github/yaravind/kafka-connect-jenkins?branch=master) [![Codacy Badge](https://api.codacy.com/project/badge/grade/c6faadd0154740aeb202710fcdea3dfc)](https://www.codacy.com/app/yaravind/kafka-connect-jenkins/dashboard) [![HitCount](https://hitt.herokuapp.com/yaravind/kafka-connect-jenkins.svg)](https://github.com/yaravind/kafka-connect-jenkins)[![Stories in Ready](https://badge.waffle.io/yaravind/kafka-connect-jenkins.png?label=ready&title=Ready)](https://waffle.io/yaravind/kafka-connect-jenkins) [![Join the chat at https://gitter.im/yaravind/kafka-connect-jenkins](https://badges.gitter.im/yaravind/kafka-connect-jenkins.svg)](https://gitter.im/yaravind/kafka-connect-jenkins?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Kafka Connect Connector for [Jenkins](https://jenkins.io/) Open Source Continuous Integration Tool. **The connector is currently in ALPHA release. Looking for early adopters.**

## What is it?

kafka-connect-jenkins is a [Kafka Connector](http://kafka.apache.org/0100/documentation.html#connect) for loading data from [Jenkins](https://jenkins.io/) Open Source [Continuous Integration](https://en.wikipedia.org/wiki/Continuous_integration) Tool.

### Details

    +--------+   0..*   +-------+  0..*   +-------+   1      +--------------+
    | Server +--------> |  Job  +-------> | Build +--------> | BuildDetails |
    +--------+          +-------+         +-------+          +--------------+

The following comparison might give you more insight.

| Example | Input Partitions (Logical) | Records | Offsets/Position |
| ------- | -------------------------- | ------- | ---------------- |
| Set of files | each File | each Line | line Number within a file  |
| Database instance | each Table | each Row | primary id + timestamp |
| Jenkins Server | each Job | BuildDetails of each Build (**lastBuild**) | build number of **lastBuild**|

### Example

We will use Apache Jenkins REST API to demonstrate an example.

| Resource  | Details |
|-----------|---------|
| Get list of all Jobs:<br/> https://builds.apache.org/api/json <br/><br/> GET **lastBuild** of `Abdera-trunk` Job:<br/> https://builds.apache.org/job/Abdera-trunk/api/json <br/><br/> GET BuildDetails for build `2546` of job `Abdera-trunk`:<br/> https://builds.apache.org/job/Abdera-trunk/2546/api/json | ![](https://github.com/yaravind/kafka-connect-jenkins/blob/master/src/site/resources/images/jenkins-resource-relationships.png) |

**What gets published to the topic?**

The BuildDetails is treated as an event and is persisted to the topic in JSON format. Below is the content of a sample event

```
{
    "actions": [
        {
            "causes": [
                {
                    "shortDescription": "Started by an SCM change"
                }
            ]
        },
        {},
        {},
        {},
        {},
        {},
        {},
        {
            "failCount": 0,
            "skipCount": 1,
            "totalCount": 491,
            "urlName": "testReport"
        },
        {},
        {}
    ],
    "artifacts": [],
    "building": false,
    "description": null,
    "displayName": "#2546",
    "duration": 480582,
    "estimatedDuration": 457794,
    "executor": null,
    "fullDisplayName": "Abdera-trunk #2546",
    "id": "2015-08-25_22-08-43",
    "keepLog": false,
    "number": 2546,
    "queueId": -1,
    "result": "SUCCESS",
    "timestamp": 1440540523000,
    "url": "https://builds.apache.org/job/Abdera-trunk/2546/",
    "builtOn": "jenkins-ubuntu-1404-4gb-c51",
    "changeSet": {
        "items": [
            {
                "affectedPaths": [
                    "client/src/test/java/org/apache/abdera/test/client/util/MultipartRelatedRequestEntityTest.java"
                ],
                "author": {
                    "absoluteUrl": "https://builds.apache.org/user/veithen",
                    "fullName": "veithen"
                },
                "commitId": "1697770",
                "timestamp": 1440537458260,
                "date": "2015-08-25T21:17:38.260515Z",
                "msg": "Use factory to create FOMEntry instance.",
                "paths": [
                    {
                        "editType": "edit",
                        "file": "/abdera/java/trunk/client/src/test/java/org/apache/abdera/test/client/util/MultipartRelatedRequestEntityTest.java"
                    }
                ],
                "revision": 1697770,
                "user": "veithen"
            }
        ],
        "kind": "svn",
        "revisions": [
            {
                "module": "https://svn.apache.org/repos/asf/abdera/java/trunk",
                "revision": 1697770
            }
        ]
    },
    "culprits": [
        {
            "absoluteUrl": "https://builds.apache.org/user/veithen",
            "fullName": "veithen"
        }
    ],
    "mavenArtifacts": {},
    "mavenVersionUsed": "3.0.4"
}
```

## What can I do with it?

In an organization, there can be one or more CI/CD servers (usually Jenkins) managing the day to day builds
and deployments of various components or services. We usually see each business unit (or vertical) managing
their own Jenkins instance. Any such organization can benefit from treating **build events** same like any other
transactional or master data. [Here are some use-cases](https://github.com/yaravind/kafka-connect-jenkins/wiki/Use-cases) that these **build events** can enable.

## Configurations

| Property | Description | Required? | Default value | 
|----------|-------------|------------|---------------|
|`connector.class`|Class implementing source connector for Jenkins.|Yes|org.aravind.oss.kafka.connect.<br/>jenkins.JenkinsSourceConnector|
|`tasks.max`| |Yes|1|
|`jenkins.base.url`|The URL where jenkins server is running|Yes|None|
|`jenkins.pollIntervalInMillis`|Frequency (in milliseconds) to poll for new build events in Jenkins|Yes|1 minute|
|`jenkins.username`|If your Jenkins is secured, you can provide the username with this property|No|None|
|`jenkins.password.or.api.token`|If your Jenkins is secured, you can provide the password or api token with this property|No|None|
|`jenkins.connection.timeoutInMillis`|Connection timeout in milliseconds. This denotes the time elapsed before the connection established or Server responded to connection request.|Yes|500|
|`jenkins.read.timeoutInMillis`|Response read timeout in milliseconds. After establishing the connection, the client socket waits for response after sending the request. This is the elapsed time since the client has sent request to the server before server responds.|Yes|3000|
|`jenkins.jobs.resource.path`|Relative path to REST API|Yes|`/api/json`|
|`topic`|Name of the topic where the Build status records are written to. **Make sure you explicitly create this topic using tools provided by Kafka. Do not rely on the default topic creation functionality in PRODUCTION.**|Yes|jenkins.connector.topic|

## How to use it?

### Standalone mode

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

### Logging

You can enable the logging for the connector by adding `log4j.logger.org.aravind.oss=DEBUG` (TRACE) to `connect-log4j.properties`. Following table can help you with finer level of logging.

| Intent | Configuration (`DEBUG` or `TRACE`) |
|--------|------------------------------------|
| Enable logging for Jenkins **Source Connector** only  | `log4j.logger.org.aravind.oss.kafka.connect.jenkins.JenkinsSourceConnector=DEBUG`<br/>`log4j.logger.org.aravind.oss.kafka.connect.lib.TaskConfigBuilder=DEBUG` |
| Enable logging for Jenkins **Source Task** only | `log4j.logger.org.aravind.oss.kafka.connect.jenkins.JenkinsSourceTask=DEBUG`<br/>`log4j.logger.org.aravind.oss.kafka.connect.jenkins.ReadYourWritesOffsetStorageAdapter=ERROR`<br/>`log4j.logger.org.aravind.oss.kafka.connect.lib.Partitions=DEBUG`|
| Enable logging for communication with Jenkins API only | `log4j.logger.org.aravind.oss.jenkins.JenkinsClient=TRACE` |

### Monitoring

> Replace `localhost` with your server and `8083` with `rest.port` configuration value.

| REST API | Details |
|----------|---------|
| `http://localhost:8083/` | Kafka Connector |
| `http://localhost:8083/connectors/` | All deployed connectors |
| `http://localhost:8083/connectors/kafka-jenkins-source-connector/` | JenkinsSourceConnector |
| `http://localhost:8083/connectors/kafka-jenkins-source-connector/config` | JenkinsSourceConnector config |
| `http://localhost:8083/connectors/kafka-jenkins-source-connector/tasks` | JenkinsSourceConnector tasks |

## Limitations

- Saves only the most recent build (**lastBuild**) know after configured `jenkins.pollIntervalInMillis`. i.e. if a Job has been built multiple times within the poll intervals, it isn't accounted for.
- Requires JDK 8 to run the connector. Making JDK 7 compatible version isn't a big deal. Raise an issue if you need one.

## Dependencies

- Maven 3.x
- JDK 1.8 or newer
- [Spock Framework](https://spockframework.github.io/spock/docs/1.0/index.html)
- [Snap CI](https://snap-ci.com/yaravind/kafka-connect-jenkins/branch/master) for Continuous Integration
- [JaCoCo](https://github.com/jacoco/jacoco) and [Coveralls](https://coveralls.io/github/yaravind/kafka-connect-jenkins) for code coverage
- [Codacy](https://www.codacy.com/app/yaravind/kafka-connect-jenkins/dashboard) to measure code quality

## Contribute

- Source code: https://github.com/yaravind/kafka-connect-jenkins
- Issue tracker: https://github.com/yaravind/kafka-connect-jenkins/issues

## License

The project is licensed under the Apache 2 license.
