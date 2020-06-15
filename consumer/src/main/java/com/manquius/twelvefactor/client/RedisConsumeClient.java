package com.manquius.twelvefactor.client;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class RedisConsumeClient implements ConsumeClient {

    private static final Logger LOG = LoggerFactory.getLogger(RedisConsumeClient.class);
    /**
     * Redis default port constant.
     */
    public static final int REDIS_DEFAULT_PORT = 6379;

    private RedisCommands<String, String> sync;
    private final RedisURI redisUri;

    public RedisConsumeClient() throws ClientCreationException {
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
        if (sync == null) {
            RedisClient redisClient = RedisClient.create(redisUri);
            StatefulRedisConnection<String, String> connection = redisClient.connect();
            sync = connection.sync();
        }
        return sync;
    }

    @Override
    public List<String> consume(String topic) throws ConsumeException {
        try {
            return getCommands().lrange("key", 0, -1);
        }catch (Exception e) {
            throw new ConsumeException("Error consuming from Redis.", e);
        }
    }
}
