package perfect.teleport.managers;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import perfect.teleport.RedisTeleport;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPAManager {

    private final Map<UUID, UUID> teleports;

    public TPAManager() {
        teleports = new HashMap<>();
    }

    public Map<UUID, UUID> getTeleports() {
        return teleports;
    }

    public TextComponent getAcceptMessage(){
        TextComponent accept = new TextComponent("Accept  ");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to accept!").color(net.md_5.bungee.api.ChatColor.GREEN).create()));
        accept.setColor(net.md_5.bungee.api.ChatColor.GREEN);

        return accept;
    }

    public TextComponent getDenyMessage(){
        TextComponent deny = new TextComponent("Deny");
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpadeny"));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to deny!").color(net.md_5.bungee.api.ChatColor.RED).create()));
        deny.setColor(net.md_5.bungee.api.ChatColor.RED);
        return deny;
    }

    public void startCountdown(UUID sender, UUID target){
        RedisTeleport redisTeleport = JavaPlugin.getPlugin(RedisTeleport.class);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(redisTeleport, () -> {
            if(teleports.containsKey(target) && teleports.get(target) != null){
                Bukkit.getPlayer(sender).sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.Sender_Message_TimeOut").replace("%prefix%", redisTeleport.getPrefix()).replace("%player%", Bukkit.getPlayer(target).getName())));
                Bukkit.getPlayer(target).sendMessage(ChatColor.translateAlternateColorCodes('&', redisTeleport.getLang().getString("Messages.TPA.Target_Message_TimeOut").replace("%prefix%", redisTeleport.getPrefix()).replace("%player%", Bukkit.getPlayer(sender).getName())));
                teleports.remove(target);
            }
        }, redisTeleport.getConfig().getInt("TPA.TimeOut")*20);
    }
}