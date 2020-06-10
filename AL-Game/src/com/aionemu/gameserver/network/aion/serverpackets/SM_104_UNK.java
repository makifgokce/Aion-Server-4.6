
package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Gokcee
 */
public class SM_104_UNK extends AionServerPacket {

	private int action, unk, unk1;

	public SM_104_UNK(int action, int unk, int unk1) {
		this.action = action;
		this.unk = unk;
		this.unk1 = unk1;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(action);
		writeH(unk);
		writeD(unk1);
	}
}
