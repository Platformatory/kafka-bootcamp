# Stage 1 -- install connectors
FROM confluentinc/cp-server-connect:7.3.1 AS install-connectors

ENV CONNECT_PLUGIN_PATH: "/usr/share/java,/usr/share/confluent-hub-components"

# Install required connector
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:10.6.0
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-azure-blob-storage-source:2.5.1
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-azure-blob-storage:1.6.15
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-azure-data-lake-gen2-storage:1.6.15
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-azure-functions:2.0.2
