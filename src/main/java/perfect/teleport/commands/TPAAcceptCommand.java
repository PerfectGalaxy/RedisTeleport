package perfect.teleport.commands;

import me.fixeddev.ebcm.parametric.CommandClass;
import me.fixeddev.ebcm.parametric.annotation.ACommand;
import me.fixeddev.ebcm.parametric.annotation.Injected;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import perfect.teleport.RedisTeleport;
import redis.clients.jedis.Jedis;

@ACommand(names = "tpaccept")
public class TPAAcceptCommand implements CommandClass {

    private final RedisTeleport redisTeleport;

    public TPAAcceptCommand(RedisTeleport redisTeleport) {
        this.redisTeleport = redisTeleport;
    }

    @ACommand(names = "")
    public boolean acceptCommand(@Injected(true)CommandSender s) {
        if (!(s instanceof Player)) {
            s.sendMessage("This command is only available for players!");
            return true;
        }

        Player player = (Player) s;

        boolean requestRedis;
        String sender = "";
        try (Jedis jedis = redisTeleport.getRedisConnection().getJedisPool().getResource()){
            requestRedis = jedis.hexists("TPARequests", player.getName() + ";" + Bukkit.getServerName());
            if(requestRedis){
                sender = jedis.hget("TPARequests", player.getName() + ";" + Bukkit.getServerName());
            }
        }

        if (!redisTeleport.getTpaManager().getTeleports().containsKey(player.getUniqueId()) && !requestRedis) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.No_Request").replace("%prefix%", redisTeleport.getPrefix())));
            return true;
        }

        if (redisTeleport.getTpaManager().getTeleports().containsKey(player.getUniqueId()) && !requestRedis) {
            if (Bukkit.getPlayer(redisTeleport.getTpaManager().getTeleports().get(player.getUniqueId())) == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.Sender_No_Online").replace("%prefix%", redisTeleport.getPrefix())));
                return true;
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.Accept_Target_Message").replace("%prefix%", redisTeleport.getPrefix()).replace("%player%", Bukkit.getPlayer(redisTeleport.getTpaManager().getTeleports().get(player.getUniqueId())).getName())));
            Bukkit.getPlayer(redisTeleport.getTpaManager().getTeleports().get(player.getUniqueId())).sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.Accept_Sender_Message").replace("%prefix%", redisTeleport.getPrefix()).replace("%player%", player.getName())));
            Bukkit.getPlayer(redisTeleport.getTpaManager().getTeleports().get(player.getUniqueId())).teleport(player);
            redisTeleport.getTpaManager().getTeleports().remove(player.getUniqueId());
            return true;
        }

        if (!requestRedis) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.No_Request").replace("%prefix%", redisTeleport.getPrefix())));
            return true;
        }

        if (requestRedis) {
            try (Jedis jedis = redisTeleport.getRedisConnection().getJedisPool().getResource()) {
                jedis.hdel("TPARequests", player.getName() + ";" + Bukkit.getServerName());
            }

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.Bungee_Accept_Target_Message").replace("%prefix%", redisTeleport.getPrefix()).replace("%player%", sender)));
            redisTeleport.getRedisConnection().sendMessage("TPA", "Accept", sender.split(";")[0] + ";" + sender.split(";")[1] + ";" + player.getName() + ";" + Bukkit.getServerName());
        }

        return true;
    }

}