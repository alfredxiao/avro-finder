package xiaoyf.tools.avrofinder;

public class MatchHelper {

    static boolean match(final String str, final String wildcardMatcher) {
        if (str == null) {
            return false;
        }

        if (wildcardMatcher.contains("*") || wildcardMatcher.contains("?")) {
            return io.confluent.kafka.schemaregistry.utils.WildcardMatcher.match(str, wildcardMatcher);
        }

        return str.contains(wildcardMatcher);
    }
}
