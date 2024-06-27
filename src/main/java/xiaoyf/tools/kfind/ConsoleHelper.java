package xiaoyf.tools.kfind;

public class ConsoleHelper {

    public void log(Object msg) {
        System.out.println(msg);
    }

    public void logf(String format, Object ... args) {
        System.out.printf(format, args);
    }
}
