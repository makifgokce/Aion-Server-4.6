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

import java.util.Calendar;

import com.aionemu.commons.network.IPRange;
import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.configs.network.IPConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.network.NetworkController;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.ChatService;
import com.aionemu.gameserver.services.EventService;

/**
 * @author -Nemesiss- CC fix
 * @modified by Novo, cura
 */
public class SM_VERSION_CHECK extends AionServerPacket {

	/**
	 * Aion Client version
	 */
	private int version;
	/**
	 * Number of characters can be created
	 */
	private int characterLimitCount;
	/**
	 * Related to the character creation mode
	 */
	private final int characterFactionsMode;
	private final int characterCreateMode;

	/**
	 * @param chatService
	 */
	public SM_VERSION_CHECK(int version) {
		this.version = version;

		if (MembershipConfig.CHARACTER_ADDITIONAL_ENABLE != 10 && MembershipConfig.CHARACTER_ADDITIONAL_COUNT > GSConfig.CHARACTER_LIMIT_COUNT) {
            characterLimitCount = MembershipConfig.CHARACTER_ADDITIONAL_COUNT;
        } else {
            characterLimitCount = GSConfig.CHARACTER_LIMIT_COUNT;
        }
        characterLimitCount *= NetworkController.getInstance().getServerCount();

        if (GSConfig.CHARACTER_CREATION_MODE < 0 || GSConfig.CHARACTER_CREATION_MODE > 2) {
            characterFactionsMode = 0;
        } else {
            characterFactionsMode = GSConfig.CHARACTER_CREATION_MODE;
        }

        if (GSConfig.CHARACTER_FACTION_LIMITATION_MODE < 0 || GSConfig.CHARACTER_FACTION_LIMITATION_MODE > 3) {
            characterCreateMode = 0;
        } else {
            characterCreateMode = GSConfig.CHARACTER_FACTION_LIMITATION_MODE * 0x04;
        }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		// aion 3.0 = 194
		// aion 3.5 = 196
		// aion 4.0 = 201
		// aion 4.5 = 203
		if (version < 204) {
			// Send wrong client version
			writeC(2);
			return;
		}
		writeC(0);
		writeC(NetworkConfig.GAMESERVER_ID);
		writeD(140820); // start year month day
		writeD(140820); // start year month day
		writeD(0); // spacing
		writeD(190401); // year month day
		writeD(GameServer.getServerStartTime()); // start server time in mili
		writeC(0); // unk
		writeC(GSConfig.SERVER_COUNTRY_CODE);// country code;
		writeC(0); // unk

		int serverMode = (characterLimitCount * 0x10) | characterFactionsMode;
        writeC(serverMode | characterCreateMode);

		writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000));
		writeH(350); // 4.6
		writeH(2561); // 4
		writeH(2569); // 4.6
		writeH(16906); // 4.6
		writeD(131329); // 4.6
		writeC(GSConfig.CHARACTER_REENTRY_TIME);
		writeC(EventsConfig.ENABLE_DECOR);
		writeC(EventService.getInstance().getEventType().getId());
		writeC(0);
		writeC(0);
		writeC(0);
		writeD(-10800);// TimeZone
		writeD(1653700612);
		writeC(2);
		writeD(0);
		writeD(0);
		writeC(0);
		writeH(3000);
		writeH(1);
		writeC(0);
		writeC(1);
		writeD(0);
		writeH(1); // its loop size
		// for... chat servers?
		{
			writeC(0);// spacer
			// if the correct ip is not sent it will not work
			byte[] addr = IPConfig.getDefaultAddress();
			for (IPRange range : IPConfig.getRanges()) {
				if (range.isInRange(con.getIP())) {
					addr = range.getAddress();
					break;
				}
			}
			writeB(addr);
			writeH(ChatService.getPort());
		}
	}
}
