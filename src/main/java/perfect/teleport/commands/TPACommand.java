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

@ACommand(names = "tpa")
public class TPACommand implements CommandClass {

    private final RedisTeleport redisTeleport;

    public TPACommand(RedisTeleport redisTeleport) {
        this.redisTeleport = redisTeleport;
    }

    @ACommand(names = "")
    public boolean tpaCommand(@Injected(true)CommandSender sender, String name) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only available for players!");
            return true;
        }

        Player player = (Player) sender;

        if(player.getName().equals(name)){
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.Same_Player").replace("%prefix%", redisTeleport.getPrefix())));
            return true;
        }

        if (Bukkit.getPlayer(name) != null){
            redisTeleport.getTpaManager().getTeleports().put(Bukkit.getPlayer(name).getUniqueId(), player.getUniqueId());
            Bukkit.getPlayer(name).sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.Target_Message").replace("%prefix%", redisTeleport.getPrefix()).replace("%player%", player.getName()).replace("%seconds%", redisTeleport.getConfig().getInt("TPA.TimeOut") + "")));
            Bukkit.getPlayer(name).spigot().sendMessage(redisTeleport.getTpaManager().getAcceptMessage(), redisTeleport.getTpaManager().getDenyMessage());

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.Sender_Message").replaceAll("%prefix%", redisTeleport.getPrefix()).replace("%player%", name)));
            redisTeleport.getTpaManager().startCountdown(player.getUniqueId(), Bukkit.getPlayer(name).getUniqueId());

            return true;
        }

        String targetServer;
        try (Jedis jedis = redisTeleport.getRedisConnection().getJedisPool().getResource()) {
            if(!jedis.hexists("Players", name)){
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.Player_No_Exists").replace("%prefix%", redisTeleport.getPrefix())));
                return true;
            }

            targetServer = jedis.hget("Players", name);
            jedis.hset("TPARequests", name + ";" + targetServer, player.getName() + ";" + Bukkit.getServerName());
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.Bungee_Sender_Message").replace("%prefix%", redisTeleport.getPrefix()).replace("%player%", name).replace("%seconds%", redisTeleport.getConfig().getInt("TPA.TimeOut") + "").replace("%server_target%", targetServer)));
        redisTeleport.getRedisConnection().sendMessage("TPA", "Request", player.getName() + ";" + Bukkit.getServerName() + ";" + name + ";" + targetServer);

        return true;
    }

}