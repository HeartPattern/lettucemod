package com.redis.lettucemod.api.sync;

import com.redis.lettucemod.timeseries.Aggregation;
import com.redis.lettucemod.timeseries.CreateOptions;
import com.redis.lettucemod.timeseries.Label;

public interface RedisTimeSeriesCommands<K,V> {

    @SuppressWarnings("unchecked")
    String create(K key, Label<K, V>... labels);

    @SuppressWarnings("unchecked")
    String create(K key, CreateOptions options, Label<K, V>... labels);

    @SuppressWarnings("unchecked")
    Long add(K key, long timestamp, double value, Label<K, V>... labels);

    @SuppressWarnings("unchecked")
    Long add(K key, long timestamp, double value, CreateOptions options, Label<K, V>... labels);

    String createRule(K sourceKey, K destKey, Aggregation aggregationType, long timeBucket);

    String deleteRule(K sourceKey, K destKey);
}