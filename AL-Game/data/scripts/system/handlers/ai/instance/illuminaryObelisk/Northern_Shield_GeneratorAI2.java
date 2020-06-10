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
package ai.instance.illuminaryObelisk;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI2;

/**
 * @author Rinzler
 * @rework Ever
 * @rework Blackfire
 */
@AIName("northern_shield_generator")
public class Northern_Shield_GeneratorAI2 extends ActionItemNpcAI2 {

	private boolean isInstanceDestroyed;
	private boolean wave1 = true;
	private boolean wave2;
	private boolean wave3;
	private boolean restrict;
	private boolean isFull;
	private boolean mobs1, mobs2, mobs3;

	@Override
	protected void handleDialogStart(Player player) {
		if (!restrict) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
		}
	}

	@Override
	public boolean onDialogSelect(final Player player, int dialogId, int questId, int extendedRewardIndex) {
		if (dialogId == 10000) {
			if (!isFull) {
				if (wave1) {
					restrict = true;
					final ItemUseObserver observer = new ItemUseObserver() {
						@Override
						public void abort() {
							player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
							PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
							PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, cancelBarAnimation));
							player.getObserveController().removeObserver(this);
							restrict = false;
						}
					};
					player.getObserveController().attach(observer);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 8000, startBarAnimation));
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, getObjectId()), true);
					if(!mobs1) {
						startWaveNorthernShieldGenerator1();
						ThreadPoolManager.getInstance().schedule(new Runnable() {

							@Override
							public void run() {
								spawn(702017, 169.55626f, 254.52907f, 293.04276f, (byte) 0, 17);
								PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402227));
							}
						}, 3000);
						mobs1 = true;
					}
					player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule(new Runnable() {
						@Override
						public void run() {
							PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
							PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, cancelBarAnimation));
							player.getObserveController().removeObserver(observer);
							if(player.getInventory().decreaseByItemId(164000289, 1)) {
								PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402197));
								spawn(702227, 212.96484f, 254.4526f, 295.90784f, (byte) 60);
								wave1 = false;
								wave2 = true;
							} else {
								player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
								PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402211));
							}
							restrict = false;

						}
					}, 8000));

				}
				if (wave2) {
					restrict = true;
					final ItemUseObserver observer = new ItemUseObserver() {
						@Override
						public void abort() {
							player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
							PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
							PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, cancelBarAnimation));
							player.getObserveController().removeObserver(this);
							restrict = false;
						}
					};
					player.getObserveController().attach(observer);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 8000, startBarAnimation));
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, getObjectId()), true);
					if(!mobs2) {
						startWaveNorthernShieldGenerator2();
						mobs2 = true;
					}
					player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule(new Runnable() {
						@Override
						public void run() {
							PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
							PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, cancelBarAnimation));
							player.getObserveController().removeObserver(observer);
							if(player.getInventory().decreaseByItemId(164000289, 1)) {
								PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402197));
								spawn(702228, 212.96484f, 254.4526f, 295.90784f, (byte) 60);
								wave2 = false;
								wave3 = true;
							} else {
								player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
								PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402211));
							}
							restrict = false;
						}
					}, 8000));

				}
				if (wave3) {
					restrict = true;
					final ItemUseObserver observer = new ItemUseObserver() {
						@Override
						public void abort() {
							player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
							PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
							PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, cancelBarAnimation));
							player.getObserveController().removeObserver(this);
							restrict = false;
						}
					};
					player.getObserveController().attach(observer);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 8000, startBarAnimation));
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, getObjectId()), true);
					if(!mobs3) {
						startWaveNorthernShieldGenerator3();
						mobs3 = true;
					}
					player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule(new Runnable() {
						@Override
						public void run() {
							PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
							PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, cancelBarAnimation));
							player.getObserveController().removeObserver(observer);
							if(player.getInventory().decreaseByItemId(164000289, 1)) {
								PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402201));
								spawn(702229, 212.96484f, 254.4526f, 295.90784f, (byte) 60);
								wave3 = false;
								mobs1 = false;
								mobs2 = false;
								mobs3 = false;
								isFull = true;
							} else {
								player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
								PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402211));
							}
							restrict = false;
						}
					}, 8000));

				}
			} else {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402203));
			}
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return true;
	}

	/**
	 * The higher the phase of the charge will spawn more difficult monsters, in the 3rd phase elite monsters will spawn. Charging a shield to the 3rd phase continuously can be hard because of all the
	 * mobs you will have to handle. A few easy monsters will spawn after a certain time if you leave the shield unit alone. After all units have been charged to the 3rd phase, defeat the remaining
	 * monsters. *************************** Northern Shield Generator * **************************
	 */

	private void startWaveNorthernShieldGenerator1() {
		sp(233726, 174.50981f, 251.38982f, 292.43088f, (byte) 0, 1000, "NorthernShieldGenerator1");
		sp(233727, 174.9973f, 254.4739f, 292.3325f, (byte) 0, 1000, "NorthernShieldGenerator2");
		sp(233857, 174.84029f, 257.80832f, 292.4389f, (byte) 0, 1000, "NorthernShieldGenerator3");

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233729, 174.50981f, 251.38982f, 292.43088f, (byte) 0, 1000, "NorthernShieldGenerator1");
				sp(233734, 174.9973f, 254.4739f, 292.3325f, (byte) 0, 1000, "NorthernShieldGenerator2");
				sp(233738, 174.84029f, 257.80832f, 292.4389f, (byte) 0, 1000, "NorthernShieldGenerator3");
			}
		}, 15000);

	}

	private void startWaveNorthernShieldGenerator2() {
		sp(233736, 174.50981f, 251.38982f, 292.43088f, (byte) 0, 1000, "NorthernShieldGenerator1");
		sp(233737, 174.9973f, 254.4739f, 292.3325f, (byte) 0, 1000, "NorthernShieldGenerator2");
		sp(233730, 174.84029f, 257.80832f, 292.4389f, (byte) 0, 1000, "NorthernShieldGenerator3");

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233728, 174.50981f, 251.38982f, 292.43088f, (byte) 0, 1000, "NorthernShieldGenerator1");
				sp(233731, 174.9973f, 254.4739f, 292.3325f, (byte) 0, 1000, "NorthernShieldGenerator2");
				sp(233732, 174.84029f, 257.80832f, 292.4389f, (byte) 0, 1000, "NorthernShieldGenerator3");
			}
		}, 15000);
	}

	private void startWaveNorthernShieldGenerator3() {
		sp(233736, 174.50981f, 251.38982f, 292.43088f, (byte) 0, 6000, "NorthernShieldGenerator1");
		sp(233737, 174.9973f, 254.4739f, 292.3325f, (byte) 0, 6000, "NorthernShieldGenerator2");
		sp(233730, 174.84029f, 257.80832f, 292.4389f, (byte) 0, 6000, "NorthernShieldGenerator3");
		sp(233729, 174.50981f, 251.38982f, 292.43088f, (byte) 0, 23000, "NorthernShieldGenerator1");
		sp(233734, 174.9973f, 254.4739f, 292.3325f, (byte) 0, 23000, "NorthernShieldGenerator2");
		sp(233738, 174.84029f, 257.80832f, 292.4389f, (byte) 0, 23000, "NorthernShieldGenerator3");

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233728, 174.50981f, 251.38982f, 292.43088f, (byte) 0, 6000, "NorthernShieldGenerator1");
				sp(233731, 174.9973f, 254.4739f, 292.3325f, (byte) 0, 6000, "NorthernShieldGenerator2");
				sp(233732, 174.84029f, 257.80832f, 292.4389f, (byte) 0, 6000, "NorthernShieldGenerator3");
				sp(233739, 174.50981f, 251.38982f, 292.43088f, (byte) 0, 23000, "NorthernShieldGenerator1");
				sp(233735, 174.9973f, 254.4739f, 292.3325f, (byte) 0, 23000, "NorthernShieldGenerator2");
				sp(233733, 174.84029f, 257.80832f, 292.4389f, (byte) 0, 23000, "NorthernShieldGenerator3");
			}
		}, 15000);
	}

	protected void sp(final int npcId, final float x, final float y, final float z, final byte h, final int time, final String walkerId) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed) {
					Npc npc = (Npc) spawn(npcId, x, y, z, h);
					npc.getSpawn().setWalkerId(walkerId);
					WalkManager.startWalking((NpcAI2) npc.getAi2());
				}
			}
		}, time);
	}

	public void onInstanceDestroy() {
		isInstanceDestroyed = true;
	}
}
