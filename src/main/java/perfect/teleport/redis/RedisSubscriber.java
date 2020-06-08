package perfect.teleport.redis;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import perfect.teleport.RedisTeleport;
import redis.clients.jedis.JedisPubSub;

public class RedisSubscriber extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        RedisTeleport redisTeleport = JavaPlugin.getPlugin(RedisTeleport.class);
        if (channel.equals("TPA")) {
            String[] msg = message.split(";");
            String sender;
            String senderServer;
            String target;
            String targetServer;

            switch (msg[0]) {
                case "Request":
                    sender = msg[1];
                    senderServer = msg[2];
                    target = msg[3];
                    targetServer = msg[4];

                    if(!Bukkit.getServerName().equals(targetServer)){
                        break;
                    }

                    Bukkit.getPlayer(target).sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.Bungee_Target_Message").replace("%prefix%", redisTeleport.getPrefix()).replace("%player%", sender).replace("%server_sender%", senderServer).replace("%seconds%", redisTeleport.getConfig().getInt("TPA.TimeOut") + "")));
                    Bukkit.getPlayer(target).spigot().sendMessage(redisTeleport.getTpaManager().getAcceptMessage(), redisTeleport.getTpaManager().getDenyMessage());
                    break;
                case "Accept":
                    sender = msg[1];
                    targetServer =  msg[4];
                    target = msg[3];
                    senderServer = msg[2];

                    if(!Bukkit.getServerName().equals(senderServer)){
                        break;
                    }

                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Connect");
                    out.writeUTF(targetServer);

                    Bukkit.getPlayer(sender).sendPluginMessage(redisTeleport, "BungeeCord", out.toByteArray());

                    redisTeleport.getRedisConnection().sendMessage("TPA", "Teleport", sender + ";" + senderServer + ";" + target + ";" + targetServer);
                    break;
                case "Teleport":
                    sender = msg[1];
                    targetServer =  msg[4];
                    target = msg[3];

                    if(!Bukkit.getServerName().equals(targetServer)){
                        break;
                    }

                    Bukkit.getPlayer(sender).sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.Bungee_Accept_Sender_Message").replace("%prefix%", redisTeleport.getPrefix()).replace("%player%", target).replace("%server_target%", targetServer)));
                    Bukkit.getPlayer(sender).teleport(Bukkit.getPlayer(target));
                    break;
                default:
                    break;
            }
        }
    }
}