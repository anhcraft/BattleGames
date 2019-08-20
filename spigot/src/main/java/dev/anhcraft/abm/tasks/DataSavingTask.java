package dev.anhcraft.abm.tasks;

import dev.anhcraft.abm.BattlePlugin;
import dev.anhcraft.abm.BattleComponent;

public class DataSavingTask extends BattleComponent implements Runnable {
    public DataSavingTask(BattlePlugin plugin) {
        super(plugin);
    }

    @Override
    public void run() {
        plugin.dataManager.saveServerData();
        plugin.PLAYER_MAP.keySet().forEach(p -> plugin.dataManager.savePlayerData(p));
    }
}
