
package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Gokcee
 */
public class SM_PLAYER_PROTECTION extends AionServerPacket {

	private int time;

	public SM_PLAYER_PROTECTION(int time) {
		this.time = time;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(time);
	}
}
