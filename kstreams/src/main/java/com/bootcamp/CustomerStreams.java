/*
 * Copyright 2020 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package com.bootcamp;

 import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
 import org.apache.kafka.clients.consumer.ConsumerConfig;
 import org.apache.kafka.common.serialization.Serde;
 import org.apache.kafka.common.serialization.Serdes;
 import org.apache.kafka.streams.KafkaStreams;
 import org.apache.kafka.streams.KeyValue;
 import org.apache.kafka.streams.StreamsBuilder;
 import org.apache.kafka.streams.StreamsConfig;
 import org.apache.kafka.streams.kstream.Consumed;
 import org.apache.kafka.streams.kstream.Grouped;
 import org.apache.kafka.streams.kstream.KStream;
 import org.apache.kafka.streams.kstream.Printed;
 import schema.Customer;
 import common.SerdeGenerator;
 
 import java.util.Map;
 import java.util.Properties;
 
 import static com.bootcamp.Util.loadConfig;
 
 public class CustomerStreams {
 
     public static void main(String[] args) throws Exception {
 
        if (args.length != 2) {
            System.out.println("Please provide command line arguments: configPath topic");
            System.exit(1);
        }
    
        final String topic = args[1];
    
        // Load properties from a local configuration file
        // Create the configuration file (e.g. at '$HOME/.confluent/java.config') with configuration parameters
        // to connect to your Kafka cluster, which can be on your local host, Confluent Cloud, or any other cluster.
        // Follow these instructions to create this file: https://docs.confluent.io/platform/current/tutorials/examples/clients/docs/java.html
        final Properties props = loadConfig(args[0]);

        // Add additional properties.
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "ashwin-streams-avro-1");
        // Disable caching to print the aggregation value after each record
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, "3");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");


        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<Integer, Customer> existingCustomers = builder.stream(topic, Consumed.with(Serdes.Integer(), SerdeGenerator.getSerde(props)));
        
        existingCustomers.filter((key, value) -> value.getAge() > 30 && value.getAge() < 55).foreach((key, value) -> System.out.println(key + " => " + value));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.setStateListener((newState, oldState) -> System.out.println("*** Changed state from " +oldState + " to " + newState));
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close)); 
 
     }
 }