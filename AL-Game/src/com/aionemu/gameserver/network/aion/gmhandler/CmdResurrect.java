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

package com.aionemu.gameserver.network.aion.gmhandler;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RESURRECT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Antraxx
 */
public final class CmdResurrect {

	private Player admin, player;

	public CmdResurrect(Player admin, Player player) {
		this.admin = admin;
		this.player = player;
		run();
	}

	public void run() {
		Player t = player != null ? player : admin;
		if (!t.getLifeStats().isAlreadyDead()) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID);
			return;
		}
		t.setPlayerResActivate(true);
		PacketSendUtility.sendPacket(t, new SM_RESURRECT(admin));
		t.setResPosState(true);
		t.setResPosX(admin.getX());
		t.setResPosY(admin.getY());
		t.setResPosZ(admin.getZ());
	}
}
