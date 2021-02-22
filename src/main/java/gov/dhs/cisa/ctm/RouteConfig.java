package gov.dhs.cisa.ctm;

import java.util.HashMap;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class RouteConfig {

	class Route {
		private String source;
		private String destination;
		private String payloadType; // text or binary
		private String compressed; // no or gzip
		private String encrypted; // no or pki or secret

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getDestination() {
			return destination;
		}

		public void setDestination(String destination) {
			this.destination = destination;
		}

		public String getPayloadType() {
			return payloadType;
		}

		public void setPayloadType(String payloadType) {
			this.payloadType = payloadType;
		}

		public String getCompressed() {
			return compressed;
		}

		public void setCompressed(String compressed) {
			this.compressed = compressed;
		}

		public String getEncrypted() {
			return encrypted;
		}

		public void setEncrypted(String encrypted) {
			this.encrypted = encrypted;
		}

		public <T extends Route> T fromJsonObject(JsonObject src, Class<T> clazz) {
			return Json.decodeValue(src.encode(), clazz);
		}

		public JsonObject toJsonObject() {
			return new JsonObject(Json.encode(this));
		}

		protected <T> T validate(final String key, final T value) {
			if (null == value) {
				throw new IllegalArgumentException(key + " must not be null");
			} else {
				return (T) value;
			}
		}

		public String toString() {
			return Json.encode(this);
		}
	}

	private HashMap<String, Route> routes = new HashMap<String, Route>();

	public RouteConfig(JsonArray jsonArray) {
		jsonArray.forEach(obj -> {
			JsonObject json = (JsonObject) obj;
			Route route = new Route();

			route.setSource(json.getString("source_endpoint"));
			route.setDestination(json.getString("destination_endpoint"));
			route.setPayloadType(json.getString("payload_type"));
			route.setEncrypted(json.getString("encrypted"));
			route.setCompressed(json.getString("compressed"));

			routes.put(route.getSource(), route);
		});
	}

	public Route getRoute(String source) {
		return routes.get(source);
	}
}
