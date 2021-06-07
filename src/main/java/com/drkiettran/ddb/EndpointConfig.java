package com.drkiettran.ddb;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

public class EndpointConfig {
	final static Logger Logger = LoggerFactory.getLogger(EndpointConfig.class);
	private String hostName;
	private Integer portNo;
	private String userName;
	private String password;
	private String queueName;
	private String type;
	private Boolean tls;
	private String tlsCert;
	private String tlsKey;
	private String key;

	public EndpointConfig(String key, JsonObject ddbQ) {
		this.hostName = ddbQ.getString("host_name");
		this.portNo = ddbQ.getInteger("port_no");
		this.userName = ddbQ.getString("user_name");
		this.password = ddbQ.getString("password");
		this.queueName = ddbQ.getString("queue_name");
		this.type = ddbQ.getString("type");
		this.tls = ddbQ.getBoolean("tls");
		this.tlsCert = ddbQ.getString("tls_cert");
		this.tlsKey = ddbQ.getString("tls_key");
		this.key = key;
	}

	public Boolean getTls() {
		return tls;
	}

	public String getType() {
		return type;
	}

	public String getTlsCert() {
		return tlsCert;
	}

	public String getTlsKey() {
		return tlsKey;
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

	public String toString() {
		StringBuilder sb = new StringBuilder("Endpoint:\n");
		sb.append("\tkey:").append(key);
		sb.append("\n\thost name:").append(hostName);
		sb.append("\n\tport no:").append(portNo);
		sb.append("\n\tuser name:").append(userName);
		sb.append("\n\tpassword:").append(password);
		sb.append("\n\tqueue name:").append(queueName);
		sb.append("\n\ttype:").append(type);
		sb.append("\n\ttls:").append(tls);
		sb.append("\n\tcert:").append(tlsCert);
		sb.append("\n\tkey:").append(tlsKey);
		return sb.toString();
	}
}
