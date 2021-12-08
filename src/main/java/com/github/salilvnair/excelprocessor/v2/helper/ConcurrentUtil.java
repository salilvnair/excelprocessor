package com.github.salilvnair.excelprocessor.v2.helper;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Salil V Nair
 */
public class ConcurrentUtil {
    private ConcurrentUtil(){}

    public static <T> Stream<List<T>> split(List<T> sourceList, int length) {
        if(sourceList.isEmpty()) {
            return Stream.empty();
        }
        int listSize = sourceList.size();
        int splitChunks = (listSize -1) / length ;

        return IntStream
                .range(0, splitChunks+1)
                .mapToObj(n -> sourceList.subList(n * length, n == splitChunks ? listSize : (n+1) * length ));
    }

    public static Stream<List<Integer>> split(int totalSize, int length) {
        List<Integer> boxed = IntStream
                .range(0, totalSize).boxed().collect(Collectors.toList());
        return split(boxed, length);
    }
}
