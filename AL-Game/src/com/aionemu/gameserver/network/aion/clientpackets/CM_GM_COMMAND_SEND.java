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

package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.UseableItemObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gm.GmPanelCommands;
import com.aionemu.gameserver.model.items.ItemCooldown;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.gmhandler.CmdAddSkill;
import com.aionemu.gameserver.network.aion.gmhandler.CmdChangeClass;
import com.aionemu.gameserver.network.aion.gmhandler.CmdCube;
import com.aionemu.gameserver.network.aion.gmhandler.CmdDeleteSkill;
import com.aionemu.gameserver.network.aion.gmhandler.CmdInvisible;
import com.aionemu.gameserver.network.aion.gmhandler.CmdLevelUpDown;
import com.aionemu.gameserver.network.aion.gmhandler.CmdLevelUpDown.LevelUpDownState;
import com.aionemu.gameserver.network.aion.gmhandler.CmdResurrect;
import com.aionemu.gameserver.network.aion.gmhandler.CmdVisible;
import com.aionemu.gameserver.network.aion.gmhandler.CmdWish;
import com.aionemu.gameserver.network.aion.gmhandler.CmdWishId;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_COOLDOWN;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_COOLDOWN;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.world.World;

/**
 * @author Magenik
 * @author Antraxx
 * @author Ever
 */
public class CM_GM_COMMAND_SEND extends AionClientPacket {

	private String cmd = "";
	private String params = "";
	private Player admin;
	private Player player;

	public CM_GM_COMMAND_SEND(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		admin = getConnection().getActivePlayer();
		String clientCmd = readS();

		int index = clientCmd.indexOf(" ");

		// System.out.println("GMCMD: " + clientCmd);
		// System.out.println("index: " + index);

		cmd = clientCmd;
		if (index >= 0) {
			cmd = clientCmd.substring(0, index).toUpperCase(Locale.forLanguageTag("en"));
			params = clientCmd.substring(index + 1);
		}

		// System.out.println("cmd   : " + cmd);
		// System.out.println("params: " + params);
	}

