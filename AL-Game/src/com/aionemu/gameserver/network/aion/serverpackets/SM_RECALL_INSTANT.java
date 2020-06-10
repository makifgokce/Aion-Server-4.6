/**
 * This file is part of Aion-Lightning <aion-lightning.org>.
 *
 *  Aion-Lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Aion-Lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details. *
 *  You should have received a copy of the GNU General Public License
 *  along with Aion-Lightning.
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Akif
 */
public class SM_RECALL_INSTANT extends AionServerPacket {

    private Player player;
    private int action, skillId, time;

    public SM_RECALL_INSTANT(Player player, int action, int skillId, int time) {
        this.player = player;
        this.action = action;
        this.skillId = skillId;
        this.time = time;
    }

    @Override
    protected void writeImpl(AionConnection client) {
    	writeC(action);
        writeS(player.getName());
        writeH(skillId);
        writeH(time);
    }
}
