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

package admincommands;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_NPC_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerVisualStateService;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Divinity
 */
public class Invis extends AdminCommand {

	public Invis() {
		super("invis");
	}

	@Override
	public void execute(final Player player, String... params) {
		if (player.getVisualState() < 3) {
			final Summon summon = player.getSummon();
			player.getEffectController().setAbnormal(AbnormalState.HIDE.getId());
			player.setVisualState(CreatureVisualState.HIDE20);
			if (summon != null && summon.isSpawned()) {
				summon.getEffectController().setAbnormal(AbnormalState.HIDE.getId());
				summon.setVisualState(CreatureVisualState.HIDE20);
				PacketSendUtility.broadcastPacketAndReceive(player, new SM_NPC_INFO(summon, player));
				AttackUtil.cancelCastOn(summon);
				summon.getEffectController().sendEffectIconsTo(player);
			}
			
			AttackUtil.cancelCastOn(player);
			
			PacketSendUtility.broadcastPacket(player, new SM_PLAYER_STATE(player), true);
			ThreadPoolManager.getInstance().schedule(new Runnable() {
				@Override
				public void run() {
					// do on all who targetting on 'effected' (set target null,
					// cancel attack skill, cancel npc pursuit)
					AttackUtil.removeTargetFrom(player, true);
					if (summon != null){
						AttackUtil.removeTargetFrom(summon, true);
					}
				}
			}, 500);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_EFFECT_INVISIBLE_BEGIN);
			if (SecurityConfig.INVIS) {
				PlayerVisualStateService.hideValidate(player);
			}
		} else {
			Summon summon = player.getSummon();
			player.getEffectController().unsetAbnormal(AbnormalState.HIDE.getId());
			player.unsetVisualState(CreatureVisualState.HIDE20);
			if (summon != null && summon.isSpawned()) {
				summon.getEffectController().unsetAbnormal(AbnormalState.HIDE.getId());
				summon.unsetVisualState(CreatureVisualState.HIDE20);
				PacketSendUtility.broadcastPacketAndReceive(player, new SM_NPC_INFO(summon, player));
				summon.getEffectController().sendEffectIconsTo(player);
			}
			PacketSendUtility.broadcastPacket(player, new SM_PLAYER_STATE(player), true);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_EFFECT_INVISIBLE_END);
			if (SecurityConfig.INVIS) {
				PlayerVisualStateService.hideValidate(player);
			}
		}
	}

	@Override
	public void onFail(Player player, String message) {
		// TODO Auto-generated method stub
	}
}
