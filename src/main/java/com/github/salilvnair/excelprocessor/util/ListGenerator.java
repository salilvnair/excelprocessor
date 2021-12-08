package com.github.salilvnair.excelprocessor.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Salil V Nair
 */
public class ListGenerator {

    private final boolean mutable;

    private ListGenerator(boolean mutable) {
        this.mutable = mutable;
    }

    public static ListGenerator immutable() {
        return new ListGenerator(false);
    }

    public static  ListGenerator mutable() {
        return new ListGenerator(true);
    }

    @SafeVarargs
    public final <E> List<E> generate(E... elements) {
        if(elements==null || elements.length == 0) {
            return Collections.emptyList();
        }
        List<E> list = Arrays.stream(elements).collect(Collectors.toList());
        return !mutable ? list : new ArrayList<>(list);
    }
}
