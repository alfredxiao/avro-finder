package xiaoyf.tools.kfind;

import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.Set;

public class KFindApplication {
    CommandLineHelper command;
    ConsumerConfigHelper config;
    ConsoleHelper console;
    ConsumerHelper helper;

    public KFindApplication(CommandLineHelper command, ConsumerConfigHelper config, ConsoleHelper console) {
        this.command = command;
        this.config = config;
        this.console = console;
    }

    boolean processRecord(Consumer<Object, Object> consumer, String grep, ConsumerRecord<Object, Object> record) {
        Object key = record.key();
        Object value = record.value();

        String keyString = valueStringToLog(key);
        String valueString = valueStringToLog(value, config.valueFields());

        if (StringUtils.isEmpty(grep)
                || MatchHelper.match(keyString, grep)
                || MatchHelper.match(valueString, grep)) {

            console.logf("p=%d,o=%d,ts=%d,t=%s,k=%s,v=%s\n",
                    record.partition(), record.offset(), record.timestamp(),
                    Instant.ofEpochMilli(record.timestamp()), keyString, valueString);

            if (!StringUtils.isEmpty(grep)) {
                helper.increaseGrepHit();
            }
        }

        helper.visitRecord(record);

        return helper.hasReachedGrepOrTotalLimit();
    }

    void report(Consumer<Object, Object> consumer, String grep, long start) {
        console.log("\n------------------------------Summary------------------------------");
        console.logf("# topic: %s, partition: %s, offset: %s, grep: %s",
                config.getTopic(), config.getPartitionOrNull(), config.geOffsetOrNull(), grep);
        console.logf(" from_epoch: %d, backward_duration: %d\n",
                config.getFromEpochOrNull(), config.getBackwardDurationOrNull());
        console.logf("# total limit: %d, grep limit: %d\n", config.getLimit(), config.getGrepLimit());
        console.logf("# total visited: %d: grep hit: %d\n", helper.getVisitCount(), helper.getGrepHit());
        console.log("# earliest visited offsets:" + helper.getEarliestVisitedOffsets());
        console.log("# latest visited offsets:" + helper.getLatestVisitedOffsets());
        console.log("# end offsets of the topic:" + consumer.endOffsets(consumer.assignment()));
        console.log("# time taken: " + (System.currentTimeMillis() - start) + " ms");
    }

    private static String valueStringToLog(Object value) {
        return valueStringToLog(value, null);
    }

    private static String valueStringToLog(Object value, Set<String> valueFields) {
        if (value == null) {
            return null;
        }

        if (!(value instanceof GenericRecord record) || ObjectUtils.isEmpty(valueFields)) {
            return value.toString();
        }

        return GenericRecordHelper.selectFields(record, valueFields).toString();
    }

    public void run() throws InterruptedException, IOException {

        Properties consumerConfig = config.getConsumerConfig();

        Consumer<Object, Object> consumer = new KafkaConsumer<>(consumerConfig);

        try (consumer) {
            long start = System.currentTimeMillis();
            helper = new ConsumerHelper(consumer, config, console);
            helper.waitForPartitionsAssigned(Duration.ofSeconds(30));
            helper.seekToExpectedStartingPoint();
            String grep = command.getOptionOrNull(CommandLineHelper._GREP);

            while (true) {
                ConsumerRecords<Object, Object> records = consumer.poll(Duration.ofMillis(200));

                for (ConsumerRecord<Object, Object> record : records) {
                    if (processRecord(consumer, grep, record)) {
                        report(consumer, grep, start);
                        return;
                    }
                }

                if (command.isExitWhenEndReached() && helper.hasReachedTopicEnd()) {
                    report(consumer, grep, start);
                    return;
                }
            }
        }
    }
}
