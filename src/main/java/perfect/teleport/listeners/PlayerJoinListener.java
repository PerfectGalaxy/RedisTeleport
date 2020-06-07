package perfect.teleport.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import perfect.teleport.RedisTeleport;
import redis.clients.jedis.Jedis;

public class PlayerJoinListener implements Listener {

    private final RedisTeleport redisTeleport;

    public PlayerJoinListener(RedisTeleport redisTeleport) {
        this.redisTeleport = redisTeleport;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try (Jedis jedis = redisTeleport.getRedisConnection().getJedisPool().getResource()) {
            jedis.hset("Players", event.getPlayer().getName(), Bukkit.getServerName());
        }
    }
}