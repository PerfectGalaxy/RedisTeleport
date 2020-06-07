package perfect.teleport;

import me.fixeddev.ebcm.Authorizer;
import me.fixeddev.ebcm.CommandManager;
import me.fixeddev.ebcm.Messenger;
import me.fixeddev.ebcm.SimpleCommandManager;
import me.fixeddev.ebcm.bukkit.BukkitAuthorizer;
import me.fixeddev.ebcm.bukkit.BukkitCommandManager;
import me.fixeddev.ebcm.bukkit.BukkitMessenger;
import me.fixeddev.ebcm.bukkit.parameter.provider.BukkitModule;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import me.fixeddev.ebcm.parametric.ParametricCommandBuilder;
import me.fixeddev.ebcm.parametric.ReflectionParametricCommandBuilder;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import perfect.teleport.commands.TPAAcceptCommand;
import perfect.teleport.commands.TPACommand;
import perfect.teleport.files.Files;
import perfect.teleport.listeners.PlayerJoinListener;
import perfect.teleport.listeners.PlayerQuitListener;
import perfect.teleport.managers.TPAManager;
import perfect.teleport.redis.RedisConnection;

public class RedisTeleport extends JavaPlugin {

    private RedisConnection redisConnection;
    private TPAManager tpaManager;
    private Files lang;
    private Files config;
    private String prefix;

    public void onEnable(){
        redisConnection = new RedisConnection(this, "", "201.145.10.255", 6379);
        redisConnection.redisConnect();
        redisConnection.subscribeChannel("TPA");

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        config = new Files(this, "config");
        lang = new Files(this, "lang");
        prefix = ChatColor.translateAlternateColorCodes('&', lang.getString("Messages.Prefix"));

        tpaManager = new TPAManager();

        registerEvents();
        registerCommands();
    }

    private void registerEvents(){
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);
    }

    private void registerCommands(){
        ParametricCommandBuilder builder = new ReflectionParametricCommandBuilder();
        Authorizer authorizer = new BukkitAuthorizer();
        ParameterProviderRegistry providerRegistry = ParameterProviderRegistry.createRegistry();
        Messenger message = new BukkitMessenger();
        CommandManager commandManager = new SimpleCommandManager(authorizer, message, providerRegistry);
        providerRegistry.installModule(new BukkitModule());

        BukkitCommandManager bukkitCommandManager = new BukkitCommandManager(commandManager, this.getName());

        bukkitCommandManager.registerCommands(builder.fromClass(new TPACommand(this)));
        bukkitCommandManager.registerCommands(builder.fromClass(new TPAAcceptCommand(this)));
    }

    public TPAManager getTpaManager() {
        return tpaManager;
    }

    public Files getLang() {
        return lang;
    }

    public Files getConfig() {
        return config;
    }

    public RedisConnection getRedisConnection() {
        return redisConnection;
    }

    public String getPrefix() {
        return prefix;
    }
}