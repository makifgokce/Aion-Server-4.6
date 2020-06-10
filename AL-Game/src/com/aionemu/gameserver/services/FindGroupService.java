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

package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.aionemu.commons.callbacks.util.GlobalCallbackHelper;
import com.aionemu.commons.objects.filter.ObjectFilter;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.autogroup.AutoGroupType;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.FindGroup;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.alliance.callback.AddPlayerToAllianceCallback;
import com.aionemu.gameserver.model.team2.alliance.callback.PlayerAllianceCreateCallback;
import com.aionemu.gameserver.model.team2.alliance.callback.PlayerAllianceDisbandCallback;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;
import com.aionemu.gameserver.model.team2.group.callback.AddPlayerToGroupCallback;
import com.aionemu.gameserver.model.team2.group.callback.PlayerGroupCreateCallback;
import com.aionemu.gameserver.model.team2.group.callback.PlayerGroupDisbandCallback;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FIND_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

import javolution.util.FastMap;

/**
 * Find Group Service
 *
 * @author cura, MrPoke
 */
public class FindGroupService {

	private FastMap<Integer, FindGroup> elyosRecruitFindGroups = new FastMap<Integer, FindGroup>().shared();
	private FastMap<Integer, FindGroup> elyosApplyFindGroups = new FastMap<Integer, FindGroup>().shared();
	private FastMap<Integer, FindGroup> asmodianRecruitFindGroups = new FastMap<Integer, FindGroup>().shared();
	private FastMap<Integer, FindGroup> asmodianApplyFindGroups = new FastMap<Integer, FindGroup>().shared();

