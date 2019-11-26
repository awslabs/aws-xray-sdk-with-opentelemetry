 ## AWS X-Ray SDK w/ OpenTelemetry API

The purpose of this repository is to serve as the central location for the development of the AWS X-Ray SDK for Java that is compatible with the [OpenTelemetry API](https://github.com/open-telemetry/opentelemetry-java/tree/master/api) currently under development. Our goal is to enable usage of both AWS X-Ray APIs and OpenTelemetry APIs within the same trace and enable usage of OpenTelemetry contrib integrations with AWS X-Ray. This enables open experimentation and sharing of ideas between these overlapping customer segments.

Currently, this is an 0.1.0-alpha and supports the 0.2.0-SNAPSHOT release of OpenTelemetry.

## Objective

Our goal is to address the following use cases on behalf of customers want to trace workloads:

* Enable workloads instrumented using OpenTelemetry APIs to send data to services such as AWS X-Ray and Amazon CloudWatch.
* Automate code instrumentation by making the use of agent frameworks such that developers no longer need to make manual code changes to instrument their applications.

## Roadmap

* Proof of concept showing the use of X-Ray and OpenTelemetry libraries for code instrumentation.
* Proof of concept showing the use of OpenTelemetry libraries with existing applications instrumented with the AWS X-Ray SDKs enabling support for a broader set of libraries (e.g.: memcached, etc.)
* <Add other PoC items addressed in demo>
* Alpha version of OpenTelemetry exporters for AWS X-Ray and Amazon CloudWatch
* Alpha version of instrumentation agent for Java.

## Getting Started

Instructions to use/information on the sample repo location, and functionality examples.

[See It In Action - Sample App](https://github.com/aws-sample/aws-xray-sdk-with-opentelemetry-sample/edit/master/README.md)

## Adding the SDK To Your Project

1. Add snapshot builds and dependencies via your project’s dependency manager
2. Set the io.opentelemetry.trace.spi.TracerProvider Java property to com.amazonaws.xray.opentelemetry.tracing.TracingProvider

## Getting Started With Maven

```
<project>
  ...
  <repositories>
    <repository>
      <id>aws-snapshots</id>
      <url>https://aws.oss.sonatype.org/content/repositories/snapshots (https://aws.oss.sonatype.org/content/repositories/snapshots/)</url>
    </repository>
  </repositories>
  ...
  <dependencies>
    <dependency>
      <groupId>com.amazonaws</groupId>
      <artifactId>aws-xray-opentelemetry-sdk-java</artifactId>
      <version>0.1.0-SNAPSHOT</version>
    </dependency>
  </dependencies>
  ...
</project>
```

## Getting Started with Gradle

```
repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url "https://aws.oss.sonatype.org/content/repositories/snapshots"
    }
}

dependencies {
    runtimeOnly("com.amazonaws:aws-xray-opentelemetry-sdk-java:0.1.0-SNAPSHOT")
}
```

## Building From Source

1. Clone this repository
2. ./gradlew build
3. ./gradlew publishToMavenLocal (if desired)

## Getting Help

Please use these community resources for getting help.

* If you think you may have found a bug or need assistance, please open an issue (https://github.com/aws/aws-xray-sdk-go/issues/new).
* Open a support ticket with AWS Support (http://docs.aws.amazon.com/awssupport/latest/user/getting-started.html).
* Ask a question in the AWS X-Ray Forum (https://forums.aws.amazon.com/forum.jspa?forumID=241&start=0).
* For contributing guidelines refer CONTRIBUTING.md (https://github.com/aws/aws-xray-daemon/blob/master/CONTRIBUTING.md).

## Documentation

<TODO>
  
The developer guide (https://docs.aws.amazon.com/xray/latest/devguide/xray-sdk-java.html) provides guidance on using the AWS X-Ray Java Agent.
See awslabs/eb-java-scorekeep (https://github.com/awslabs/eb-java-scorekeep/tree/xray)/javaagent for a sample application.

## License

The AWS X-Ray SDK with OpenTelemetry is licensed under the MIT-0 License. See LICENSE and NOTICE.txt for more information.

