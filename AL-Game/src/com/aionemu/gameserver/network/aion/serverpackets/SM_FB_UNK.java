
package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Gokcee
 */
public class SM_FB_UNK extends AionServerPacket {

	private int action;

	public SM_FB_UNK(int action) {
		this.action = action;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(action);
	}
}
