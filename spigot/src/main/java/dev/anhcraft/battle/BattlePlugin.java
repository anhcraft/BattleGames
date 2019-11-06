/*
 *
 *     Battle Minigame.
 *     Copyright (c) 2019 by anhcraft.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package dev.anhcraft.battle;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import dev.anhcraft.battle.api.*;
import dev.anhcraft.battle.api.game.ABTeam;
import dev.anhcraft.battle.api.game.Arena;
import dev.anhcraft.battle.api.game.LocalGame;
import dev.anhcraft.battle.api.game.Mode;
import dev.anhcraft.battle.api.gui.Gui;
import dev.anhcraft.battle.api.inventory.items.*;
import dev.anhcraft.battle.api.market.Market;
import dev.anhcraft.battle.api.misc.BattleEffect;
import dev.anhcraft.battle.api.misc.EffectOption;
import dev.anhcraft.battle.api.misc.Kit;
import dev.anhcraft.battle.api.misc.Perk;
import dev.anhcraft.battle.api.misc.info.*;
import dev.anhcraft.battle.api.storage.StorageType;
import dev.anhcraft.battle.api.storage.data.PlayerData;
import dev.anhcraft.battle.api.storage.data.ServerData;
import dev.anhcraft.battle.cmd.BattleCommand;
import dev.anhcraft.battle.gui.*;
import dev.anhcraft.battle.gui.handlers.CoreListener;
import dev.anhcraft.battle.gui.handlers.MainInventoryListener;
import dev.anhcraft.battle.gui.inventory.*;
import dev.anhcraft.battle.gui.market.CategoryMenu;
import dev.anhcraft.battle.gui.market.ProductMenu;
import dev.anhcraft.battle.system.handlers.GrenadeHandler;
import dev.anhcraft.battle.system.handlers.GunHandler;
import dev.anhcraft.battle.system.handlers.Handler;
import dev.anhcraft.battle.system.integrations.PapiExpansion;
import dev.anhcraft.battle.system.integrations.SWMIntegration;
import dev.anhcraft.battle.system.integrations.VaultApi;
import dev.anhcraft.battle.system.listeners.BlockListener;
import dev.anhcraft.battle.system.listeners.GameListener;
import dev.anhcraft.battle.system.listeners.PlayerListener;
import dev.anhcraft.battle.system.managers.*;
import dev.anhcraft.battle.system.messengers.BungeeMessenger;
import dev.anhcraft.battle.system.renderers.bossbar.BossbarRenderer;
import dev.anhcraft.battle.system.renderers.scoreboard.PlayerScoreboard;
import dev.anhcraft.battle.system.renderers.scoreboard.ScoreboardRenderer;
import dev.anhcraft.battle.tasks.*;
import dev.anhcraft.confighelper.ConfigHelper;
import dev.anhcraft.confighelper.exception.InvalidValueException;
import dev.anhcraft.craftkit.CraftExtension;
import dev.anhcraft.craftkit.cb_common.NMSVersion;
import dev.anhcraft.craftkit.helpers.TaskHelper;
import dev.anhcraft.craftkit.utils.ServerUtil;
import dev.anhcraft.jvmkit.helpers.HTTPConnectionHelper;
import dev.anhcraft.jvmkit.utils.*;
import net.md_5.bungee.api.ChatColor;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class BattlePlugin extends JavaPlugin implements BattleAPI {
    public static final long BOSSBAR_UPDATE_INTERVAL = 10;
    public static final long SCOREBOARD_UPDATE_INTERVAL = 10;
    private static final String[] CONFIG_FILES = new String[]{
            "system.yml",
            "general.yml",
            "_ locale/en_us.yml", // PUT DEFAULT LOCALE HERE
            "modes.yml",
            "arenas.yml",
            // START: ATTACHMENTS
            "items/ammo.yml",
            "items/magazines.yml",
            "items/scopes.yml",
            // END: ATTACHMENTS
            "items/guns.yml",
            "items/grenades.yml",
            "items/items.yml",
            "gui.yml " + (NMSVersion.current().compare(NMSVersion.v1_13_R1) >= 0 ? "gui.yml" : "gui.legacy.yml"),
            "kits.yml",
            "perks.yml",
            "market.yml"
    };
    private static final FileConfiguration[] CONFIG = new FileConfiguration[CONFIG_FILES.length];
    public final Map<OfflinePlayer, PlayerData> PLAYER_MAP = new ConcurrentHashMap<>();
    private final Map<String, Arena> ARENA_MAP = new HashMap<>();
    private final Map<String, AmmoModel>  AMMO_MAP = new HashMap<>();
    private final Map<String, GunModel> GUN_MAP = new HashMap<>();
    private final Map<String, GrenadeModel> GRENADE_MAP = new HashMap<>();
    private final Map<String, Kit> KIT_MAP = new HashMap<>();
    private final Map<String, Perk> PERK_MAP = new HashMap<>();
    private final Map<String, MagazineModel> MAGAZINE_MAP = new HashMap<>();
    private final Map<String, ScopeModel> SCOPE_MAP = new HashMap<>();
    private final Map<Class<? extends Handler>, Handler> HANDLERS = new HashMap<>();
    private final ServerData SERVER_DATA = new ServerData();
    private final Market MARKET = new Market();
    private File localeDir;
    public CraftExtension extension;
    public ChatManager chatManager;
    public TitleManager titleProvider;
    public GameManager gameManager;
    public DataManager dataManager;
    public TaskHelper taskHelper;
    public ItemManager itemManager;
    public GuiManager guiManager;
    public ScoreboardRenderer scoreboardRenderer;
    public BossbarRenderer bossbarRenderer;
    public BungeeMessenger bungeeMessenger;
    public GameTask gameTask;
    public QueueTitleTask queueTitleTask;
    public QueueServerTask queueServerTask;
    public EntityTrackingTask entityTracker;
    private Expression toExpConverter;
    private Expression toLevelConverter;
    private PapiExpansion papiExpansion;
    private SimpleDateFormat longFormDate;
    private SimpleDateFormat shortFormDate1;
    private SimpleDateFormat shortFormDate2;
    private SimpleDateFormat shortFormDate3;
    private boolean syncDataTaskNeed;
    private File configFolder = getDataFolder();
    private String remoteConfigUrl;
    private boolean spigotBungeeSupport;
    private boolean supportBungee;
    private List<String> lobbyList;
    public SWMIntegration SWMIntegration;
    private boolean slimeWorldManagerSupport;

    @Override
    public void onEnable() {
        try {
            Class<?> sc = Class.forName("org.spigotmc.SpigotConfig");
            spigotBungeeSupport = (boolean) ReflectionUtil.getStaticField(sc, "bungee");
        } catch (ClassNotFoundException e) {
            exit("ABM can only work on Spigot-based servers.");
            return;
        }
        if (!VaultApi.init()) exit("Failed to hook to Vault");
        getLogger().info("Consider to donate me if you think ABM is awesome <3");
        getDataFolder().mkdir();

        initConfigFiles();
        injectApiProvider();

        if(getServer().getPluginManager().isPluginEnabled("SlimeWorldManager")){
            slimeWorldManagerSupport = true;
            SWMIntegration = new SWMIntegration(this);
            getLogger().info("Hooked to SlimeWorldManager");
        }

        extension = CraftExtension.of(BattlePlugin.class);
        papiExpansion = new PapiExpansion(this);
        papiExpansion.register();
        taskHelper = new TaskHelper(this);
        chatManager = new ChatManager(this);
        titleProvider = new TitleManager(this);
        itemManager = new ItemManager(this);
        guiManager = new GuiManager(this);
        gameManager = new GameManager(this);
        HANDLERS.put(GunHandler.class, new GunHandler(this));
        HANDLERS.put(GrenadeHandler.class, new GrenadeHandler(this));

        initGeneral(CONFIG[1]);
        initLocale(CONFIG[2]);
        initMode(CONFIG[3]);
        initArena(CONFIG[4]);
        initAmmo(CONFIG[5]);
        initMagazine(CONFIG[6]);
        initScope(CONFIG[7]);
        initGun(CONFIG[8]);
        initGrenade(CONFIG[9]);
        //initItem(CONFIG[10]);
        initGui(CONFIG[11]);
        initKits(CONFIG[12]);
        initPerks(CONFIG[13]);
        initMarket(CONFIG[14]);

        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        PlayerListener pl = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(pl, this);
        getServer().getOnlinePlayers().forEach(pl::handleJoin);

        taskHelper.newAsyncTimerTask(scoreboardRenderer = new ScoreboardRenderer(), 0, SCOREBOARD_UPDATE_INTERVAL);
        taskHelper.newAsyncTimerTask(bossbarRenderer = new BossbarRenderer(), 0, BOSSBAR_UPDATE_INTERVAL);
        taskHelper.newAsyncTimerTask(new DataSavingTask(this), 0, 60);
        if(syncDataTaskNeed)
            taskHelper.newAsyncTimerTask(new DataLoadingTask(this), 0, 60);
        taskHelper.newAsyncTimerTask(queueTitleTask = new QueueTitleTask(), 0, 20);
        taskHelper.newTimerTask(gameTask = new GameTask(this), 0, 1);
        taskHelper.newAsyncTimerTask(entityTracker = new EntityTrackingTask(), 0, 10);
        if(supportBungee){
            taskHelper.newAsyncTimerTask(queueServerTask = new QueueServerTask(this), 0, 20);
            getServer().getMessenger().registerIncomingPluginChannel(this, BungeeMessenger.BATTLE_CHANNEL, bungeeMessenger = new BungeeMessenger(this));
            getServer().getMessenger().registerOutgoingPluginChannel(this, BungeeMessenger.BATTLE_CHANNEL);
        }

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.registerCommand(new BattleCommand(this));
        manager.getCommandCompletions().registerAsyncCompletion("ammo", new CommandCompletions.AsyncCommandCompletionHandler<BukkitCommandCompletionContext>() {
            @Override
            public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
                return AMMO_MAP.keySet();
            }
        });
        manager.getCommandCompletions().registerAsyncCompletion("gun", new CommandCompletions.AsyncCommandCompletionHandler<BukkitCommandCompletionContext>() {
            @Override
            public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
                return GUN_MAP.keySet();
            }
        });
        manager.getCommandCompletions().registerAsyncCompletion("magazine", new CommandCompletions.AsyncCommandCompletionHandler<BukkitCommandCompletionContext>() {
            @Override
            public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
                return MAGAZINE_MAP.keySet();
            }
        });
        manager.getCommandCompletions().registerAsyncCompletion("scope", new CommandCompletions.AsyncCommandCompletionHandler<BukkitCommandCompletionContext>() {
            @Override
            public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
                return SCOPE_MAP.keySet();
            }
        });
        manager.getCommandCompletions().registerAsyncCompletion("arena", new CommandCompletions.AsyncCommandCompletionHandler<BukkitCommandCompletionContext>() {
            @Override
            public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
                return ARENA_MAP.keySet();
            }
        });
        manager.getCommandCompletions().registerAsyncCompletion("grenade", new CommandCompletions.AsyncCommandCompletionHandler<BukkitCommandCompletionContext>() {
            @Override
            public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
                return GRENADE_MAP.keySet();
            }
        });
        manager.getCommandCompletions().registerAsyncCompletion("perk", new CommandCompletions.AsyncCommandCompletionHandler<BukkitCommandCompletionContext>() {
            @Override
            public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
                return PERK_MAP.keySet();
            }
        });
    }

    private void injectApiProvider() {
        ApiProvider ap = new ApiProvider();
        ap.set(this);
        try {
            Class<?> clazz = ApiProvider.class;
            Field pf = clazz.getDeclaredField("provider");
            pf.setAccessible(true);
            pf.set(null, ap);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable(){
        gameManager.listGames(game -> {
            if(game instanceof LocalGame) {
                ((LocalGame) game).getPlayers().values().forEach(player -> {
                    if (player.getBackupInventory() != null)
                        player.toBukkit().getInventory().setContents(player.getBackupInventory());
                });
            }
        });
        dataManager.saveServerData();
        PLAYER_MAP.keySet().forEach(dataManager::savePlayerData);
        ServerUtil.getAllEntities(entity -> {
            if(entity instanceof HumanEntity) return;
            if(entity.hasMetadata("abm_temp_entity")) entity.remove();
        });
        dataManager.destroy();
        CraftExtension.unregister(BattlePlugin.class);
    }

    public FileConfiguration getSystemConf(){
        return CONFIG[0];
    }

    public FileConfiguration getGeneralConf(){
        return CONFIG[1];
    }

    public FileConfiguration getLocaleConf(){
        return CONFIG[2];
    }

    public FileConfiguration getModeConf(){
        return CONFIG[3];
    }

    public FileConfiguration getArenaConf(){
        return CONFIG[4];
    }

    public FileConfiguration getAmmoConf(){
        return CONFIG[5];
    }

    public FileConfiguration getMagazineConf(){
        return CONFIG[6];
    }

    public FileConfiguration getScopeConf(){
        return CONFIG[7];
    }

    public FileConfiguration getGunConf(){
        return CONFIG[8];
    }

    public FileConfiguration getGrenadeConf(){
        return CONFIG[9];
    }

    public FileConfiguration getItemConf(){
        return CONFIG[10];
    }

    public FileConfiguration getGuiConf(){
        return CONFIG[11];
    }

    public FileConfiguration getKitConf(){
        return CONFIG[12];
    }

    public FileConfiguration getPerkConf(){
        return CONFIG[13];
    }

    public FileConfiguration getMarketConf(){
        return CONFIG[14];
    }

    private YamlConfiguration loadConfigFile(String fp, String cp){
        if(remoteConfigUrl != null){
            String url = String.format(remoteConfigUrl, fp);
            getLogger().info("Downloading config from "+url);
            HTTPConnectionHelper c = new HTTPConnectionHelper(url)
                    .setProperty("User-Agent", HTTPConnectionHelper.USER_AGENT_CHROME)
                    .connect();
            byte[] bytes = c.read();
            try {
                c.getInput().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Reader reader = new StringReader(new String(bytes, StandardCharsets.UTF_8));
            return YamlConfiguration.loadConfiguration(reader);
        }
        File f = new File(configFolder, fp);
        getLogger().info("Loading config file from "+f.getAbsolutePath());
        try {
            if(f.exists()) return YamlConfiguration.loadConfiguration(f);
            else if(f.createNewFile()) {
                getLogger().info("Creating new file at "+f.getAbsolutePath());
                InputStream in = getResource("config/"+cp);
                byte[] bytes = IOUtil.toByteArray(in, FileUtil.DEFAULT_BUFF_SIZE);
                in.close();
                FileUtil.write(f, bytes);
                Reader reader = new StringReader(new String(bytes, StandardCharsets.UTF_8));
                return YamlConfiguration.loadConfiguration(reader);
            } else exit("Failed to create file: "+f.getAbsolutePath());
        } catch (IOException e) {
            exit("Failed to load file: "+f.getAbsolutePath());
            e.printStackTrace();
        }
        return null;
    }

    private void initConfigFiles(){
        for(int i = 0; i < CONFIG_FILES.length; i++){
            String[] s = CONFIG_FILES[i].split(" ");
            String fp = s[0];
            String cp = s[0];
            if(s.length == 2) {
                cp = s[1];
                if(fp.equals("_")) {
                    CONFIG[i] = YamlConfiguration.loadConfiguration(getTextResource("config/"+cp));
                    continue;
                }
            }
            CONFIG[i] = loadConfigFile(fp, cp);
            if(i == 0) initSystem(CONFIG[0]);
        }
    }

    private void initSystem(FileConfiguration c) {
        if(c.getString("plugin_version").chars().sum() < "1.1.4".chars().sum()){
            getLogger().warning("ATTENTION! It looks like you have updated the plugin from an older version!");
            getLogger().warning("You should be noticed that the new version will have massive changes to the configuration");
            getLogger().warning("Therefore, it is recommended to upgrade your config manually with the following steps:");
            getLogger().warning("1. Backup all the config files");
            getLogger().warning("2. Remove the entire Battle folder");
            getLogger().warning("3. Check out the new files");
            getLogger().warning("4. Compare with the old files");
            getLogger().warning("5. Re-configure");
            getLogger().warning("If you need help, contact me via Discord: https://discord.gg/QSpc5xH");
        }
        boolean remoteConfig = c.getBoolean("remote_config.enabled");
        if(remoteConfig){
            remoteConfigUrl = c.getString("remote_config.url");
            if(remoteConfigUrl == null) getLogger().warning("Remove config url is not defined");
            else return;
        }
        String cf = c.getString("config_folder");
        if(cf != null && !cf.isEmpty()){
            File file = new File(cf);
            if(file.exists()){
                if(file.isDirectory()) {
                    configFolder = file;
                    getLogger().info("Now using defined config folder: "+file.getAbsoluteFile());
                }
                else getLogger().warning("Config folder is not an directory");
            } else file.mkdir();
        }
        new File(configFolder, "locale").mkdir();
        new File(configFolder, "items").mkdir();
        // TODO check and upgrade db here
    }

    private void initGeneral(FileConfiguration c) {
        StorageType storageType = StorageType.valueOf(c.getString("storage.type").toUpperCase());
        dataManager = new DataManager(this, storageType);
        switch (storageType){
            case FILE: {
                File f = new File(c.getString("storage.file.data_path"));
                f.mkdir();
                dataManager.initFileStorage(f);
                break;
            }
            case MYSQL: {
                String host = c.getString("storage.mysql.hostname");
                int port = c.getInt("storage.mysql.port");
                String database = c.getString("storage.mysql.database");
                String username = c.getString("storage.mysql.username");
                String password = c.getString("storage.mysql.password");
                ConfigurationSection dsp = c.getConfigurationSection("storage.mysql.datasource_properties");
                String url = new StringBuilder("jdbc:mysql://").append(host).append(':').append(port).append('/').append(database).toString();
                dataManager.initMySQLStorage(url, username, password, dsp);
                syncDataTaskNeed = true;
                break;
            }
        }
        dataManager.loadServerData();

        toLevelConverter = new ExpressionBuilder(CONFIG[1].getString("level_system.exp_to_level_formula")).variables("x").build();
        toExpConverter = new ExpressionBuilder(CONFIG[1].getString("level_system.level_to_exp_formula")).variables("x").build();
        longFormDate = new SimpleDateFormat(getGeneralConf().getString("date_format.long_form"));
        shortFormDate1 = new SimpleDateFormat(getGeneralConf().getString("date_format.short_form.hours"));
        shortFormDate2 = new SimpleDateFormat(getGeneralConf().getString("date_format.short_form.minutes"));
        shortFormDate3 = new SimpleDateFormat(getGeneralConf().getString("date_format.short_form.seconds"));

        if(getGeneralConf().getBoolean("bungeecord.enabled")){
            if(spigotBungeeSupport) {
                supportBungee = true;
                lobbyList = Collections.unmodifiableList(getGeneralConf().getStringList("bungeecord.lobby_servers"));
            }
            else getLogger().warning("Looks like you have enabled Bungeecord support. But please also enable it in spigot.yml too. The option is now skipped for safe!");
        }
    }

    private void initLocale(FileConfiguration cache) {
        String path = getGeneralConf().getString("locale");
        YamlConfiguration local = loadConfigFile("locale/"+path, "locale/"+path);
        if(local != null) {
            boolean outdatedLocale = false;
            Set<String> keys = cache.getKeys(true);
            for (String k : keys) {
                Object v;
                if (local.contains(k)) v = local.get(k);
                else {
                    getLogger().warning("The locale file is outdated. Missing path: " + k);
                    v = cache.get(k);
                    local.set(k, v);
                    outdatedLocale = true;
                }
                if (v instanceof String)
                    cache.set(k, ChatColor.translateAlternateColorCodes('&', (String) v));
                else cache.set(k, v);
            }
            if(outdatedLocale){
                File lc = new File(configFolder, "locale/temp."+path);
                try {
                    cache.save(lc);
                    getLogger().info("An up-to-date locale file that filled all missing entries was saved to "+lc.getAbsolutePath()+"!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else getLogger().warning("Locale file not found.");

        ConfigurationSection itemTypeSec = cache.getConfigurationSection("item_type");
        if(itemTypeSec != null){
            for(ItemType t : ItemType.values()){
                String n = itemTypeSec.getString(t.name().toLowerCase());
                if(n != null) t.setLocalizedName(n);
            }
        }

        ABTeam.TEAM_A.setLocalizedName(cache.getString("ab_team.team_a"));
        ABTeam.TEAM_B.setLocalizedName(cache.getString("ab_team.team_b"));
    }

    private void initMode(FileConfiguration c) {
        c.getKeys(false).forEach(s -> {
            Mode.getMode(s, m -> m.init(c.getConfigurationSection(s)));
        });
    }

    private void initArena(FileConfiguration c) {
        c.getKeys(false).forEach(s -> {
            Arena arena = new Arena(s);
            ConfigurationSection cs = c.getConfigurationSection(s);
            try {
                ConfigHelper.readConfig(cs, Arena.SCHEMA, arena);
            } catch (InvalidValueException e) {
                e.printStackTrace();
            }
            ARENA_MAP.put(s, arena);
        });
    }

    private void initAmmo(FileConfiguration c) {
        c.getKeys(false).forEach(s -> {
            AmmoModel a = new AmmoModel(s);
            ConfigurationSection cs = c.getConfigurationSection(s);
            try {
                ConfigHelper.readConfig(cs, AmmoModel.SCHEMA, a);
            } catch (InvalidValueException e) {
                e.printStackTrace();
            }
            AMMO_MAP.put(s, a);
        });
    }

    private void initMagazine(FileConfiguration c) {
        c.getKeys(false).forEach(s -> {
            MagazineModel m = new MagazineModel(s);
            ConfigurationSection cs = c.getConfigurationSection(s);
            try {
                ConfigHelper.readConfig(cs, MagazineModel.SCHEMA, m);
            } catch (InvalidValueException e) {
                e.printStackTrace();
            }
            MAGAZINE_MAP.put(s, m);
        });
    }

    private void initGun(FileConfiguration c) {
        c.getKeys(false).forEach(s -> {
            GunModel g = new GunModel(s);
            ConfigurationSection cs = c.getConfigurationSection(s);
            try {
                ConfigHelper.readConfig(cs, GunModel.SCHEMA, g);
            } catch (InvalidValueException e) {
                e.printStackTrace();
            }
            GUN_MAP.put(s, g);
        });
    }

    private void initGrenade(FileConfiguration c) {
        c.getKeys(false).forEach(s -> {
            GrenadeModel g = new GrenadeModel(s);
            ConfigurationSection cs = c.getConfigurationSection(s);
            try {
                ConfigHelper.readConfig(cs, GrenadeModel.SCHEMA, g);
            } catch (InvalidValueException e) {
                e.printStackTrace();
            }
            GRENADE_MAP.put(s, g);
        });
    }

    private void initScope(FileConfiguration c) {
        c.getKeys(false).forEach(s -> {
            ScopeModel sm = new ScopeModel(s);
            ConfigurationSection cs = c.getConfigurationSection(s);
            try {
                ConfigHelper.readConfig(cs, ScopeModel.SCHEMA, sm);
            } catch (InvalidValueException e) {
                e.printStackTrace();
            }
            SCOPE_MAP.put(s, sm);
        });
    }

    private void initGui(FileConfiguration c) {
        guiManager.registerGuiHandler("core", new CoreListener());
        guiManager.registerGuiHandler("inventory_menu", new MainInventoryListener());
        guiManager.registerGuiHandler("inventory_gun", new GunInventory());
        guiManager.registerGuiHandler("inventory_magazine", new MagazineInventory());
        guiManager.registerGuiHandler("inventory_ammo", new AmmoInventory());
        guiManager.registerGuiHandler("inventory_scope", new ScopeInventory());
        guiManager.registerGuiHandler("inventory_grenade", new GrenadeInventory());
        guiManager.registerGuiHandler("kit_menu", new KitMenu());
        guiManager.registerGuiHandler("arena_chooser", new ArenaChooser());
        guiManager.registerGuiHandler("market_category_menu", new CategoryMenu());
        guiManager.registerGuiHandler("market_product_menu", new ProductMenu());

        c.getKeys(false).forEach(s -> {
            if(s.length() > 0 && s.charAt(0) != '$')
                guiManager.registerGui(s, new Gui(c.getConfigurationSection(s)));
        });
    }

    private void initKits(FileConfiguration c) {
        c.getKeys(false).forEach(s -> {
            Kit kit = new Kit(s);
            ConfigurationSection cs = c.getConfigurationSection(s);
            try {
                ConfigHelper.readConfig(cs, Kit.SCHEMA, kit);
            } catch (InvalidValueException e) {
                e.printStackTrace();
            }
            KIT_MAP.put(s, kit);
        });
    }

    private void initPerks(FileConfiguration c) {
        c.getKeys(false).forEach(s -> {
            Perk perk = new Perk(s);
            ConfigurationSection cs = c.getConfigurationSection(s);
            try {
                ConfigHelper.readConfig(cs, Perk.SCHEMA, perk);
            } catch (InvalidValueException e) {
                e.printStackTrace();
            }
            PERK_MAP.put(s, perk);
        });
    }

    private void initMarket(FileConfiguration c) {
        try {
            ConfigHelper.readConfig(c, Market.SCHEMA, MARKET);
        } catch (InvalidValueException e) {
            e.printStackTrace();
        }
    }

    public void resetScoreboard(Player player) {
        if(getGeneralConf().getBoolean("default_scoreboard.enabled")) {
            String title = getGeneralConf().getString("default_scoreboard.title");
            List<String> content = getGeneralConf().getStringList("default_scoreboard.content");
            int len = getGeneralConf().getInt("default_scoreboard.fixed_length");
            scoreboardRenderer.setScoreboard(new PlayerScoreboard(player, title, content, len));
        } else scoreboardRenderer.removeScoreboard(player);
    }

    @Override
    @NotNull
    public Map<String, String> mapInfo(@NotNull InfoHolder holder){
        Condition.argNotNull("holder", holder);
        return holder.read().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    InfoData data = entry.getValue();
                    if(data instanceof InfoBooleanData){
                        if(((InfoBooleanData) data).getValue())
                            return getLocaleConf().getString("state.enabled");
                        else
                            return getLocaleConf().getString("state.disabled");
                    }
                    else if(data instanceof InfoDoubleData)
                        return MathUtil.formatRound(((InfoDoubleData) data).getValue());
                    else if(data instanceof InfoIntData)
                        return Integer.toString(((InfoIntData) data).getValue());
                    else if(data instanceof InfoLongData)
                        return Long.toString(((InfoLongData) data).getValue());
                    else if(data instanceof InfoStringData)
                        return ((InfoStringData) data).getValue();
                    return "Error! (data class="+data.getClass().getSimpleName()+")";
                }
        ));
    }

    public <T extends Handler> T getHandler(Class<T> clazz){
        return (T) HANDLERS.get(clazz);
    }

    public PapiExpansion getPapiExpansion() {
        return papiExpansion;
    }

    @NotNull
    @Override
    public String formatLongFormDate(@NotNull Date date){
        Condition.argNotNull("date", date);
        return longFormDate.format(date);
    }

    @NotNull
    @Override
    public String formatShortFormTime(long time){
        final long x = 1000;
        if(time < 60 * x) return formatShortFormDateSeconds(new Date(time));
        else if(time < 60 * 60 * x) return formatShortFormDateMinutes(new Date(time));
        else return formatShortFormDateHours(new Date(time));
    }

    @NotNull
    @Override
    public String formatShortFormDateHours(@NotNull Date date){
        Condition.argNotNull("date", date);
        return shortFormDate1.format(date);
    }

    @NotNull
    @Override
    public String formatShortFormDateMinutes(@NotNull Date date){
        Condition.argNotNull("date", date);
        return shortFormDate2.format(date);
    }

    @NotNull
    @Override
    public String formatShortFormDateSeconds(@NotNull Date date){
        Condition.argNotNull("date", date);
        return shortFormDate3.format(date);
    }

    @Override
    public float getDefaultWalkingSpeed() {
        return (float) getGeneralConf().getDouble("misc.default_speed.walk", 0.2);
    }

    @Override
    public float getDefaultFlyingSpeed() {
        return (float) getGeneralConf().getDouble("misc.default_speed.fly", 0.2);
    }

    @Override
    public long calculateExp(int level) {
        return (long) toExpConverter.setVariable("x", level).evaluate();
    }

    @Override
    public int calculateLevel(long exp) {
        return (int) toLevelConverter.setVariable("x", exp).evaluate();
    }

    @Override
    @Nullable
    public PlayerData getPlayerData(@Nullable OfflinePlayer player) {
        return PLAYER_MAP.get(player);
    }

    @NotNull
    @Override
    public ServerData getServerData() {
        return SERVER_DATA;
    }

    @Override
    public Arena getArena(@Nullable String id) {
        return ARENA_MAP.get(id);
    }

    @Override
    public AmmoModel getAmmoModel(@Nullable String id) {
        return AMMO_MAP.get(id);
    }

    @Override
    public GunModel getGunModel(@Nullable String id) {
        return GUN_MAP.get(id);
    }

    @Override
    public MagazineModel getMagazineModel(@Nullable String id) {
        return MAGAZINE_MAP.get(id);
    }

    @Override
    public ScopeModel getScopeModel(@Nullable String id) {
        return SCOPE_MAP.get(id);
    }

    @Override
    public GrenadeModel getGrenadeModel(@Nullable String id) {
        return GRENADE_MAP.get(id);
    }

    @Override
    public Kit getKit(@Nullable String id) {
        return KIT_MAP.get(id);
    }

    @Override
    public @Nullable Perk getPerk(@Nullable String id) {
        return PERK_MAP.get(id);
    }

    @NotNull
    @Override
    public List<Arena> listArenas() {
        return ImmutableList.copyOf(ARENA_MAP.values());
    }

    @Override
    public void listArenas(@NotNull Consumer<Arena> consumer) {
        Condition.argNotNull("consumer", consumer);
        ARENA_MAP.values().forEach(consumer);
    }

    @NotNull
    @Override
    public List<AmmoModel> listAmmoModels() {
        return ImmutableList.copyOf(AMMO_MAP.values());
    }

    @Override
    public void listAmmoModels(@NotNull Consumer<AmmoModel> consumer) {
        Condition.argNotNull("consumer", consumer);
        AMMO_MAP.values().forEach(consumer);
    }

    @NotNull
    @Override
    public List<GunModel> listGunModels() {
        return ImmutableList.copyOf(GUN_MAP.values());
    }

    @Override
    public void listGunModels(@NotNull Consumer<GunModel> consumer) {
        Condition.argNotNull("consumer", consumer);
        GUN_MAP.values().forEach(consumer);
    }

    @NotNull
    @Override
    public List<MagazineModel> listMagazineModels() {
        return ImmutableList.copyOf(MAGAZINE_MAP.values());
    }

    @Override
    public void listMagazineModels(@NotNull Consumer<MagazineModel> consumer) {
        Condition.argNotNull("consumer", consumer);
        MAGAZINE_MAP.values().forEach(consumer);
    }

    @Override
    public @NotNull List<ScopeModel> listScopes() {
        return ImmutableList.copyOf(SCOPE_MAP.values());
    }

    @Override
    public void listScopes(@NotNull Consumer<ScopeModel> consumer) {
        Condition.argNotNull("consumer", consumer);
        SCOPE_MAP.values().forEach(consumer);
    }

    @Override
    @NotNull
    public List<GrenadeModel> listGrenades() {
        return ImmutableList.copyOf(GRENADE_MAP.values());
    }

    @Override
    public void listGrenades(@NotNull Consumer<GrenadeModel> consumer) {
        Condition.argNotNull("consumer", consumer);
        GRENADE_MAP.values().forEach(consumer);
    }

    @NotNull
    @Override
    public List<Kit> listKits() {
        return ImmutableList.copyOf(KIT_MAP.values());
    }

    @Override
    public void listKits(@NotNull Consumer<Kit> consumer) {
        Condition.argNotNull("consumer", consumer);
        KIT_MAP.values().forEach(consumer);
    }

    @Override
    @NotNull
    public List<Perk> listPerks() {
        return ImmutableList.copyOf(PERK_MAP.values());
    }

    @Override
    public void listPerks(@NotNull Consumer<Perk> consumer) {
        Condition.argNotNull("consumer", consumer);
        PERK_MAP.values().forEach(consumer);
    }

    @Override
    public @NotNull BattleGameManager getGameManager() {
        return gameManager;
    }

    @Override
    public @NotNull BattleItemManager getItemManager() {
        return itemManager;
    }

    @Override
    public @NotNull BattleGuiManager getGuiManager() {
        return guiManager;
    }

    @Override
    public @NotNull BattleChatManager getChatManager() {
        return chatManager;
    }

    @Override
    public boolean hasBungeecordSupport() {
        return supportBungee;
    }

    @Override
    @NotNull
    public List<String> getLobbyServers() {
        return lobbyList;
    }

    @Override
    public int getMaxReconnectionTries() {
        return getGeneralConf().getInt("bungeecord.reconnect_tries_per_server");
    }

    @Override
    public long getConnectionDelay() {
        return getGeneralConf().getLong("bungeecord.connection_delay");
    }

    @Override
    public boolean hasSlimeWorldManagerSupport() {
        return slimeWorldManagerSupport;
    }

    @Override
    public void playEffect(@NotNull Location location, @NotNull BattleEffect effect) {
        Number delayTime = ((Number) effect.getOptions().getOrDefault(EffectOption.REPEAT_DELAY, 0));
        int maxRepeat = (int) effect.getOptions().getOrDefault(EffectOption.REPEAT_TIMES, 0);
        BiConsumer<Location, BattleEffect> consumer = effect.getType().getEffectConsumer();
        AtomicInteger id = new AtomicInteger();
        id.set(taskHelper.newAsyncTimerTask(new Runnable() {
            private int counter;

            @Override
            public void run() {
                if(counter++ == maxRepeat){
                    taskHelper.cancelTask(id.get());
                    return;
                }
                consumer.accept(location, effect);
            }
        }, 0, delayTime.longValue()));
    }

    @Override
    public @NotNull Market getMarket() {
        return MARKET;
    }

    private void exit(String msg) {
        getLogger().warning("Plugin is now shutting down...");
        getLogger().info("Reason: "+msg);
        getServer().getPluginManager().disablePlugin(this);
    }
}