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

package dev.anhcraft.battle.system;

import co.aikar.commands.PaperCommandManager;
import dev.anhcraft.battle.BattlePlugin;

public interface IPremiumModule {
    void onIntegrate(BattlePlugin plugin);

    void onInitSystem(BattlePlugin plugin);

    void onReloadConfig(BattlePlugin plugin);

    void onRegisterEvents(BattlePlugin plugin);

    void onRegisterTasks(BattlePlugin plugin);

    void onRegisterCommands(BattlePlugin plugin, PaperCommandManager commandManager);

    void onDisable(BattlePlugin plugin);
}
