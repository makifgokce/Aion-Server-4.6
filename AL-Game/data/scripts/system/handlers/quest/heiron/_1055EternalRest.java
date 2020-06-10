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

package quest.heiron;

import static ch.lambdaj.Lambda.maxFrom;

import java.util.Collection;
import java.util.HashSet;

import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.drop.DropService;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Rhys2002
 */
public class _1055EternalRest extends QuestHandler {

	private final static int questId = 1055;
	private final static int[] npc_ids = { 204629, 204625, 204628, 204627, 204626, 204622, 700270 };

	public _1055EternalRest() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		for (int npc_id : npc_ids) {
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 1500, true);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc) {
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		}

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204629) {
				return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 204629) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 0) {
						return sendQuestDialog(env, 1011);
					} else if (var == 2) {
						return sendQuestDialog(env, 1693);
					}
				case SETPRO1:
					if (var == 0) {
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
				case SETPRO2:
					if (var == 1) {
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
					return false;
				default:
					break;
			}
		} else if (targetId == 204625) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 1) {
						return sendQuestDialog(env, 1352);
					} else if (var == 2) {
						return sendQuestDialog(env, 1693);
					} else if (var == 4) {
						return sendQuestDialog(env, 2375);
					}
				case CHECK_USER_HAS_QUEST_ITEM:
					if (QuestService.collectItemCheck(env, true)) {
						if (!giveQuestItem(env, 182201613, 1)) {
							return true;
						}
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						return sendQuestDialog(env, 10000);
					} else {
						return sendQuestDialog(env, 10001);
					}
				case SETPRO2:
					if (var == 1) {
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
				case SET_SUCCEED:
					if (var == 4) {
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
					return false;
				default:
					break;
			}
		} else if (targetId == 204628) {
					if (var == 2) {
						if (player.getInventory().getItemCountByItemId(182201609) == 1) {
							return closeDialogWindow(env);
						}
					}
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 2) {
						return sendQuestDialog(env, 1694);
					}
				case SETPRO3:
					if (var == 2) {
						if (player.getInventory().getItemCountByItemId(182201609) == 0) {
								VisibleObject target = player.getTarget();
								Creature creature = (Creature) target;
								dropQuestItems(player, creature);
								return true;
						}
					}
					return false;
				default:
					break;
			}
		} else if (targetId == 204627) {
					if (var == 2) {
						if (player.getInventory().getItemCountByItemId(182201610) == 1) {
							return closeDialogWindow(env);
						}
					}
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 2) {
						return sendQuestDialog(env, 1781);
					}
				case SETPRO3:
					if (var == 2) {
						if (player.getInventory().getItemCountByItemId(182201610) == 0) {
								VisibleObject target = player.getTarget();
								Creature creature = (Creature) target;
								dropQuestItems(player, creature);
								return true;
						}
					}
					return false;
				default:
					break;
			}
		} else if (targetId == 204626) {
					if (var == 2) {
						if (player.getInventory().getItemCountByItemId(182201611) == 1) {
							return closeDialogWindow(env);
						}
					}
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 2) {
						return sendQuestDialog(env, 1864);
					}
				case SETPRO3:
					if (var == 2) {
						if (player.getInventory().getItemCountByItemId(182201611) == 0) {
								VisibleObject target = player.getTarget();
								Creature creature = (Creature) target;
								dropQuestItems(player, creature);
								return true;
						}
					}
					return false;
				default:
					break;
			}
		} else if (targetId == 204622) {
						if (var == 2) {
							if (player.getInventory().getItemCountByItemId(182201612) == 1) {
								return true;
							}
						}
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 2) {
						return sendQuestDialog(env, 1949);
					}
				case SETPRO3:
					if (var == 2) {
						if (player.getInventory().getItemCountByItemId(182201612) == 0) {
								VisibleObject target = player.getTarget();
								Creature creature = (Creature) target;
								dropQuestItems(player, creature);
								return true;
						}
					}
					return false;
				default:
					break;
			}
		} else if (targetId == 700270) {
			if (env.getDialog() == DialogAction.USE_OBJECT) {
				return useQuestObject(env, 3, 4, false, 0, 0, 1, 182201613, 1); // 4
			}
		}
		return false;
	}

	private void dropQuestItems(Player player, Creature npc) {
		if (npc.isInState(CreatureState.DEAD)) {
			AuditLogger.info(player, "Attempted multiple Chest looting!");
			return;
		}

		npc.getController().onDie(player);
		Collection<Player> players = new HashSet<>();
		if (player.isInGroup2()) {
			for (Player member : player.getPlayerGroup2().getOnlineMembers()) {
				if (MathUtil.isIn3dRange(member, npc, GroupConfig.GROUP_MAX_DISTANCE)) {
					players.add(member);
				}
			}
		} else if (player.isInAlliance2()) {
			for (Player member : player.getPlayerAlliance2().getOnlineMembers()) {
				if (MathUtil.isIn3dRange(member, npc, GroupConfig.GROUP_MAX_DISTANCE)) {
					players.add(member);
				}
			}
		} else {
			players.add(player);
		}
		DropRegistrationService.getInstance().registerDrop((Npc) npc, player, maxFrom(players).getLevel(), players);
		DropService.getInstance().requestDropList(player, npc.getObjectId());
	}
}
