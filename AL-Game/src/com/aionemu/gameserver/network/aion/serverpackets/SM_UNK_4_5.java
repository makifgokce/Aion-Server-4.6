
package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Gokcee
 */
public class SM_UNK_4_5 extends AionServerPacket {

	private int action;

	public SM_UNK_4_5(int action) {
		this.action = action;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(action);
	}
}
