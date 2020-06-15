package com.manquius.twelvefactor.clients;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisProduceClient implements ProduceClient {

    private static final Logger LOG = LoggerFactory.getLogger(RedisProduceClient.class);
    /**
     * Redis default port constant.
     */
    public static final int REDIS_DEFAULT_PORT = 6379;

    private RedisCommands<String, String> sync;
    private final RedisURI redisUri;

    public RedisProduceClient() throws ClientCreationException {
        String host = System.getenv("TWELVEFACTOR_REDIS_MASTER_SERVICE_HOST");
        LOG.debug("Reading Redis host configuration from TWELVEFACTOR_REDIS_MASTER_SERVICE_HOST: " + host);
        if(host == null){
            host = System.getenv("TWELVEFACTOR_REDIS_ANNOUNCE_0_SERVICE_HOST");
            LOG.debug("Reading Redis host configuration from TWELVEFACTOR_REDIS_ANNOUNCE_0_SERVICE_HOST: " + host);
        }
        if(host == null){
            LOG.debug("Defaulting Redis host configuration to: localhost");
            host = "localhost";
        }
        String portString = System.getenv("TWELVEFACTOR_REDIS_MASTER_SERVICE_PORT");
        LOG.debug("Reading Redis host configuration from TWELVEFACTOR_REDIS_MASTER_SERVICE_PORT: " + portString);
        if(portString == null){
            portString = System.getenv("TWELVEFACTOR_REDIS_ANNOUNCE_0_SERVICE_PORT");
            LOG.debug("Reading Redis host configuration from TWELVEFACTOR_REDIS_ANNOUNCE_0_SERVICE_PORT: " + portString);
        }
        int port;
        if(portString == null){
            LOG.debug("Defaulting Redis port configuration to: " + REDIS_DEFAULT_PORT);
            port = REDIS_DEFAULT_PORT;
        } else {
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                LOG.error("Configured Redis port is not a number: " + portString + ". Using default: " + REDIS_DEFAULT_PORT);
                port = REDIS_DEFAULT_PORT;
            }
        }
        redisUri = RedisURI.Builder.redis(host)
                .withPort(port)
                .build();
    }

    private RedisCommands<String, String> getCommands() {
        if(sync == null) {
            RedisClient redisClient = RedisClient.create(redisUri);
            StatefulRedisConnection<String, String> connection = redisClient.connect();
            sync = connection.sync();
        }
        return sync;
    }

    @Override
    public void produce(String topic, String message) throws ProduceException {
        try {
            getCommands().lpush(topic, message);
        } catch (Exception e) {
            throw new ProduceException("Error producing to Redis", e);
        }
    }
}
