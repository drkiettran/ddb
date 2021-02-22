package gov.dhs.cisa.ctm;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.predicate.ResponsePredicate;

public class Response {
	private int statusCode;
	private String reason;

	public static <T extends Response> T fromJsonObject(JsonObject source, Class<T> clazz) {
		return Json.decodeValue(source.encode(), clazz);
	}

	public JsonObject toJsonObject() {
		return new JsonObject(Json.encode(this));
	}

	protected static <T> T validate(final String key, final T value) {
		if (null == value) {
			throw new IllegalArgumentException(key + " must not be null");
		} else {
			return (T) value;
		}
	}

	public String toString() {
		return Json.encode(this);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int i) {
		this.statusCode = i;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
