package main.java.examples;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.TopicExistsException;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static examples.Util.loadConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomersAvroProducer {

  // Create topic in Confluent Cloud
  public static void createTopic(final String topic,
                          final Properties cloudConfig) {
      final NewTopic newTopic = new NewTopic(topic, Optional.empty(), Optional.empty());
      try (final AdminClient adminClient = AdminClient.create(cloudConfig)) {
          adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
      } catch (final InterruptedException | ExecutionException e) {
          // Ignore if TopicExistsException, which may be valid if topic exists
          if (!(e.getCause() instanceof TopicExistsException)) {
              throw new RuntimeException(e);
          }
      }
  }

  public static void main(final String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Please provide command line arguments: configPath topic");
      System.exit(1);
    }

    // Load properties from a local configuration file
    // Create the configuration file (e.g. at '$HOME/.confluent/java.config') with configuration parameters
    // to connect to your Kafka cluster, which can be on your local host, Confluent Cloud, or any other cluster.
    // Follow these instructions to create this file: https://docs.confluent.io/platform/current/tutorials/examples/clients/docs/java.html
    final Properties props = loadConfig(args[0]);

    // Create topic if needed
    final String topic = args[1];
    createTopic(topic, props);

    // Add additional properties.
    // props.put(ProducerConfig.ACKS_CONFIG, "all");
    // props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
    // props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

    String avroSchema = "{\"type\": \"record\", \"name\": \"Customer\", \"namespace\": \"schema\", \"fields\": [{\"name\": \"customerId\", \"type\": \"int\", \"doc\": \"Identifier for reference\"}, {\"name\": \"email\", \"type\": \"string\", \"doc\": \"Email address of user\"}, {\"name\": \"age\", \"type\": \"int\", \"doc\": \"Age of user\"}], \"doc:\": \"Schema of user\"}";
    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(avroSchema);

    Producer<Integer, Object> producer = new KafkaProducer<Integer, Object>(props);

    // Produce sample data
    final Integer numMessages = 50;
    for (Integer i = 0; i < numMessages; i++) {
      Integer key = i;
      String email = String.format("employee%d@mail.com", i);
      Integer age = i + 20;
      GenericRecord CustomerRecordAvro = new GenericData.Record(schema);
      CustomerRecordAvro.put("customerId", i);
      CustomerRecordAvro.put("email", email);
      CustomerRecordAvro.put("age", age);


      System.out.printf("Producing record: %s\t%s%n", key, CustomerRecordAvro);
      producer.send(new ProducerRecord<Integer, Object>(topic, key, CustomerRecordAvro), new Callback() {
          @Override
          public void onCompletion(RecordMetadata m, Exception e) {
            if (e != null) {
              e.printStackTrace();
            } else {
              System.out.printf("Produced record to topic %s partition [%d] @ offset %d%n", m.topic(), m.partition(), m.offset());
            }
          }
      });
    }

    producer.flush();

    System.out.printf("50 messages were produced to topic %s%n", topic);

    producer.close();
  }
}