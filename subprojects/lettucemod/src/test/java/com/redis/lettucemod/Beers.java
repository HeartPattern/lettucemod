package com.redis.lettucemod;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.async.RedisModulesAsyncCommands;
import com.redis.lettucemod.search.CreateOptions;
import com.redis.lettucemod.search.Field;
import com.redis.lettucemod.search.TextField.PhoneticMatcher;

import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisFuture;

public class Beers {

	private static final String FILE = "beers.json";
	public static final String PREFIX = "beer:";
	public static final String INDEX = "beers";

	public static final String FIELD_PAYLOAD = "payload";
	public static final Field<String> FIELD_ID = Field.tag("id").sortable().build();
	public static final Field<String> FIELD_BREWERY_ID = Field.tag("brewery_id").sortable().build();
	public static final Field<String> FIELD_NAME = Field.text("name").sortable().build();
	public static final Field<String> FIELD_ABV = Field.numeric("abv").sortable().build();
	public static final Field<String> FIELD_IBU = Field.numeric("ibu").sortable().build();
	public static final Field<String> FIELD_DESCRIPTION = Field.text("descript").matcher(PhoneticMatcher.ENGLISH)
			.noStem().build();
	public static final Field<String> FIELD_STYLE_NAME = Field.tag("style_name").sortable().build();
	public static final Field<String> FIELD_CATEGORY_NAME = Field.tag("cat_name").sortable().build();
	@SuppressWarnings("unchecked")
	public static final Field<String>[] SCHEMA = new Field[] { FIELD_ID, FIELD_NAME, FIELD_STYLE_NAME,
			FIELD_CATEGORY_NAME, FIELD_BREWERY_ID, FIELD_DESCRIPTION, FIELD_ABV, FIELD_IBU };
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static void createIndex(StatefulRedisModulesConnection<String, String> connection) {
		CreateOptions<String, String> options = CreateOptions.<String, String>builder().prefix(PREFIX)
				.payloadField(FIELD_PAYLOAD).build();
		connection.sync().ftCreate(INDEX, options, SCHEMA);
	}

	public static Iterator<JsonNode> jsonNodeIterator() throws IOException {
		return MAPPER.readerFor(Map.class).readTree(inputStream()).iterator();
	}

	public static MappingIterator<Map<String, Object>> mapIterator() throws IOException {
		return MAPPER.readerFor(Map.class).readValues(inputStream());
	}

	private static InputStream inputStream() {
		return Beers.class.getClassLoader().getResourceAsStream(FILE);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static int populateIndex(StatefulRedisModulesConnection<String, String> connection) throws IOException {
		createIndex(connection);
		connection.setAutoFlushCommands(false);
		RedisModulesAsyncCommands<String, String> async = connection.async();
		List<RedisFuture<?>> futures = new ArrayList<>();
		try {
			MappingIterator<Map<String, Object>> iterator = mapIterator();
			while (iterator.hasNext()) {
				Map<String, Object> beer = iterator.next();
				beer.put(FIELD_PAYLOAD, beer.get(FIELD_DESCRIPTION.getName()));
				futures.add(async.hset(PREFIX + beer.get(FIELD_ID.getName()), (Map) beer));
			}
			connection.flushCommands();
			LettuceFutures.awaitAll(connection.getTimeout(), futures.toArray(new RedisFuture[0]));
		} finally {
			connection.setAutoFlushCommands(true);
		}
		return futures.size();
	}
}