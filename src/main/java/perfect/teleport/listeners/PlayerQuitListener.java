package perfect.teleport.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import perfect.teleport.RedisTeleport;
import redis.clients.jedis.Jedis;

public class PlayerQuitListener implements Listener {

    private final RedisTeleport redisTeleport;

    public PlayerQuitListener(RedisTeleport redisTeleport) {
        this.redisTeleport = redisTeleport;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        try (Jedis jedis = redisTeleport.getRedisConnection().getJedisPool().getResource()) {
            jedis.hdel("Players", event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onKick(PlayerQuitEvent event) {
        try (Jedis jedis = redisTeleport.getRedisConnection().getJedisPool().getResource()) {
            jedis.hdel("Players", event.getPlayer().getName());
        }
    }
}