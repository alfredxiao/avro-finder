package xiaoyf.tools.avrofinder;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;


public class ConsumerConfigHelper {
    public static final String DEFAULT_BOOTSTRAP_SERVER = "localhost:9092";
    public static final String DEFAULT_SCHEMA_REGISTRY = "http://localhost:8081";
    public static final String DEFAULT_KEY_DESERIALIZER = StringDeserializer.class.getName();
    //  avro uses io.confluent.kafka.serializers.KafkaAvroDeserializer.class
    public static final String DEFAULT_VALUE_DESERIALIZER = StringDeserializer.class.getName();
    public static final String FROM_BEGINNING = "beginning";
    public static final String FROM_END = "end";
    public static final int FROM_END_OFFSET = -2;

    private final CommandLineHelper command;

    public ConsumerConfigHelper(CommandLineHelper command) {
        this.command = command;
    }

    public Properties getConsumerConfig() throws IOException {
        Properties config = defaultConsumerConfig();
        Properties fileConfig = command.getConsumerConfigFromFile();
        Properties commandLineConfig = command.getConsumerConfigFromCommandLine();

        config.putAll(fileConfig);
        config.putAll(commandLineConfig);

        return config;
    }

    private Properties defaultConsumerConfig() {
        Properties config = new Properties();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, DEFAULT_BOOTSTRAP_SERVER);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, DEFAULT_KEY_DESERIALIZER);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DEFAULT_VALUE_DESERIALIZER);
        config.put("schema.registry.url", DEFAULT_SCHEMA_REGISTRY);
        config.put("avro.use.logical.type.converters", true);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        return config;
    }

    public Set<String> valueFields() {
        Set<String> fields = new HashSet<>();

        String valueFieldsArg = command.getOptionOrNull(CommandLineHelper._VALUE_FIELDS);
        if (!StringUtils.isBlank(valueFieldsArg)) {
            fields = Arrays.stream(valueFieldsArg.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
        }

        return fields;
    }

    public String getTopic() {
        return command.getOptionOrNull(CommandLineHelper._TOPIC);
    }

    public Integer geOffsetOrNull() {
        String offset = command.getOptionOrNull(CommandLineHelper._OFFSET);

        if (offset == null) {
            return null;
        }

        return switch (offset) {
            case FROM_BEGINNING -> null;
            case FROM_END -> FROM_END_OFFSET;
            default -> Integer.parseInt(offset);
        };
    }

    public Integer getPartitionOrNull() {
        String partition = command.getOptionOrNull(CommandLineHelper._PARTITION);

        return partition == null ? null : Integer.parseInt(partition);
    }

    public Integer getLimit() {
        String limit = command.getOptionOrNull(CommandLineHelper._LIMIT);
        return limit == null ? null : Integer.parseInt(limit);
    }

    public Integer getGrepLimit() {
        String grepLimit = command.getOptionOrNull(CommandLineHelper._GREP_LIMIT);
        return grepLimit == null ? null : Integer.parseInt(grepLimit);
    }

    public Long getFromEpochOrNull() {
        String fromEpoch = command.getOptionOrNull(CommandLineHelper._FROM_EPOCH);
        return fromEpoch == null ? null : Long.parseLong(fromEpoch);
    }

    public Long getBackwardDurationOrNull() {
        String backwardDuration = command.getOptionOrNull(CommandLineHelper._BACKWARD_DURATION);
        return backwardDuration == null ? null : Duration.parse(backwardDuration).toMillis();
    }
}
