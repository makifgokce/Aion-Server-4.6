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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.QuestStateList;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.templates.ExpandType;
import com.aionemu.gameserver.model.templates.WarehouseExpandTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_WAREHOUSE_INFO;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Simple
 */
public class WarehouseService {

	private static final Logger log = LoggerFactory.getLogger(WarehouseService.class);
	private static final int MIN_EXPAND = 0;
	private static final int MAX_EXPAND = 11;

	/**
	 * Shows Question window and expands on positive response
	 *
	 * @param player
	 * @param npc
	 */
	public static void expandWarehouse(final Player player, Npc npc) {
		final WarehouseExpandTemplate expandTemplate = DataManager.WAREHOUSEEXPANDER_DATA.getWarehouseExpandListTemplate(npc.getNpcId());

		if (expandTemplate == null) {
			log.error("Warehouse Expand Template could not be found for Npc ID: " + npc.getObjectTemplate().getTemplateId());
			return;
		}

		if (npcCanExpandLevel(expandTemplate, player.getWarehouseNpcExpands() + 1) && canExpand(player)) {
			if (player.getWarehouseNpcExpands() >= CustomConfig.NPC_CUBE_EXPANDS_SIZE_LIMIT) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EXTEND_CHAR_WAREHOUSE_CANT_EXTEND_MORE);
				return;
			}
			/**
			* Check if our player can pay the warehouse expand price
			*/
			final int price = getPriceByLevel(expandTemplate, player.getWarehouseNpcExpands() + 1);
			RequestResponseHandler responseHandler = new RequestResponseHandler(npc) {
				@Override
				public void acceptRequest(Creature requester, Player responder) {
					if (player.getInventory().getKinah() < price) {
						PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300831));
						return;
					}
					expand(responder, ExpandType.NPC);
					player.getInventory().decreaseKinah(price);
				}

				@Override
				public void denyRequest(Creature requester, Player responder) {
					// nothing to do
				}
			};

			boolean result = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_WAREHOUSE_EXPAND_WARNING, responseHandler);
			if (result) {
				PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_WAREHOUSE_EXPAND_WARNING, 0, 0, String.valueOf(price)));
			}
		} else {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300432));
		}
	}

	/**
	 * @param player
	 */
	public static void expand(Player player, ExpandType type) {
		if (!canExpand(player)) {
			return;
		}
		PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300433, "8")); // 8  Slots  added
		switch(type) {
			case NPC:
				player.setWarehouseNpcExpands(player.getWarehouseNpcExpands() + 1);
				break;
			case QUEST:
				player.setWarehouseQuestExpands(player.getWarehouseQuestExpands() + 1);
				break;
			case ITEM:
				player.setWarehouseItemExpands(player.getWarehouseItemExpands() + 1);
				break;
		}

		sendWarehouseInfo(player, false);
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
	 * @param player
	 * @return
	 */
	public static boolean canExpand(Player player) {
		return validateNewSize(player.getWarehouseNpcExpands() + player.getWarehouseItemExpands() + player.getWarehouseQuestExpands() + 1);
	}

	/**
	 * Checks if npc can expand level
	 *
	 * @param clist
	 * @param level
	 * @return true or false
	 */
	private static boolean npcCanExpandLevel(WarehouseExpandTemplate clist, int level) {
		// check if level exists in template
		return clist.contains(level);
	}

	/**
	 * The guy who created cube template should blame himself :) One day I will
	 * rewrite them
	 *
	 * @param template
	 * @param level
	 * @return
	 */
	private static int getPriceByLevel(WarehouseExpandTemplate clist, int level) {
		return clist.get(level).getPrice();
	}

	/**
	 * Sends correctly warehouse packets
	 *
	 * @param player
	 */
	public static void sendWarehouseInfo(Player player, boolean sendAccountWh) {
		List<Item> items = player.getStorage(StorageType.REGULAR_WAREHOUSE.getId()).getItems();

		int whSize = player.getWarehouseNpcExpands();
		int itemsSize = items.size();

		/**
		 * Regular warehouse
		 */
		boolean firstPacket = true;
		if (itemsSize != 0) {
			int index = 0;

			while (index + 10 < itemsSize) {
				PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_INFO(items.subList(index, index + 10), StorageType.REGULAR_WAREHOUSE.getId(), whSize,
						firstPacket, player));
				index += 10;
				firstPacket = false;
			}
			PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_INFO(items.subList(index, itemsSize), StorageType.REGULAR_WAREHOUSE.getId(), whSize,
					firstPacket, player));
		}

		PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_INFO(null, StorageType.REGULAR_WAREHOUSE.getId(), whSize, false, player));

		if (sendAccountWh) {
			/**
			 * Account warehouse
			 */
			PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_INFO(player.getStorage(StorageType.ACCOUNT_WAREHOUSE.getId()).getItemsWithKinah(),
					StorageType.ACCOUNT_WAREHOUSE.getId(), 0, true, player));
		}

		PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_INFO(null, StorageType.ACCOUNT_WAREHOUSE.getId(), 0, false, player));
	}

	private static int getCompletedCubeQuests(Player player) {
		int result = 0;
		QuestStateList qs = player.getQuestStateList();
		int[] questIds = { 1987, 2985};
		for (int q : questIds) {
			if (qs.getQuestState(q) != null && qs.getQuestState(q).getStatus().equals(QuestStatus.COMPLETE)) {
				result++;
			}
		}
		return result > 1 ? 1 : result;
	}
	public static boolean canExpandByTicket(Player player, int level) {
		if (!canExpand(player)) {
			return false;
		}
		int ticketExpands = player.getQuestExpands() - getCompletedCubeQuests(player);

		return ticketExpands < level;
	}
}
