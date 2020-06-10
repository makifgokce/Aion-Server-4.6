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
package com.aionemu.gameserver.model.gameobjects.player;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.FriendRequestListDAO;
import com.aionemu.gameserver.model.IExpirable;
import com.aionemu.gameserver.model.PlayerClass;

/**
 * @author Gokcee
 */
public class FriendRequest implements IExpirable{

	private PlayerCommonData pcd;
	private String friendRequestNote = "";
	private int expireTime;

	public FriendRequest(PlayerCommonData pcd, String note, int expireTime) {
		this.pcd = pcd;
		this.friendRequestNote = note;
		this.expireTime = expireTime;
	}

	/**
	 * Returns this friend's name
	 *
	 * @return Friend's name
	 */
	public String getName() {
		return pcd.getName();
	}

	public int getLevel() {
		return pcd.getLevel();
	}

	public PlayerClass getPlayerClass() {
		return pcd.getPlayerClass();
	}

	public int getOid() {
		return pcd.getPlayerObjId();
	}

	public Player getPlayer() {
		return pcd.getPlayer();
	}

	public boolean isOnline() {
		return pcd.isOnline();
	}
	public String getFriendRequestNote() {
		return friendRequestNote;
	}

	@Override
	public int getExpireTime() {
		return expireTime;
	}

    public void setExpireTime(int expireTime) {
    	this.expireTime = expireTime;
    }
    public int getExpireTimeRemaining() {
        if (expireTime == 0) {
            return 0;
        }
        return expireTime - (int) (System.currentTimeMillis() / 1000);
    }

	@Override
	public void expireEnd(Player player) {
		player.getCommonData().getFriendRequestList().delFriend(getOid());
		DAOManager.getDAO(FriendRequestListDAO.class).delFriends(player.getObjectId(), getOid());
	}

	@Override
	public boolean canExpireNow() {
		return true;
	}

	@Override
	public void expireMessage(Player player, int time) {

	}
}
