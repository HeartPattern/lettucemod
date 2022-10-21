package com.redis.lettucemod.search.querybuilder;

/**
 * Created by mnunberg on 2/23/18.
 */
public class GeoValue extends Value {
	public enum Unit {
		KILOMETERS("km"), METERS("m"), FEET("ft"), MILES("mi");

		private final String distanceUnit;

		Unit(String unit) {
			this.distanceUnit = unit;
		}

		@Override
		public String toString() {
			return distanceUnit;
		}
	}

	private final String unit;
	private final double lon;
	private final double lat;
	private final double radius;

	public GeoValue(double lon, double lat, double radius, Unit unit) {
		this.lon = lon;
		this.lat = lat;
		this.radius = radius;
		this.unit = unit.toString();
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(lon).append(" ").append(lat).append(" ").append(radius).append(" ")
				.append(unit).append("]").toString();
	}

	@Override
	public boolean isCombinable() {
		return false;
	}
}