	@Override
	protected void runImpl() {
		if (admin == null) {
			return;
		}

		// check accesslevel - not needed but to be sure
		if (admin.getAccessLevel() < AdminConfig.GM_PANEL) {
			return;
		}
		Player plyr = World.getInstance().findPlayer(Util.convertName(params));
		if(plyr != null) {
			player = plyr;
		} else if(admin.getTarget() instanceof Player && !admin.getTarget().equals(admin)) {
			player = (Player) admin.getTarget();
		}


		switch (GmPanelCommands.getValue(cmd)) {
			case REMOVE_SKILL_DELAY_ALL:
				if (params.equalsIgnoreCase("1")) {
					admin.setCoolDownZero(false);
				} else if(params.equalsIgnoreCase("0")) {
					admin.setCoolDownZero(true);
				}
				break;
			case ITEMCOOLTIME:
				List<Integer> delayIds = new ArrayList<>();
				if (admin.getSkillCoolDowns() != null) {
					long currentTime = System.currentTimeMillis();
					for (Entry<Integer, Long> en : admin.getSkillCoolDowns().entrySet()) {
						delayIds.add(en.getKey());
					}

					for (Integer delayId : delayIds) {
						admin.setSkillCoolDown(delayId, currentTime);
					}

					delayIds.clear();
					PacketSendUtility.sendPacket(admin, new SM_SKILL_COOLDOWN(admin.getSkillCoolDowns()));
				}

				if (admin.getItemCoolDowns() != null) {
					for (Entry<Integer, ItemCooldown> en : admin.getItemCoolDowns().entrySet()) {
						delayIds.add(en.getKey());
					}

					for (Integer delayId : delayIds) {
						admin.addItemCoolDown(delayId, 0, 0);
					}

					delayIds.clear();
					PacketSendUtility.sendPacket(admin, new SM_ITEM_COOLDOWN(admin.getItemCoolDowns()));
				}

				if (admin.getHouseRegistry() != null && admin.getHouseObjectCooldownList().getHouseObjectCooldowns().size() > 0) {
					Iterator<HouseObject<?>> iter = admin.getHouseRegistry().getObjects().iterator();
					while (iter.hasNext()) {
						HouseObject<?> obj = iter.next();
						if (obj instanceof UseableItemObject) {
							if (!admin.getHouseObjectCooldownList().isCanUseObject(obj.getObjectId())) {
								admin.getHouseObjectCooldownList().addHouseObjectCooldown(obj.getObjectId(), 0);
							}
						}
					}
				}

				break;
			case ATTRBONUS:
				//new CmdAttrBonus(admin, params);
				break;
			case TELEPORTTO:
				if(player != null) {
					TeleportService2.teleportTo(admin, player.getWorldId(), player.getX(), player.getY(), player.getZ());
				}
				break;
			case TELEPORT_TO_NAMED:
				// new CmdTeleportToNamed(admin, params);
				break;
			case RESURRECT:
				new CmdResurrect(admin, player);
				break;
			case INVISIBLE:
				new CmdInvisible(admin, "");
				break;
			case VISIBLE:
				new CmdVisible(admin, "");
				break;
			case LEVELDOWN:
				new CmdLevelUpDown(admin, params, LevelUpDownState.DOWN);
				break;
			case LEVELUP:
				new CmdLevelUpDown(admin, params, LevelUpDownState.UP);
				break;
			case WISHID:
				new CmdWishId(admin, params);
				break;
			case DELETECQUEST:
				//new CmdDeleteQuest(admin, params);
				break;
			case GIVETITLE:
				//new CmdGiveTitle(admin, params);
				Integer titleId = Integer.parseInt(params);
				if ((titleId > 272) || (titleId < 1)) {
					PacketSendUtility.sendMessage(admin, "title id " + titleId + " is invalid (must be between 1 and 272)");
				} else {
					if (admin != null) {
						if (!admin.getTitleList().addTitle(titleId, false, 0)) {
							PacketSendUtility.sendMessage(admin, "you can't add title #" + titleId + " to yourself");
						} else {
							PacketSendUtility.sendMessage(admin, "you added title #" + titleId);
						}
					}
				}
				break;
			case DELETE_ITEMS:
				PacketSendUtility.sendMessage(admin, "Invalid command: " + cmd.toString());
				break;
			case CHANGECLASS:
				new CmdChangeClass(admin, params);
				break;
			case CLASSUP:
				new CmdChangeClass(admin, params);
				break;
			case SETINVENTORYGROWTH:
				new CmdCube(admin, params);
				break;
            case ADDSKILL:
                new CmdAddSkill(admin, params);
                break;
            case WISH:
                new CmdWish(admin, params);
                break;
            case DELETESKILL:
                new CmdDeleteSkill(admin, params);
                break;
            case ENDQUEST:
                //new CmdDeleteQuest(admin, params);
                break;
            case FREEFLY:
                //PacketSendUtility.sendMessage(admin, "Free Fly Enabled!");
                break;
            case ADDQUEST:
                //new CmdAddQuest(admin, params);
                //break;
			case SKILLPOINT:
			case COMBINESKILL:
			case ENCHANT100:
				break;
			case SET_MAKEUP_BONUS:
				if(player != null) {
					player.getLifeStats().increaseHp(TYPE.HP, player.getLifeStats().getMaxHp() + 1);
					player.getLifeStats().increaseMp(TYPE.MP, player.getLifeStats().getMaxMp() + 1);
					player.getLifeStats().increaseFp(TYPE.FP, player.getLifeStats().getMaxFp() + 1);
				} else {
					admin.getLifeStats().increaseHp(TYPE.HP, admin.getLifeStats().getMaxHp() + 1);
					admin.getLifeStats().increaseMp(TYPE.MP, admin.getLifeStats().getMaxMp() + 1);
					admin.getLifeStats().increaseFp(TYPE.FP, admin.getLifeStats().getMaxFp() + 1);
				}
				break;
			case SET_VITALPOINT:
				if(player != null) {
					player.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.SPEC2);
					player.getCommonData().setDeathCount(0);
				} else {
					admin.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.SPEC2);
					admin.getCommonData().setDeathCount(0);
				}
				break;
			case CLEARUSERCOOLT:
				PacketSendUtility.sendMessage(admin, "Invalid command: " + cmd.toString());
				break;
			case PARTYRECALL:
				Player groupToMove = player;
				if (groupToMove == null) {
					return;
				}

				if (!groupToMove.isInGroup2()) {
					PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_PARTY_YOU_ARE_NOT_PARTY_MEMBER);
					return;
				}

				for (Player target : groupToMove.getPlayerGroup2().getMembers()) {
					if (target != admin) {
						TeleportService2.teleportTo(target, admin.getWorldId(), admin.getInstanceId(), admin.getX(), admin.getY(), admin.getZ(), admin.getHeading(), TeleportAnimation.BEAM_ANIMATION);
					}
				}
				break;
			default:
				PacketSendUtility.sendMessage(admin, "Invalid command: " + cmd.toString());
				break;
		}
	}
}
