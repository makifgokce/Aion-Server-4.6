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

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.FriendRequestListDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.DeniedStatus;
import com.aionemu.gameserver.model.gameobjects.player.FriendRequest;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FRIEND_RESPONSE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SocialService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.world.World;

/**
 * Received when a user tries to add someone as his friend
 *
 * @author Ben
 */
public class CM_FRIEND_ADD extends AionClientPacket {

	private String targetName;
	private String message;
	private PlayerCommonData pcd;

	public CM_FRIEND_ADD(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		targetName = Util.convertName(readS());
		message = readS();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {

		final Player activePlayer = getConnection().getActivePlayer();
		Player targetPlayer = World.getInstance().findPlayer(targetName);
		pcd = DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonDataByName(targetName);
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
			if(pcd.isOnline()) {
			RequestResponseHandler responseHandler = new RequestResponseHandler(activePlayer) {

				@Override
				public void acceptRequest(Creature requester, Player responder) {
					if (!pcd.isOnline()) {
						sendPacket(new SM_FRIEND_RESPONSE(targetName, SM_FRIEND_RESPONSE.TARGET_OFFLINE));
					}
					else if (activePlayer.getCommonData().getFriendList().isFull() || responder.getCommonData().getFriendList().isFull()) {
						return;
					}
					else {
						SocialService.makeFriends((Player) requester, responder.getCommonData());
					}

				}

				@Override
				public void denyRequest(Creature requester, Player responder) {
					sendPacket(new SM_FRIEND_RESPONSE(targetName, SM_FRIEND_RESPONSE.TARGET_DENIED));
					PacketSendUtility.sendPacket(responder, new SM_SYSTEM_MESSAGE(1401517, requester.getName()));

				}
			};
				boolean requested = targetPlayer.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_BUDDYLIST_ADD_BUDDY_REQUEST, responseHandler);
				// If the player is busy and could not be asked
				if (!requested) {
					sendPacket(SM_SYSTEM_MESSAGE.STR_BUDDYLIST_BUSY);
				}
				else {
					if (targetPlayer.getCommonData().getPlayerSettings().isInDeniedStatus(DeniedStatus.FRIEND)) {
						sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_FRIEND(targetPlayer.getName()));
						return;
					}
					if (targetPlayer.getCommonData().getFriendList().isFull()) {
						sendPacket(new SM_FRIEND_RESPONSE(targetName, SM_FRIEND_RESPONSE.TARGET_LIST_FULL));
						return;
					}
					// Send question packet to buddy 1401518
					targetPlayer.getClientConnection().sendPacket(new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_BUDDYLIST_ADD_BUDDY_REQUEST, activePlayer.getObjectId(), 0, activePlayer.getName()));
				}
			} else {
				if(pcd.getPlayerSettings().isInDeniedStatus(DeniedStatus.FRIEND)) {
					sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_FRIEND(pcd.getName()));
					return;
				} else if(pcd.getFriendRequestList().getFriendRequest(activePlayer.getObjectId()) != null) {
					sendPacket(new SM_SYSTEM_MESSAGE(1401500, targetName));
					return;
				} else {
					int expireTime = ((int) (System.currentTimeMillis() / 1000) + 604800); // 7 days
					sendPacket(new SM_FRIEND_RESPONSE(targetName, SM_FRIEND_RESPONSE.TARGET_OFFLINE_ADDED));
					PacketSendUtility.sendPacket(activePlayer, new SM_SYSTEM_MESSAGE(1401501, targetName)); // %0 şu an çevrimiçi değil. Fakat arkadaşlık isteğin gönderildi.
					pcd.getFriendRequestList().addFriendRequest(new FriendRequest(activePlayer.getCommonData(), message, expireTime));
					DAOManager.getDAO(FriendRequestListDAO.class).addFriends(pcd.getPlayerObjId(), activePlayer.getObjectId(), message, expireTime);
				}

			}
		}
	}
}
