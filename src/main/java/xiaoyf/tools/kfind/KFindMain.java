package xiaoyf.tools.kfind;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Properties;


public class KFindMain {

    public static void main(String[] args) throws Exception {
        CommandLineHelper command = new CommandLineHelper(args);
        ConsumerConfigHelper config = new ConsumerConfigHelper(command);
        ConsoleHelper console = new ConsoleHelper();
        
        KFindApplication application = new KFindApplication(command, config, console);

        application.run();
    }

}