	private FindGroupService() {

		GlobalCallbackHelper.addCallback(new FindGroupOnAddPlayerToGroupListener());
		GlobalCallbackHelper.addCallback(new FindGroupPlayerGroupdDisbandListener());
		GlobalCallbackHelper.addCallback(new FindGroupPlayerGroupdCreateListener());
		GlobalCallbackHelper.addCallback(new FindGroupOnAddPlayerToAllianceListener());
		GlobalCallbackHelper.addCallback(new FindGroupAllianceDisbandListener());
		GlobalCallbackHelper.addCallback(new FindGroupAllianceCreateListener());
	}
	//Recruit Group Members
	public void addRecruitFindGroupList(Player player, int action, String message, int groupType) {
		AionObject object = null;
		boolean isInTeam;
		if (player.isInTeam()) {
			object = player.getCurrentTeam();
			isInTeam = true;
		} else {
			object = player;
			isInTeam = false;
		}

		FindGroup findGroup = new FindGroup(object, message, groupType, isInTeam);
		int objectId = object.getObjectId();
		switch (player.getRace()) {
			case ELYOS:
				elyosRecruitFindGroups.put(objectId, findGroup);
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400392));
				break;
			case ASMODIANS:
				asmodianRecruitFindGroups.put(objectId, findGroup);
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400392));
				break;
			default:
				break;
		}

		Collection<FindGroup> findGroupList = new ArrayList<>();
		findGroupList.add(findGroup);

		PacketSendUtility.sendPacket(player, new SM_FIND_GROUP(action, (int) (System.currentTimeMillis() / 1000), findGroupList));
	}
	//Apply for group
	public void addApplyFindGroupList(Player player, int action, String message, int groupType, int classId, int level) {
	AionObject object = player;

	FindGroup findGroup = new FindGroup(object, message, groupType, classId, level);
	int objectId = object.getObjectId();
	switch (player.getRace()) {
		case ELYOS:
			elyosApplyFindGroups.put(objectId, findGroup);
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400393));
			break;
		case ASMODIANS:
			asmodianApplyFindGroups.put(objectId, findGroup);
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400393));
			break;
		default:
			break;
	}

	Collection<FindGroup> findGroupList = new ArrayList<>();
	findGroupList.add(findGroup);

	PacketSendUtility.sendPacket(player, new SM_FIND_GROUP(action, (int) (System.currentTimeMillis() / 1000), findGroupList));

	}
	public void updateRecruitFindGroupList(Player player, String message, int groupType, int objectId) {
		FindGroup findGroup = null;

		switch (player.getRace()) {
			case ELYOS:
				findGroup = elyosRecruitFindGroups.get(objectId);
				findGroup.setMessage(message);
				findGroup.setGroupType(groupType);
				break;
			case ASMODIANS:
				findGroup = asmodianRecruitFindGroups.get(objectId);
				findGroup.setMessage(message);
				findGroup.setGroupType(groupType);
				break;
			default:
				break;
		}
	}
	public void updateApplyFindGroupList(Player player, String message, int groupType, int classId, int level, int objectId) {
		FindGroup findGroup = null;

		switch (player.getRace()) {
			case ELYOS:
				findGroup = elyosApplyFindGroups.get(objectId);
				findGroup.setMessage(message);
				findGroup.setGroupType(groupType);
				findGroup.setClassId(classId);
				findGroup.setLevel(level);
				break;
			case ASMODIANS:
				findGroup = asmodianApplyFindGroups.get(objectId);
				findGroup.setMessage(message);
				findGroup.setGroupType(groupType);
				findGroup.setClassId(classId);
				findGroup.setLevel(level);
				break;
			default:
				break;
		}
	}

	public Collection<FindGroup> getRecruitFindGroups(final Race race, final int action) {
		switch (race) {
			case ELYOS:
				switch (action) {
					case 0x00:
						return elyosRecruitFindGroups.values();
					case 0xA:
						return Collections.emptyList();
				}
				break;
			case ASMODIANS:
				switch (action) {
					case 0x00:
						return asmodianRecruitFindGroups.values();
					case 0xA:
						return Collections.emptyList();
				}
				break;
			default:
				break;
		}
		return null;
	}
	public Collection<FindGroup> getApplyFindGroups(final Race race, final int action) {
		switch (race) {
			case ELYOS:
				switch (action) {
					case 0x04:
						return elyosApplyFindGroups.values();
					case 0xA:
						return Collections.emptyList();
				}
				break;
			case ASMODIANS:
				switch (action) {
					case 0x04:
						return asmodianApplyFindGroups.values();
					case 0xA:
						return Collections.emptyList();
				}
				break;
			default:
				break;
		}
		return null;
	}

	public void registerInstanceGroup(Player player, int action, int instanceId, String message, int minMembers, int groupType) {
		AutoGroupType agt = AutoGroupType.getAGTByMaskId(instanceId);
		if (agt != null) {
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceId, 1, 0, player.getName()));
		}
	}

	public void sendFindGroups(final Player player, final int action) {
		switch(action) {
			case 0x00:
				PacketSendUtility.sendPacket(player, new SM_FIND_GROUP(action, (int) (System.currentTimeMillis() / 1000), getRecruitFindGroups(player.getRace(), action)));
				break;
			case 0x04:
				PacketSendUtility.sendPacket(player, new SM_FIND_GROUP(action, (int) (System.currentTimeMillis() / 1000), getApplyFindGroups(player.getRace(), action)));
				break;
			case 0xA:
				PacketSendUtility.sendPacket(player, new SM_FIND_GROUP(action, (int) (System.currentTimeMillis() / 1000), getRecruitFindGroups(player.getRace(), action)));
				break;
		}
	}

	public FindGroup removeRecruitFindGroup(final Race race, int action, int objectId, int serverId, int hasTeam) {
		FindGroup findGroup = null;
		switch (race) {
			case ELYOS:
				findGroup = elyosRecruitFindGroups.remove(objectId);
				break;
			case ASMODIANS:
				findGroup = asmodianRecruitFindGroups.remove(objectId);
				break;
			default:
				break;
		}
		if (findGroup != null) {
			PacketSendUtility.broadcastFilteredPacket(new SM_FIND_GROUP(action, (int) (System.currentTimeMillis() / 1000), objectId, serverId, hasTeam), new ObjectFilter<Player>() {
				@Override
				public boolean acceptObject(Player object) {
					return race == object.getRace();
				}
			});
		}
		return findGroup;
	}
	public FindGroup removeApplyFindGroup(final Race race, int action, int objectId) {
		FindGroup findGroup = null;
		switch (race) {
			case ELYOS:
				findGroup = elyosApplyFindGroups.remove(objectId);
				break;
			case ASMODIANS:
				findGroup = asmodianApplyFindGroups.remove(objectId);
				break;
			default:
				break;
		}
		if (findGroup != null) {
			PacketSendUtility.broadcastFilteredPacket(new SM_FIND_GROUP(action, (int) (System.currentTimeMillis() / 1000), objectId), new ObjectFilter<Player>() {
				@Override
				public boolean acceptObject(Player object) {
					return race == object.getRace();
				}
			});
		}
		return findGroup;
	}

	public static final FindGroupService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final FindGroupService instance = new FindGroupService();
	}

	static class FindGroupOnAddPlayerToGroupListener extends AddPlayerToGroupCallback {

		@Override
		public void onBeforePlayerAddToGroup(PlayerGroup group, Player player) {
			FindGroupService.getInstance().removeRecruitFindGroup(player.getRace(), 0x00, player.getObjectId(), player.getCommonData().getServerId(), player.isInTeam() ? 0 : 16);
			FindGroupService.getInstance().removeApplyFindGroup(player.getRace(), 0x04, player.getObjectId());
		}

		@Override
		public void onAfterPlayerAddToGroup(PlayerGroup group, Player player) {
			if (group.isFull()) {
				FindGroupService.getInstance().removeRecruitFindGroup(group.getRace(), 0, group.getObjectId(), player.getCommonData().getServerId(), player.isInTeam() ? 0 : 16);
			}
		}
	}

	static class FindGroupPlayerGroupdDisbandListener extends PlayerGroupDisbandCallback {

		@Override
		public void onBeforeGroupDisband(PlayerGroup group) {
			FindGroupService.getInstance().removeRecruitFindGroup(group.getRace(), 0, group.getTeamId(), group.getLeaderObject().getCommonData().getServerId(), group.getLeaderObject().isInTeam() ? 0 : 16);
		}

		@Override
		public void onAfterGroupDisband(PlayerGroup group) {
		}
	}

	static class FindGroupPlayerGroupdCreateListener extends PlayerGroupCreateCallback {

		@Override
		public void onBeforeGroupCreate(Player player) {
		}

		@Override
		public void onAfterGroupCreate(Player player) {
			FindGroup inviterFindGroup = FindGroupService.getInstance().removeRecruitFindGroup(player.getRace(), 0x00, player.getObjectId(), player.getCommonData().getServerId(), player.isInTeam() ? 0 : 16);
			if (inviterFindGroup == null) {
				inviterFindGroup = FindGroupService.getInstance().removeApplyFindGroup(player.getRace(), 0x04, player.getObjectId());
				if(inviterFindGroup != null) {
					FindGroupService.getInstance().addApplyFindGroupList(player, 0x06, inviterFindGroup.getMessage(), inviterFindGroup.getGroupType(), inviterFindGroup.getClassId(), inviterFindGroup.getLevel());
				}
			} else {
					FindGroupService.getInstance().addRecruitFindGroupList(player, 0x02, inviterFindGroup.getMessage(), inviterFindGroup.getGroupType());
			}

		}
	}

	static class FindGroupAllianceDisbandListener extends PlayerAllianceDisbandCallback {

		@Override
		public void onBeforeAllianceDisband(PlayerAlliance alliance) {
			FindGroupService.getInstance().removeRecruitFindGroup(alliance.getRace(), 0, alliance.getTeamId(), alliance.getLeaderObject().getCommonData().getServerId(), alliance.getLeaderObject().isInTeam() ? 0 : 16);
		}

		@Override
		public void onAfterAllianceDisband(PlayerAlliance alliance) {
		}
	}

	static class FindGroupAllianceCreateListener extends PlayerAllianceCreateCallback {

		@Override
		public void onBeforeAllianceCreate(Player player) {
		}

		@Override
		public void onAfterAllianceCreate(Player player) {
			FindGroup inviterFindGroup = FindGroupService.getInstance().removeRecruitFindGroup(player.getRace(), 0x00, player.getObjectId(), player.getCommonData().getServerId(), player.isInTeam() ? 0 : 16);
			if (inviterFindGroup == null) {
				inviterFindGroup = FindGroupService.getInstance().removeApplyFindGroup(player.getRace(), 0x04, player.getObjectId());
				if(inviterFindGroup != null) {
					FindGroupService.getInstance().addApplyFindGroupList(player, 0x06, inviterFindGroup.getMessage(), inviterFindGroup.getGroupType(), inviterFindGroup.getClassId(), inviterFindGroup.getLevel());
				}
			} else {
					FindGroupService.getInstance().addRecruitFindGroupList(player, 0x02, inviterFindGroup.getMessage(), inviterFindGroup.getGroupType());
			}
		}
	}

	static class FindGroupOnAddPlayerToAllianceListener extends AddPlayerToAllianceCallback {

		@Override
		public void onBeforePlayerAddToAlliance(PlayerAlliance alliance, Player player) {
			FindGroupService.getInstance().removeRecruitFindGroup(player.getRace(), 0x00, player.getObjectId(), player.getCommonData().getServerId(), player.isInTeam() ? 0 : 16);
			FindGroupService.getInstance().removeApplyFindGroup(player.getRace(), 0x04, player.getObjectId());
		}

		@Override
		public void onAfterPlayerAddToAlliance(PlayerAlliance alliance, Player player) {
			if (alliance.isFull()) {
				FindGroupService.getInstance().removeRecruitFindGroup(alliance.getRace(), 0, alliance.getObjectId(), alliance.getLeaderObject().getCommonData().getServerId(), alliance.getLeaderObject().isInTeam() ? 0 : 16);
			}
		}
	}
}
