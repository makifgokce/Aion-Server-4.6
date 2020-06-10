
package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Gokcee
 */
public class SM_A5_UNK extends AionServerPacket {

	private int action, unk;

	public SM_A5_UNK(int action, int unk) {
		this.action = action;
		this.unk = unk;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action);
		writeH(unk);
	}
}
