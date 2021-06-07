package com.drkiettran.ddb;

import java.net.URI;
import java.net.URISyntaxException;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.net.JksOptions;

/**
 * MainVerticle class: -Dvertx.options.blockedThreadCheckInterval=12345
 * 
 * @author student
 *
 */
public class MainVerticle extends AbstractVerticle {
	private static final String DDB_QUEUE_KEY = "in_queue";
	public final Integer PORTNO = 9999;
	private int portNo;
	private Boolean tls;
	private Boolean tlsMutual;
	private String keystore;
	private String keystorePassword;
	private String truststore;
	private String truststorePassword;
	private String hostName;
	final static Logger Logger = LoggerFactory.getLogger(MainVerticle.class);

	@Override
	public void start(Promise<Void> startPromise) throws URISyntaxException {
		Logger.info("*** DDB Service starts ...");

		getWebConfig();
		Logger.info(this);

		HttpServerOptions serverOptions = new HttpServerOptions();
		if (tls) {
			serverOptions.setSsl(true);
			serverOptions.setKeyStoreOptions(getJksKeystoreOptions());
			if (tlsMutual) {
				serverOptions.setTrustStoreOptions(getJksTruststoreOptions());
				serverOptions.setClientAuth(ClientAuth.REQUIRED);
			}
		}

		vertx.createHttpServer(serverOptions).requestHandler(req -> {
			Logger.info("*** request handler ..." + req.uri());
			req.body(bh -> {

				try {
					processRequest(req, bh);
				} catch (URISyntaxException e) {
					Logger.error("Invalid URI: ", e);
				}

			});
		}).listen(portNo, hostName, http -> {
			if (http.succeeded()) {
				startPromise.complete();
				Logger.info("*** HTTP server started on port: " + portNo);
				new AmqpQueueReceiver(vertx, DDB_QUEUE_KEY);
			} else {
				startPromise.fail(http.cause());
			}
		});
	}

	private JksOptions getJksKeystoreOptions() {
		JksOptions options = new JksOptions();
		options.setPath(keystore);
		options.setPassword(keystorePassword);
		return options;
	}

	private JksOptions getJksTruststoreOptions() {
		JksOptions options = new JksOptions();
		options.setPath(truststore);
		options.setPassword(truststorePassword);
		return options;
	}

	private void getWebConfig() {
		this.hostName = config().getString("http.hostname");
		this.portNo = config().getInteger("http.port");
		this.tls = config().getBoolean("tls");
		this.tlsMutual = config().getBoolean("tls_mutual");
		this.keystore = config().getString("keystore");
		this.keystorePassword = config().getString("keystore_password");
		this.truststore = config().getString("truststore");
		this.truststorePassword = config().getString("truststore_password");
	}

	private boolean validRequest(HttpServerRequest req, AsyncResult<Buffer> bh) {
		return "POST".equals(req.method().toString());
	}

	private void processRequest(HttpServerRequest req, AsyncResult<Buffer> bh) throws URISyntaxException {
		Response resp = new Response();
		URI uri = new URI(req.absoluteURI());

		if ("/to_ddb".equals(uri.getPath())) {
			if (!validRequest(req, bh)) {
				resp.setStatusCode(405);
				resp.setReason("Method not found!");
			} else {
				if (!req.headers().get("Content-type").equals("application/json")) {
					String body = bh.result().toString();
					new AmqpQueueSender(vertx, DDB_QUEUE_KEY, body);
					resp.setStatusCode(400);
					resp.setReason("Invalid content-type");
				} else {
					String body = bh.result().toString();
					new AmqpQueueSender(vertx, DDB_QUEUE_KEY, body);
					resp.setStatusCode(200);
					resp.setReason("OK");
				}
			}
		} else {
			resp.setStatusCode(400);
			resp.setReason("INVALID REQUEST!");
		}
		req.response().setStatusCode(resp.getStatusCode()).putHeader("content-type", "application/json")
				.end(resp.toString());

	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Main Verticle Cfg:");
		sb.append("\n\thost name: ").append(hostName);
		sb.append("\n\tport no: ").append(portNo);
		sb.append("\n\ttls: ").append(tls);
		sb.append("\n\tmutual tls: ").append(tlsMutual);
		sb.append("\n\tkeystore: ").append(keystore);
		sb.append("\n\tkeystore password: ").append(keystorePassword);
		sb.append("\n\ttruststore: ").append(truststore);
		sb.append("\n\ttruststore password: ").append(truststorePassword);
		return sb.toString();
	}
}
