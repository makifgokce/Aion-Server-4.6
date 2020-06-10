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

package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.FindGroupService;

/**
 * @author cura, MrPoke
 */
public class CM_FIND_GROUP extends AionClientPacket {

	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(CM_FIND_GROUP.class);
	private int action;
	private int objectId;
	private String message;
	private int groupType;
	private int classId;
	private int level;
	private int instanceId;
	private int minMembers;
	private int serverId;
	private int hasTeam;

	public CM_FIND_GROUP(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		action = readC();

		switch (action) {
			case 0x00: // recruit list
				break;
			case 0x01: // offer delete
				objectId = readD();
				serverId = readH(); //serverId
				readC(); // 0
				hasTeam = readC(); // someCode (10) for Group owner ?
				break;
			case 0x02: // send offer
				objectId = readD();
				message = readS();
				groupType = readC();
				break;
			case 0x03: // recruit update
				objectId = readD();
				serverId = readH(); //serverId
				readC(); // 0
				hasTeam = readC(); // someCode (10) for Group owner ?
				message = readS();
				groupType = readC();
				break;
			case 0x04: // apply list
				break;
			case 0x05: // post delete
				objectId = readD();
				break;
			case 0x06: // apply create
				objectId = readD();
				message = readS();
				groupType = readC();
				classId = readC();
				level = readC();
				break;
			case 0x07: // apply update
				objectId = readD();
				message = readS();
				groupType = readC();
				classId = readC();
				level = readC();
				break;
			case 0x08: // register InstanceGroup
				instanceId = readD();
				groupType = readC();// need to be tested
				message = readS();// text
				minMembers = readC();// minMembers chosen by writer
				break;
			case 0x0A: // New 4.0 Group Recruitment
				break;
			default:
				log.error("Unknown find group packet? 0x" + Integer.toHexString(action).toUpperCase(Locale.forLanguageTag("en")));
				break;
		}
	}

	@Override
	protected void runImpl() {
		final Player player = this.getConnection().getActivePlayer();
		if(this.getConnection().getState() == State.IN_GAME) {
			switch (action) {
				case 0x00:
				case 0x04:
					FindGroupService.getInstance().sendFindGroups(player, action);
					break;
				case 0x01:
					FindGroupService.getInstance().removeRecruitFindGroup(player.getRace(), action, objectId, serverId, hasTeam);
					break;
				case 0x05:
					FindGroupService.getInstance().removeApplyFindGroup(player.getRace(), action, objectId);
					break;
				case 0x02:
					FindGroupService.getInstance().addRecruitFindGroupList(player, action, message, groupType);
					break;
				case 0x06:
					FindGroupService.getInstance().addApplyFindGroupList(player, action, message, groupType, classId, level);
					break;
				case 0x03:
					FindGroupService.getInstance().updateRecruitFindGroupList(player, message, groupType, objectId);
					break;
				case 0x07:
					FindGroupService.getInstance().updateApplyFindGroupList(player, message, groupType, classId, level, objectId);
					break;
				case 0x08:
					FindGroupService.getInstance().registerInstanceGroup(player, 0x0E, instanceId, message, minMembers, groupType);
					break;
				case 0x0A: // search
					FindGroupService.getInstance().sendFindGroups(player, action);
					break;
				default:
					break;
			}
		}
	}
}
