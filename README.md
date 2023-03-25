# Microsoft Kafka Bootcamp

Welcome to the Microsoft Kafka Bootcamp. We will be getting started with some basics of Kafka and its associated components.

## Prerequisites

- Confluent Cloud Account - Please [register](https://www.confluent.io/confluent-cloud/tryfree/) an account if you have not already done it.
- Docker and docker-compose installed on your machine
    - Docker Engine - [https://docs.docker.com/engine/install/](https://docs.docker.com/engine/install/)
    - Docker compose - [https://docs.docker.com/compose/install/other/](https://docs.docker.com/compose/install/other/)
- [Gradle](https://gradle.org/install/) installed
- [Java 11](https://openjdk.org/install/) installed and configured as the current Java version for the environment. Verify that `java -version` outputs version 11 and ensure that the `JAVA_HOME` environment variable is set to the Java installation directory containing `bin`.
- IDE of your choice - Visual Studio or IntelliJ

## Create a Kafka and Schema Registry Cluster

- Create a Basic Kafka cluster on your Confluent Cloud account.
- Create a Essentials Stream Governance package on the same environment as your Kafka cluster.
- You can obtain the Kafka cluster bootstrap server configuration from the Cluster settings section under `Cluster Overview` .
- Similarly, you can obtain the schema registry URL endpoint from the stream governance section.

## Confluent Cloud Programmatic Access

- We will need a Kafka API Key for the clients to connect to Kafka cluster. So, let’s create one.
- We will need a Kafka API Key for the clients to connect to Kafka cluster. So, let’s create one.
- Create a API key by navigating to the `API Keys` section under `Cluster Overview`
    - Scope of the API key will be global.

    **Note:** Global scopes are not recommended in production. Creating API key with granular permissions using ACL(s) is out of scope for this bootcamp.

## Install Kafka CLI

- Download the CLI archive,

    ```bash
    curl -O https://archive.apache.org/dist/kafka/3.3.1/kafka_2.13-3.3.1.tgz
    ```

- Extract the CLI,

    ```bash
    tar zxf kafka_2.13-3.3.1.tgz
    ```

- Add the CLI bin to environment PATH

    ```bash
    export PATH=$PATH:$PWD/kafka_2.13-3.3.1/bin
    ```


## CLI

### Topic

- We will need to configure the `client.properties` file for the Admin Client to connect to Kafka Cluster.

    ```
    sasl.mechanism=PLAIN
    security.protocol=SASL_SSL
    sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
       username="<Kafka-api-key>" \
       password="<Kafka-api-secret>";
    ```

    We will need the bootstrap server in addition to the above.

- List topics from the Kafka cluster

    ```bash
    kafka-topics.sh --bootstrap-server <kafka-server> --command-config client.properties --list
    ```

- Create a topic

    ```bash
    kafka-topics.sh --bootstrap-server <kafka-server> --command-config client.properties --create --topic test_topic
    ```

- Describe a topic

    ```bash
    kafka-topics.sh --bootstrap-server <kafka-server> --command-config client.properties --describe --topic test_topic
    ```

- Delete a topic

    ```bash
    kafka-topics.sh --bootstrap-server <kafka-server> --command-config client.properties --delete --topic test_topic
    ```


### Producer

- Create a `producer.properties` file with the connection details as well as producer related configurations. For more details on the available Producer configuration, visit [this](https://docs.confluent.io/platform/current/installation/configuration/producer-configs.html#ak-producer-configurations-for-cp) link

    ```
    sasl.mechanism=PLAIN
    security.protocol=SASL_SSL
    sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
       username="<Kafka-api-key>" \
       password="<Kafka-api-secret>";
    key.serializer=org.apache.kafka.common.serialization.StringSerializer
    value.serializer=org.apache.kafka.common.serialization.StringSerializer
    batch.size=16384
    linger.ms=0
    acks=all
    ```

- Produce to a topic from the console

    ```bash
    kafka-console-producer.sh --bootstrap-server <kafka-server> --producer.config producer.properties --topic test_topic
    ```


### Consumer

- Create a `consumer.properties` file with the connection details as well as consumer related configurations. For more details on the available Consumer configuration, visit [this](https://docs.confluent.io/platform/current/installation/configuration/consumer-configs.html#ak-consumer-configurations-for-cp) link

    ```
    sasl.mechanism=PLAIN
    security.protocol=SASL_SSL
    sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
       username="<Kafka-api-key>" \
       password="<Kafka-api-secret>";
    key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
    value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
    fetch.min.bytes=1
    auto.offset.reset=earliest
    ```

- Consume from a topic in console

    ```bash
    kafka-console-consumer.sh --bootstrap-server <kafka-server> --consumer.config consumer.properties --topic test_topic --group test_group
    ```

- The consumer group id of the consumer can be defined with the `--group` tag as seen above.

## Java

### Producer using Schema Registry

- Go to the schema registry example directory.

    ```bash
    cd java-client/
    ```

- A sample producer code is at `java-client/src/main/java/examples/ProducerAvroExample.java`
- Gradle build file with the required dependencies mentioned is stored as `build.gradle`
- You can test the code before preceding by compiling with:

    ```bash
    gradle build
    ```

    And you should see:

    ```bash
    BUILD SUCCESSFUL
    ```

- To build a JAR that we can run from the command line, first run:

    ```bash
    gradle shadowJar
    ```

- Create a `producer.properties` file at the Java folder with schema registry credentials,

    ```
    bootstrap.servers=<kafka-bootstrap-server>
    sasl.mechanism=PLAIN
    security.protocol=SASL_SSL
    sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
       username="<Kafka-api-key>" \
       password="<Kafka-api-secret>";
    key.serializer=org.apache.kafka.common.serialization.StringSerializer
    value.serializer=io.confluent.kafka.serializers.KafkaAvroSerializer
    batch.size=16384
    linger.ms=0
    acks=all
    schema.registry.url=<schema-registry-url>
    schema.registry.basic.auth.credentials.source=USER_INFO
    schema.registry.basic.auth.user.info=<schema-registry-api-key>:<schema-registry-api-secret>
    ```

- Run the following command to build and execute the producer application, which will produce some random data events to the `test_avro` topic.

    ```bash
    java -cp build/libs/kafka-clients-java-7.3.1.jar main.java.examples.ProducerAvroExample producer.properties test_avro
    ```


### Consumer using Schema Registry

- A sample consumer code is at `src/main/java/examples/ConsumerAvroExample.java`
- You can test the code before preceding by compiling with:

    ```bash
    gradle build
    ```

    And you should see:

    ```bash
    BUILD SUCCESSFUL
    ```

- To build a JAR that we can run from the command line, first run:

    ```bash
    gradle shadowJar
    ```

- Create a `consumer.properties` file at the Java folder

    ```
    bootstrap.servers=<kafka-bootstrap-server>
    sasl.mechanism=PLAIN
    security.protocol=SASL_SSL
    sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
       username="<Kafka-api-key>" \
       password="<Kafka-api-secret>";
    key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
    value.deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer
    fetch.min.bytes=1
    auto.offset.reset=earliest
    group.id=test_avro_consumer
    schema.registry.url=<schema-registry-url>
    schema.registry.basic.auth.credentials.source=USER_INFO
    schema.registry.basic.auth.user.info=<schema-registry-api-key>:<schema-registry-api-secret>
    ```

- From another terminal, run the following command to run the consumer application which will read the events from the `test_avro`topic and write the information to the terminal.

    ```bash
    java -cp build/libs/kafka-clients-java-7.3.1.jar main.java.examples.ConsumerAvroExample consumer.properties test_avro
    ```

- The consumer application will start and print any events it has not yet consumed and then wait for more events to arrive. Once you are done with the consumer, press `ctrl-c`
 to terminate the consumer application.

## KStreams

Run the kstreams application in `kstreams` directory.
    ```bash
    java -cp target/kstreams-1.0-SNAPSHOT-jar-with-dependencies.jar com.bootcamp.CustomerStreams producer.properties customers
    ```
Produce AVRO formatted data to `customers` topic first.

    ```bash
    java -cp build/libs/kafka-clients-java-7.3.1.jar main.java.examples.CustomersAvroProducer ../kstreams/producer.properties customers
    ```

## Kafka Connect

- We will deploy a standalone Kafka Connect cluster using the `cp-kafka-connect` image which is configured to connect to the Kafka cluster in Confluent Cloud.
- The Kafka Connect configurations should be given as environment variables. Please refer to this [link](https://docs.confluent.io/platform/current/installation/docker/config-reference.html#kconnect-long-configuration) on the format the configurations should follow.
- The default `cp-kafka-connect` image only contains a handful of connectors and does not contain popular connectors like JDBC, S3-sink etc.
- We need to bake a new image by installing the required connectors on top of the `cp-kafka-connect` image. For this, we will use a `Dockerfile` like below,

    ```docker
    # Stage 1 -- install connectors
    FROM confluentinc/cp-server-connect:7.3.1 AS install-connectors

    ENV CONNECT_PLUGIN_PATH: "/usr/share/java,/usr/share/confluent-hub-components"

    # Install SSE connector
    RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:10.6.0
    RUN confluent-hub install confluentinc/kafka-connect-azure-blob-storage-source:2.5.1
    RUN confluent-hub install confluentinc/kafka-connect-azure-blob-storage:1.6.15
    RUN confluent-hub install confluentinc/kafka-connect-azure-data-lake-gen2-storage:1.6.15
    RUN confluent-hub install confluentinc/kafka-connect-azure-functions:2.0.2
    ```

- The above `Dockerfile` will be called inside the `docker-compose.yml` while starting up the docker containers.
- Please refer to the `docker-compose.yml` file for reference.
- Please refer to the sample connector config file as instructed.
- Create a connector through the HTTP Post call,
    ```
    curl -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d '@<file-path>'
    ```
- Check the status of the connectors,
    ```
    curl -X GET http://localhost:8083/connectors?expand=status
    ```
- Delete a connector,
    ```
    curl -X DELETE http://localhost:8083/connectors/<connector-name>
    ```
- Stop the Kafka connect worker,
    ```
    docker-compose down -v
    ```

## KSQL

- We will deploy a standalone KSQL server using the `cp-ksqldb-server` image which is configured to connect to the Kafka cluster in Confluent Cloud.
- For configuration format, please visit this [link](https://docs.confluent.io/platform/current/installation/docker/config-reference.html#ksqldb-server-configuration)
- Please refer to the `docker-compose.yml` file for reference.
- More details on the KSQL Configurations to follow
- Connect to the ksql server using the KSQL Cli,
    ```
    # Connect the ksqlDB CLI to the server.

    docker-compose exec ksqldb-cli  ksql http://ksqldb-server:8088
    ```
- Stop the KSQL cluster,
    ```
    docker-compose down -v
    ```
