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

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents a player's Friend list
 *
 * @author makifgokce
 */
public class FriendRequestList implements Iterable<FriendRequest> {

	private final Queue<FriendRequest> friends;
	private PlayerCommonData pcd;

	/**
	 * Constructs an empty friend list for the given player
	 *
	 * @param player
	 *            Player who has this friendlist
	 */
	public FriendRequestList(PlayerCommonData pcd) {
		this(pcd, new ConcurrentLinkedQueue<FriendRequest>());
	}

	/**
	 * Constructs a friend list for the given player, with the given friends
	 *
	 * @param player
	 *            Player who has this friend list
	 * @param friends
	 *            Friends on the list
	 */
	public FriendRequestList(PlayerCommonData pcd, Collection<FriendRequest> newFriends) {
		this.friends = new ConcurrentLinkedQueue<>(newFriends);
		this.pcd = pcd;
	}

	/**
	 * Gets the friend with this objId<br />
	 * Returns null if it is not our friend
	 *
	 * @param objId
	 *            objId of friend
	 * @return Friend
	 */
	public FriendRequest getFriendRequest(int objId) {
		for (FriendRequest friend : friends) {
			if (friend.getOid() == objId) {
				return friend;
			}
		}
		return null;
	}

	/**
	 * Returns number of friends in list
	 *
	 * @return Num Friends in list
	 */
	public int getSize() {
		return friends.size();
	}

	/**
	 * Adds the given friend to the list<br />
	 * To add a friend in the database, see <tt>PlayerService</tt>
	 *
	 * @param friend
	 */
	public void addFriendRequest(FriendRequest friend) {
		friends.add(friend);
	}

	public void delFriend(int friendOid) {
		Iterator<FriendRequest> it = iterator();
		while (it.hasNext()) {
			if (it.next().getOid() == friendOid) {
				it.remove();
			}
		}
	}

	/**
	 * Gets the Friend by this name
	 *
	 * @param name
	 *            Name of friend
	 * @return Friend matching name
	 */
	public FriendRequest getFriendRequest(String name) {
		for (FriendRequest friend : friends) {
			if (friend.getName().equalsIgnoreCase(name)) {
				return friend;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<FriendRequest> iterator() {
		return friends.iterator();
	}

}
