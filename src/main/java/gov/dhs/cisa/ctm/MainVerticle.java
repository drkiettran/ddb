package gov.dhs.cisa.ctm;

import java.net.URI;
import java.net.URISyntaxException;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

/**
 * MainVerticle class:
 * 
 * @author student
 *
 */
public class MainVerticle extends AbstractVerticle {
	private static final String DDB_QUEUE_KEY = "ddb_queue";
	public final Integer PORTNO = 9999;
	final static Logger Logger = LoggerFactory.getLogger(MainVerticle.class);

	@Override
	public void start(Promise<Void> startPromise) throws URISyntaxException {
		Logger.info("*** DDB Service starts ...");
		vertx.createHttpServer().requestHandler(req -> {
			Logger.info("*** request handler ..." + req.uri());
			req.body(bh -> {

				try {
					processRequest(req, bh);
				} catch (URISyntaxException e) {
					Logger.error("Invalid URI: ", e);
				}

			});
		}).listen(config().getInteger("http.port", PORTNO), http -> {
			if (http.succeeded()) {
				startPromise.complete();
				Logger.info("*** HTTP server started on port: " + config().getInteger("http.port", PORTNO));
				new AmqpQueueReceiver(DDB_QUEUE_KEY);
			} else {
				startPromise.fail(http.cause());
			}
		});
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
					new AmqpQueueSender(DDB_QUEUE_KEY, body);
					resp.setStatusCode(400);
					resp.setReason("Invalid content-type");
				} else {
					String body = bh.result().toString();
					new AmqpQueueSender(DDB_QUEUE_KEY, body);
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
}
