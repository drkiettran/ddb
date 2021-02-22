package gov.dhs.cisa.ctm;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

public class EndpointConfig {
	final static Logger Logger = LoggerFactory.getLogger(EndpointConfig.class);

	public static final String DDB_QUEUE_NAME = "ddb_queue";
	private String hostName;
	private Integer portNo;
	private String userName;
	private String password;
	private String queueName;
	private String key;

	public EndpointConfig(String key, JsonObject ddbQ) {
		this.hostName = ddbQ.getString("host_name");
		this.portNo = ddbQ.getInteger("port_no");
		this.userName = ddbQ.getString("user_name");
		this.password = ddbQ.getString("password");
		this.queueName = ddbQ.getString("queue_name");
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public String getHostName() {
		return hostName;
	}

	public Integer getPortNo() {
		return portNo;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getQueueName() {
		return queueName;
	}
}
