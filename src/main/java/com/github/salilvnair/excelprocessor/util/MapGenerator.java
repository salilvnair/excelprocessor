package com.github.salilvnair.excelprocessor.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Salil V Nair
 */
public class MapGenerator {
    private final boolean mutable;

    private final boolean ordered;



    private MapGenerator(boolean mutable) {
        this.mutable = mutable;
        this.ordered = false;
    }

    private MapGenerator(boolean mutable, boolean ordered) {
        this.mutable = mutable;
        this.ordered = ordered;
    }
    
    public static MapGenerator immutable() {
        return new MapGenerator(false);
    }

    public static MapGenerator ordered() {
        return ordered(false);
    }

    public static MapGenerator ordered(boolean mutable) {
        return new MapGenerator(mutable, true);
    }

    public static  MapGenerator mutable() {
        return new MapGenerator(true);
    }


    public <K, V> Map<K, V> generate(K k1, V v1) {
        Map<K,V> map = ordered ? new LinkedHashMap<>() : new HashMap<>();
        map.put(k1, v1);
        return !mutable ? Collections.unmodifiableMap(map) : map;
    }
    public  <K, V> Map<K, V> generate(K k1, V v1, K k2, V v2) {
        Map<K, V> map = ordered ? new LinkedHashMap<>(generate(k1, v1)) : new HashMap<>(generate(k1, v1));
        map.putAll(generate(k2,v2));
        return !mutable ? Collections.unmodifiableMap(map) : map;
    }
    public  <K, V> Map<K, V> generate(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = ordered ? new LinkedHashMap<>(generate(k1, v1, k2, v2)) : new HashMap<>(generate(k1, v1, k2, v2));
        map.putAll(generate(k3,v3));
        return !mutable ? Collections.unmodifiableMap(map) : map;
    }
    public  <K, V> Map<K, V> generate(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> map = ordered ? new LinkedHashMap<>(generate(k1, v1, k2, v2, k3, v3)) : new HashMap<>(generate(k1, v1, k2, v2, k3,v3));
        map.putAll(generate(k4,v4));
        return !mutable ? Collections.unmodifiableMap(map) : map;
    }
    public  <K, V> Map<K, V> generate(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        Map<K, V> map = ordered ? new LinkedHashMap<>(generate(k1, v1, k2, v2, k3, v3)) : new HashMap<>(generate(k1, v1, k2, v2, k3,v3));
        map.putAll(generate(k4,v4, k5, v5));
        return !mutable ? Collections.unmodifiableMap(map) : map;
    }
}
