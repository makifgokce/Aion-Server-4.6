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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.FriendRequestListDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_BLOCK_RESPONSE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FRIEND_RESPONSE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MARK_FRIENDLIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SocialService;
import com.aionemu.gameserver.utils.Util;

/**
 * Received when a user tries to add someone as his friend
 *
 * @author Ben
 */
public class CM_FRIEND_REQUEST extends AionClientPacket {

	private String targetName;
	private int action;
	private int objectId;
	private PlayerCommonData pcd;

	private static Logger log = LoggerFactory.getLogger(CM_FRIEND_REQUEST.class);
	public CM_FRIEND_REQUEST(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		objectId = readD();
		targetName = Util.convertName(readS());
		action = readC();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {

		final Player activePlayer = getConnection().getActivePlayer();
		pcd = DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonDataByName(targetName);
		switch(action) {
			case 0:// Arkadaşlık isteğini kabul et
				if (targetName.equalsIgnoreCase(activePlayer.getName())) {
					// Adding self to friend list not allowed - Its blocked by the client by default, so no need to send an error
				} // if offline
				else if (pcd == null) {
					sendPacket(new SM_FRIEND_RESPONSE(targetName, SM_FRIEND_RESPONSE.TARGET_NOT_FOUND));
					return;
				}
				else if (activePlayer.getCommonData().getFriendList().getFriend(pcd.getPlayerObjId()) != null) {
					sendPacket(new SM_FRIEND_RESPONSE(targetName, SM_FRIEND_RESPONSE.TARGET_ALREADY_FRIEND));
				} else if (activePlayer.getCommonData().getFriendList().isFull()) {
					sendPacket(SM_SYSTEM_MESSAGE.STR_BUDDYLIST_LIST_FULL);
				} else if (activePlayer.getCommonData().getRace() != pcd.getRace()) {
					sendPacket(new SM_FRIEND_RESPONSE(targetName, SM_FRIEND_RESPONSE.TARGET_NOT_FOUND));
				} else if (pcd.getFriendList().isFull() && pcd != null) {
					sendPacket(new SM_FRIEND_RESPONSE(targetName, SM_FRIEND_RESPONSE.TARGET_LIST_FULL));
				} else if (activePlayer.getCommonData().getBlockList().contains(pcd.getPlayerObjId()) && pcd != null) {
					sendPacket(new SM_FRIEND_RESPONSE(targetName, SM_FRIEND_RESPONSE.TARGET_BLOCKED));
				} else if (pcd.getBlockList().contains(activePlayer.getObjectId()) && pcd != null) {
					sendPacket(SM_SYSTEM_MESSAGE.STR_YOU_EXCLUDED(targetName));
				} else if(pcd.getFriendRequestList().getFriendRequest(activePlayer.getObjectId()) != null && pcd != null) {
					sendPacket(new SM_SYSTEM_MESSAGE(1401500, targetName));
				} else // Send request
				{
					SocialService.makeFriends(activePlayer, pcd);
					activePlayer.getCommonData().getFriendRequestList().delFriend(objectId);
					DAOManager.getDAO(FriendRequestListDAO.class).delFriends(activePlayer.getObjectId(), objectId);
				}
				break;
			case 1:// Arkadaşlık isteğini reddet
				sendPacket(new SM_FRIEND_RESPONSE(targetName, SM_FRIEND_RESPONSE.TARGET_OFFLINE_DENIED));
				activePlayer.getCommonData().getFriendRequestList().delFriend(objectId);
				DAOManager.getDAO(FriendRequestListDAO.class).delFriends(activePlayer.getObjectId(), objectId);
				break;
			case 2:// Blokla
				// Trying to block self
				if (activePlayer.getName().equalsIgnoreCase(targetName)) {
					sendPacket(new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.CANT_BLOCK_SELF, targetName));
				} // List full
				else if (activePlayer.getCommonData().getBlockList().isFull()) {
					sendPacket(new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.LIST_FULL, targetName));
				} // Player offline
				else if (pcd == null) {
					sendPacket(new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.TARGET_NOT_FOUND, targetName));
				} // Player is your friend
				else if (activePlayer.getCommonData().getFriendList().getFriend(objectId) != null) {
					sendPacket(SM_SYSTEM_MESSAGE.STR_BLOCKLIST_NO_BUDDY);
				} // Player already blocked
				else if (activePlayer.getCommonData().getBlockList().contains(objectId)) {
					sendPacket(SM_SYSTEM_MESSAGE.STR_BLOCKLIST_ALREADY_BLOCKED);
				} // Try and block player
				else {
					if (!SocialService.addBlockedUser(activePlayer, pcd, " ")) {
					log.error("Failed to add " + pcd.getName() + " to the block list for " + activePlayer.getName() + " - check database setup.");
					} else {
						activePlayer.getCommonData().getFriendRequestList().delFriend(objectId);
						DAOManager.getDAO(FriendRequestListDAO.class).delFriends(activePlayer.getObjectId(), objectId);
					}
				}

				break;
		}
		sendPacket(new SM_MARK_FRIENDLIST());
	}
}
