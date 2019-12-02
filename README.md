 ## AWS X-Ray SDK w/ OpenTelemetry API

The purpose of this repository is to serve as the central location for the development of the AWS X-Ray SDK for Java that is compatible with the [OpenTelemetry API](https://github.com/open-telemetry/opentelemetry-java/tree/master/api) currently under development. Our goal is to enable usage of both AWS X-Ray APIs and OpenTelemetry APIs within the same trace and enable usage of OpenTelemetry contrib integrations with AWS X-Ray. This enables open experimentation and sharing of ideas between these overlapping customer segments.

Currently, the SDK release is 0.1.0-SNAPSHOT and supports the 0.2.0-SNAPSHOT release of OpenTelemetry.

## Objective

Our goal is to address the following use cases on behalf of customers want to trace workloads:

* Enable workloads instrumented using OpenTelemetry APIs to send data to services such as AWS X-Ray and Amazon CloudWatch.
* Automate code instrumentation by making the use of agent frameworks such that developers no longer need to make manual code changes to instrument their applications.

## Roadmap

* Proof of concept showing the use of X-Ray and OpenTelemetry libraries for code instrumentation.
* Proof of concept showing the use of OpenTelemetry libraries with existing applications instrumented with the AWS X-Ray SDKs enabling support for a broader set of libraries
* Alpha version of OpenTelemetry exporters for AWS X-Ray and Amazon CloudWatch
* Alpha version of instrumentation agent for Java.

## Getting Started

[See It In Action - Sample App](https://github.com/aws-samples/aws-xray-sdk-with-opentelemetry-sample)

## Adding the SDK To Your Project

We are working to enable snapshot releases of the SDK [here](https://github.com/awslabs/aws-xray-sdk-with-opentelemetry/issues/8). 

## Building From Source

```
./gradlew build
./gradlew publishToMavenLocal //if desired
```

## Getting Help

Please use these community resources for getting help.

* If you think you may have found a bug or need assistance, please [open an issue](https://github.com/awslabs/aws-xray-sdk-with-opentelemetry/issues/new).
* Open a support ticket with [AWS Support](http://docs.aws.amazon.com/awssupport/latest/user/getting-started.html).
* Ask a question in the [AWS X-Ray Forum](https://forums.aws.amazon.com/forum.jspa?forumID=241&start=0).
* For contributing guidelines refer [CONTRIBUTING.md](https://github.com/awslabs/aws-xray-sdk-with-opentelemetry/blob/master/CONTRIBUTING.md).

## License

The AWS X-Ray SDK with OpenTelemetry is licensed under the Apache 2.0 License. See LICENSE and NOTICE.txt for more information.
