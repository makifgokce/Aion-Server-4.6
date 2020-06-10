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

//import com.aionemu.gameserver.model.bonus_service.F2pBonus;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.f2p.F2pAccount;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ACCOUNT_ACCESS_PROPERTIES;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PACKAGE_INFO_NOTIFY;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.PacketSendUtility;

public class F2pService {

	//private static F2pBonus f2p;
	public void onEnterWorld(Player player) {
		if (player.getF2p().getF2pAccount() != null) {
			//playerBoostPack(player);
			ExpireTimerTask.getInstance().addTask(player.getF2p().getF2pAccount(), player);
			PacketSendUtility.sendPacket(player, new SM_ACCOUNT_ACCESS_PROPERTIES(player.isGM(), 4, 8, player.getF2p().getF2pAccount().getRemainingTime(), true));
			PacketSendUtility.sendPacket(player, new SM_PACKAGE_INFO_NOTIFY(1, 3, player.getF2p().getF2pAccount().getRemainingTime()));
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1700040, player.getF2p().getF2pAccount().getRemainingTime()));
		} else {
			PacketSendUtility.sendPacket(player, new SM_ACCOUNT_ACCESS_PROPERTIES(player.isGM(), 2, 0, 0, false));
			PacketSendUtility.sendPacket(player, new SM_PACKAGE_INFO_NOTIFY(0, 3, 0));
		}
	}
/*
	public void playerBoostPack(Player player) {
		// Gold Pack.
		f2p = new F2pBonus(1);
		f2p.applyEffect(player, 1);
	}
*/

	public void onAddF2p(Player player, Integer minutes) {
		F2pAccount f2pAccount = new F2pAccount(minutes == null ? 0 : (int) (System.currentTimeMillis() / 1000 + minutes.intValue() * 60));
		player.getF2p().add(f2pAccount, true);
		PacketSendUtility.sendPacket(player, new SM_ACCOUNT_ACCESS_PROPERTIES(player.isGM(), 4, 8, player.getF2p().getF2pAccount().getRemainingTime(), true));
		PacketSendUtility.sendPacket(player, new SM_PACKAGE_INFO_NOTIFY(1, 3, player.getF2p().getF2pAccount().getRemainingTime()));
		ExpireTimerTask.getInstance().addTask(player.getF2p().getF2pAccount(), player);
		//playerBoostPack(player);
	}

	public void onUpdateF2p(Player player, Integer minutes) {
		F2pAccount f2pAccountUpdate = new F2pAccount(minutes == null ? 0 : player.getF2p().getF2pAccount().getExpireTime() + (minutes * 60));
		player.getF2p().update(f2pAccountUpdate, true);
		PacketSendUtility.sendPacket(player, new SM_ACCOUNT_ACCESS_PROPERTIES(player.isGM(), 4, 8, player.getF2p().getF2pAccount().getRemainingTime(), true));
		PacketSendUtility.sendPacket(player, new SM_PACKAGE_INFO_NOTIFY(1, 3, player.getF2p().getF2pAccount().getRemainingTime())); // TODO Count + Id (1, 3)
		ExpireTimerTask.getInstance().addTask(player.getF2p().getF2pAccount(), player);
	}

	public static F2pService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final F2pService instance = new F2pService();
	}
}
