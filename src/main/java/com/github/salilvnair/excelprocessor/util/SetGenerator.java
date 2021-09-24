package com.github.salilvnair.excelprocessor.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Salil V Nair
 */
public class SetGenerator {

    private final boolean mutable;
    private final boolean ordered;

    private SetGenerator(boolean mutable) {
        this.mutable = mutable;
        this.ordered = false;
    }

    private SetGenerator(boolean mutable, boolean ordered) {
        this.mutable = mutable;
        this.ordered = ordered;
    }

    public static SetGenerator immutable() {
        return new SetGenerator(false);
    }

    public static SetGenerator mutable() {
        return new SetGenerator(true);
    }

    public static SetGenerator ordered() {
        return ordered(false);
    }

    public static SetGenerator ordered(boolean immutable) {
        return new SetGenerator(immutable, true);
    }

    @SafeVarargs
    public final <E> Set<E> generate(E... elements) {
        if(elements==null || elements.length == 0) {
            return Collections.emptySet();
        }
        Set<E> set = Arrays.stream(elements).collect(Collectors.toSet());
        return !mutable ? ordered ? new LinkedHashSet<>(set) : set : ordered ? new LinkedHashSet<>(set) : new HashSet<>(set);
    }
}
