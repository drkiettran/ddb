package com.drkiettran.ddb;

import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpConnection;
import io.vertx.amqp.AmqpMessage;
import io.vertx.amqp.AmqpReceiver;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;

public class AmqpQueueReceiver {
	final static Logger Logger = LoggerFactory.getLogger(AmqpQueueReceiver.class);
	private AmqpClient client;
	private AmqpConnection connection;
	private AmqpReceiver receiver;
	private Vertx vertx;
	private String key;

	public AmqpQueueReceiver(Vertx vertx, String key) {
		this.key = key;
		this.vertx = vertx;
		ConfigRetriever retriever = ConfigRetriever.create(vertx);
		retriever.getConfig(json -> {
			JsonObject config = json.result();
			JsonObject ddbQ = config.getJsonObject(key);
			EndpointConfig cfg = new EndpointConfig(key, ddbQ);
			createConnection(cfg);
		});
	}

	private void createConnection(EndpointConfig cfg) {
		Logger.info("*** Connecting ....");
		AmqpClientOptions options = new AmqpClientOptions().setHost(cfg.getHostName()).setPort(cfg.getPortNo())
				.setUsername(cfg.getUserName()).setPassword(cfg.getPassword());
		Logger.info(cfg);

		if (cfg.getTls()) {
			options.setSsl(true).setHostnameVerificationAlgorithm("").setTrustAll(true);
			Buffer key = vertx.fileSystem().readFileBlocking(cfg.getTlsKey());
			Buffer cert = vertx.fileSystem().readFileBlocking(cfg.getTlsCert());
			options.setPemKeyCertOptions(new PemKeyCertOptions().setKeyValue(key).setCertValue(cert));
		}

		client = AmqpClient.create(vertx, options);

		client.connect(ar -> {
			if (ar.failed()) {
				Logger.info("*** Unable to connect to the broker");
			} else {
				receiveMessage(ar, cfg);
			}
		});
	}

	private void receiveMessage(AsyncResult<AmqpConnection> ar, EndpointConfig cfg) {
		Logger.info("*** Connected successfully!");
		connection = ar.result();
		connection.createReceiver(cfg.getQueueName(), done -> {
			Logger.info("*** receiving ...");
			if (done.failed()) {
				Logger.info("*** Unable to create receiver");
			} else {
				receiver = done.result();
				receiver.handler(msg -> {
					Logger.info(msg);
					processMsg(msg, cfg);
				});
			}

		});
	}

	private void processMsg(AmqpMessage msg, EndpointConfig cfg) {
		ConfigRetriever retriever = ConfigRetriever.create(vertx);
		retriever.getConfig(json -> {
			JsonObject result = json.result();
			RouteConfig.Route route = new RouteConfig(result.getJsonArray("routes")).getRoute(cfg.getKey());
			if (route != null) {
				Logger.info("*** receiving >>> " + msg.bodyAsString() + " <<<");
				Logger.info("*** from      >>> " + key + " <<<");
				new AmqpQueueSender(vertx, route.getDestination(), msg.bodyAsString());
			} else {
				Logger.info("No route!");
			}
			Logger.info("Processing completes!");
		});
	}

}
