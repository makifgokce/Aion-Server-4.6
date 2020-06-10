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

package com.aionemu.gameserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.templates.CubeExpandTemplate;
import com.aionemu.gameserver.model.templates.ExpandType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUBE_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 * @author Simple
 * @reworked Luzien
 */
public class CubeExpandService {

	private static final Logger log = LoggerFactory.getLogger(CubeExpandService.class);
	private static final int MIN_EXPAND = 0;
	private static final int MAX_EXPAND = CustomConfig.BASIC_CUBE_SIZE_LIMIT;

	/**
	 * Shows Question window and expands on positive response
	 *
	 * @param player
	 * @param npc
	 */
	public static void expandCube(final Player player, Npc npc) {
		final CubeExpandTemplate expandTemplate = DataManager.CUBEEXPANDER_DATA.getCubeExpandListTemplate(npc.getNpcId());

		if (expandTemplate == null) {
			log.error("Cube Expand Template could not be found for Npc ID: " + npc.getObjectId());
			return;
		}

		if (npcCanExpandLevel(expandTemplate, player.getNpcExpands() + 1) && canExpand(player)) {
			/**
			 * Check if player is allowed to expand by buying
			 */
			if (player.getNpcExpands() >= CustomConfig.NPC_CUBE_EXPANDS_SIZE_LIMIT) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EXTEND_INVENTORY_CANT_EXTEND_MORE);
				return;
			}
			/**
			 * Check if our player can pay the cubic expand price
			 */
			final int price = getPriceByLevel(expandTemplate, player.getNpcExpands() + 1);

			RequestResponseHandler responseHandler = new RequestResponseHandler(npc) {
				@Override
				public void acceptRequest(Creature requester, Player responder) {
					if (price > player.getInventory().getKinah()) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_WAREHOUSE_EXPAND_NOT_ENOUGH_MONEY);
						return;
					}
					expand(responder, ExpandType.NPC);
					player.getInventory().decreaseKinah(price);
				}

				@Override
				public void denyRequest(Creature requester, Player responder) { // nothing to do
				}
			};

			boolean result = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_WAREHOUSE_EXPAND_WARNING, responseHandler);
			if (result) {
				PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_WAREHOUSE_EXPAND_WARNING, 0, 0, String.valueOf(price)));
			}
		} else {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300430));
		}
	}

	/**
	 * Expands the cubes
	 *
	 * @param player
	 * @param isNpcExpand
	 */
	public static void expand(Player player, ExpandType type) {
		if (!canExpand(player)) {
			return;
		}
		PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300431, "9")); // 9 Slots added
		switch (type) {
			case NPC:
				player.setNpcExpands(player.getNpcExpands() + 1);
				break;
			case QUEST:
				player.setQuestExpands(player.getQuestExpands() + 1);
				break;
			case ITEM:
				player.setItemExpands(player.getItemExpands() + 1);
				break;
		}
		PacketSendUtility.sendPacket(player, SM_CUBE_UPDATE.cubeSize(StorageType.CUBE, player));
	}

	public static boolean canExpandByTicket(Player player, int ticketLevel) {
		if (!canExpand(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EXTEND_INVENTORY_CANT_EXTEND_MORE);
			return false;
		}
		int cubeLevel = player.getNpcExpands() + player.getQuestExpands() + player.getItemExpands();
		ticketLevel *= 3;
		if(cubeLevel < ticketLevel) {
			return true;
		}else {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EXTEND_INVENTORY_CANT_EXTEND_MORE);
			return false;
		}
	}

	/**
	 * @param player
	 * @return
	 */
	public static boolean canExpand(Player player) {
		return validateNewSize(player.getNpcExpands() + player.getQuestExpands() + player.getItemExpands() + 1);
	}

	/**
	 * Checks if new player cube is not max
	 *
	 * @param level
	 * @return true or false
	 */
	private static boolean validateNewSize(int level) {
		// check min and max level
		return !(level < MIN_EXPAND || level > MAX_EXPAND);
	}

	/**
	 * Checks if npc can expand level
	 *
	 * @param clist
	 * @param level
	 * @return true or false
	 */
	private static boolean npcCanExpandLevel(CubeExpandTemplate clist, int level) {
		// check if level exists in template
		return clist.contains(level);
	}

	/**
	 * The guy who created cube template should blame himself :) One day I will
	 * rewrite them
	 *
	 * @param clist
	 * @param level
	 * @return
	 */
	private static int getPriceByLevel(CubeExpandTemplate clist, int level) {
		return clist.get(level).getPrice();
	}
}
