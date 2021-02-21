package com.example.starter;

import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpConnection;
import io.vertx.amqp.AmqpMessage;
import io.vertx.amqp.AmqpSender;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

public class AmqpQueueSender {
	final static Logger Logger = LoggerFactory.getLogger(AmqpQueueSender.class);

	private AmqpClient client;
	private AmqpConnection connection;
	private AmqpSender sender;

	public AmqpQueueSender(String key, String msg) {
		Vertx vertx = Vertx.vertx();

		ConfigRetriever retriever = ConfigRetriever.create(vertx);
		retriever.getConfig(json -> {
			JsonObject config = json.result();
			JsonObject ddbQ = config.getJsonObject(key);
			EndpointConfig cfg = new EndpointConfig(key, ddbQ);

			AmqpClientOptions options = new AmqpClientOptions().setHost(cfg.getHostName()).setPort(cfg.getPortNo())
					.setUsername(cfg.getUserName()).setPassword(cfg.getPassword());

			client = AmqpClient.create(options);

			client.connect(ar -> {
				if (ar.failed()) {
					Logger.info("*** Unable to connect to the broker");
				} else {
					Logger.info("*** Connection for sender created");
					connection = ar.result();
					connection.createSender(cfg.getQueueName(), done -> {
						if (done.failed()) {
							Logger.info("*** Unable to create a sender");
						} else {
							sender = done.result();
							Logger.info("*** sending to .... " + cfg.getQueueName());
							sender.send(AmqpMessage.create().withBody(msg).build());
							sender.close();
						}
					});
				}
			});
		});
	}

}