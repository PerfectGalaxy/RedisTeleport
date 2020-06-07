package perfect.teleport.files;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Files extends YamlConfiguration {

    private final String fileName;
    private final JavaPlugin plugin;
    private final File folder;

    public Files(JavaPlugin plugin, String fileName, File folder) {
        this(plugin, fileName, ".yml", folder);
    }

    public Files(JavaPlugin plugin, String filename, String fileextension, File folder) {
        this.folder = folder;
        this.plugin = plugin;
        this.fileName = filename + (filename.endsWith(fileextension) ? "" : fileextension);
        this.createFile();
    }

    public Files(final JavaPlugin plugin, final String fileName) {
        this(plugin, fileName, ".yml");
    }

    public Files(final JavaPlugin plugin, final String fileName, final String fileExtension) {
        this(plugin, fileName, fileExtension, plugin.getDataFolder());
    }

    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public String getString(String path) {
        String getted;
        try {
            getted = super.getString(path);
        } catch (NullPointerException e) {
            getted = path;
        }
        return getted.replace('&', '§');
    }

    public List<String> getColouredStringList(String path) {
        List<String> f = new ArrayList<>();
        for (String l : super.getStringList(path)) {
            f.add(l.replace('&', '§'));
        }
        return f;
    }

    public <T> T get(Class<T> clazz, String path) {
        Object obj = super.get(path);
        return clazz.cast(obj);
    }

    private void createFile() {
        try {
            final File file = new File(folder, this.fileName);
            if (!file.exists()) {
                if (this.plugin.getResource(this.fileName) != null) {
                    this.plugin.saveResource(this.fileName, false);
                } else {
                    this.save(file);
                }
                this.load(file);
            } else {
                this.load(file);
                this.save(file);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void save() {
        final File folder = this.plugin.getDataFolder();
        try {
            this.save(new File(folder, this.fileName));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}