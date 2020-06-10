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
package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.FriendRequestList;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;

/**
 * @author Gokcee
 */
public abstract class FriendRequestListDAO implements DAO {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassName() {
		return FriendRequestListDAO.class.getName();
	}

	/**
	 * Loads the friend list for the given player
	 *
	 * @param pcd
	 *            Player to get friend list of
	 * @return FriendList for player
	 */
	public abstract FriendRequestList load(final PlayerCommonData pcd);

	/**
	 * Makes the given players friends
	 * <ul>
	 * <li>Note: Adds for both players</li>
	 * </ul>
	 *
	 * @param player
	 *            Player who is adding
	 * @param friend
	 *            Friend to add to the friend list
	 * @return Success
	 */
	public abstract boolean addFriends(final int playerOid, final int friendOid, final String message, final int expireTime);

	/**
	 * Deletes the friends from eachothers lists
	 *
	 * @param player
	 *            Player whos is deleting
	 * @param friendName
	 *            Name of friend to delete
	 * @return Success
	 */
	public abstract boolean delFriends(final int playerOid, final int friendOid);


}
