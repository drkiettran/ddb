package com.drkiettran.ddb;

import io.vertx.amqp.AmqpMessage;
import io.vertx.amqp.AmqpMessageBuilder;
import io.vertx.core.json.JsonObject;

public class DdbMessage {
	private AmqpMessageBuilder builder;
	private AmqpMessage m1;
	private AmqpMessage m2;
	private AmqpMessage m3;

	public DdbMessage() {
		builder = AmqpMessage.create();

		// Very simple message
		 m1 = builder.withBody("hello").build();

		// Message overriding the destination
		 m2 = builder.withBody("hello").address("another-queue").build();

		// Message with a JSON object as body, metadata and TTL
		 m3 = builder
		  .withJsonObjectAsBody(new JsonObject().put("message", "hello"))
		  .subject("subject")
		  .ttl(10000)
		  .applicationProperties(new JsonObject().put("prop1", "value1"))
		  .build();
	}

	public AmqpMessage getM1() {
		return m1;
	}

	public AmqpMessage getM2() {
		return m2;
	}

	public AmqpMessage getM3() {
		return m3;
	}
}
