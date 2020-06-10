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

import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_NPC_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Antraxx
 */
public class CmdVisible extends AbstractGMHandler {

	public CmdVisible(Player admin, String params) {
		super(admin, params);
		run();
	}

	private void run() {
		Summon summon = admin.getSummon();
		admin.getEffectController().unsetAbnormal(AbnormalState.HIDE.getId());
		admin.unsetVisualState(CreatureVisualState.HIDE20);
		if (summon != null && summon.isSpawned()) {
			summon.getEffectController().unsetAbnormal(AbnormalState.HIDE.getId());
			summon.unsetVisualState(CreatureVisualState.HIDE20);
			PacketSendUtility.broadcastPacketAndReceive(admin, new SM_NPC_INFO(summon, admin));
			summon.getEffectController().sendEffectIconsTo(admin);
		}
		PacketSendUtility.broadcastPacket(admin, new SM_PLAYER_STATE(admin), true);
		PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_SKILL_EFFECT_INVISIBLE_END);
	}
}
