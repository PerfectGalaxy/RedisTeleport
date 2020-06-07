package perfect.teleport.redis;

import org.bukkit.scheduler.BukkitRunnable;
import perfect.teleport.RedisTeleport;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnection {

    private final RedisTeleport redisTeleport;
    private JedisPool jedisPool;
    private final String password;
    private final String host;
    private final int port;

    public RedisConnection(RedisTeleport redisTeleport, String password, String host, int port) {
        this.password = password;
        this.redisTeleport = redisTeleport;
        this.host = host;
        this.port = port;
    }

    public void redisConnect() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(8);

        if(password != null && !password.trim().isEmpty()) {
            jedisPool = new JedisPool(jedisPoolConfig, host, port, 2000, password);
        } else {
            jedisPool = new JedisPool(jedisPoolConfig, host, port, 2000);
        }
    }

    public void subscribeChannel(String... channels) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.subscribe(new RedisSubscriber(), channels);
                }
            }
        }.runTaskAsynchronously(redisTeleport);
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void sendMessage(String channel, String subchannel, String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel, subchannel + ";" + message);
        }
    }
}