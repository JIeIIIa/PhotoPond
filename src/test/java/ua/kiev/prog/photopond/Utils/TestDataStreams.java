package ua.kiev.prog.photopond.Utils;

import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface TestDataStreams {

    static Stream nullable() {
        return Stream.of(new Object[]{null});
    }

    static Stream<String> strings() {
        return Stream.of("first", "second", "third");
    }

    static LongStream longs() {
        return LongStream.range(1, Long.MAX_VALUE).limit(5);
    }
}
