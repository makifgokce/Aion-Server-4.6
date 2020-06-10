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

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author pixfid
 * @modified Magenik , Kev
 */
public class SM_ACCOUNT_ACCESS_PROPERTIES extends AionServerPacket {


	private boolean isGm;
	private int accountType;
	private int purchaseType;
	@SuppressWarnings("unused")
	private int time;
	private boolean active;

	public SM_ACCOUNT_ACCESS_PROPERTIES(boolean isGm) {
		this.isGm = isGm;
	}

	public SM_ACCOUNT_ACCESS_PROPERTIES(boolean isGm, int accountType, int purchaseType, int time, boolean active) {
		this.isGm = isGm;
		this.accountType = accountType;
		this.purchaseType = purchaseType;
		this.time = time;
		this.active = active;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(this.isGm ? 5 : 0); // Max 5 GM-Panel(Shift+F1)
		writeH(0);
		writeC(0);
		writeD(this.isGm ? 32768 : 0); // unk
		writeD(0);
		writeC(0);
		writeD(this.active ? 31 : 0); // 31 with Active GoldPaket (Boost)
		writeD(0);
		writeD(purchaseType); // GoldPaket active 8 else 0
		writeD(accountType); // 2 = Starter 4 = Veteran
	}
}
