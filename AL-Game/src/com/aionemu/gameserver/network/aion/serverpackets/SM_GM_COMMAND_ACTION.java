
package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Gokcee
 */
public class SM_GM_COMMAND_ACTION extends AionServerPacket {

	private int action;

	public SM_GM_COMMAND_ACTION(int action) {
		this.action = action;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action);
	}
}
