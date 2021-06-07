package com.drkiettran.ddb;

import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpConnection;
import io.vertx.amqp.AmqpMessage;
import io.vertx.amqp.AmqpSender;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;

public class AmqpQueueSender {
	final static Logger Logger = LoggerFactory.getLogger(AmqpQueueSender.class);

	private AmqpClient client;
	private AmqpConnection connection;
	private AmqpSender sender;

	public AmqpQueueSender(Vertx vertx, String key, String msg) {
		ConfigRetriever retriever = ConfigRetriever.create(vertx);
		retriever.getConfig(json -> {
			JsonObject config = json.result();
			JsonObject ddbQ = config.getJsonObject(key);
			EndpointConfig cfg = new EndpointConfig(key, ddbQ);
			Logger.info(cfg);

			AmqpClientOptions options = new AmqpClientOptions().setHost(cfg.getHostName()).setPort(cfg.getPortNo())
					.setUsername(cfg.getUserName()).setPassword(cfg.getPassword());

			if (cfg.getTls()) {
				options.setSsl(true).setHostnameVerificationAlgorithm("").setTrustAll(true);
				Buffer tlsKey = vertx.fileSystem().readFileBlocking(cfg.getTlsKey());
				Buffer tlsCert = vertx.fileSystem().readFileBlocking(cfg.getTlsCert());
				options.setPemKeyCertOptions(new PemKeyCertOptions().setKeyValue(tlsKey).setCertValue(tlsCert));
			}

			client = AmqpClient.create(vertx, options);

			client.connect(ar -> {
				if (ar.failed()) {
					Logger.error(ar.cause());
					Logger.info("*** Unable to connect to the broker");
				} else {
					Logger.info("*** Connection for sender created");
					connection = ar.result();
					connection.createSender(cfg.getQueueName(), done -> {
						if (done.failed()) {
							Logger.info("*** Unable to create a sender");
						} else {
							sender = done.result();
							Logger.info("*** sending >>> " + msg + " <<<");
							Logger.info("*** to      >>> " + cfg.getQueueName());
							sender.send(AmqpMessage.create().withBody(msg).build());
							sender.close();
						}
					});
				}
			});
		});
	}

}
