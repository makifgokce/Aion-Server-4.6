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

package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.FriendRequest;
import com.aionemu.gameserver.model.gameobjects.player.FriendRequestList;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 *
 * @author xTz
 */
public class SM_MARK_FRIENDLIST extends AionServerPacket {

	@Override
	protected void writeImpl(AionConnection con) {
		FriendRequestList list = con.getActivePlayer().getCommonData().getFriendRequestList();
		writeD(con.getActivePlayer().getObjectId());
		writeC(1);
		writeH(list.getSize());
		for(FriendRequest request : list) {
			writeD(request.getOid());
			writeS(request.getName());
			writeS(request.getFriendRequestNote());
			writeD(request.getLevel());
			writeD(request.getPlayerClass().getClassId());
			writeC(request.isOnline() ? 1 : 0);
			writeD(1); // unk
		}
	}
}
