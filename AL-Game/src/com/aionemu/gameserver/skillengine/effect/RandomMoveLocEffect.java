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
package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RIDE_ROBOT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TARGET_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TRANSFORM;
import com.aionemu.gameserver.skillengine.model.DashStatus;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.SkillMoveType;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Bio
 * @reworked Kill3r
 * @update a7xatomic
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RandomMoveLocEffect")
public class RandomMoveLocEffect extends EffectTemplate {

	@XmlAttribute(name = "distance")
	private float distance;
	@XmlAttribute(name = "direction")
	private float direction;

	@Override
	public void applyEffect(Effect effect) {
		final Player effector = (Player) effect.getEffector();

		// Deselect targets
		PacketSendUtility.sendPacket(effector, new SM_TARGET_UPDATE(effector));

		Skill skill = effect.getSkill();
		World.getInstance().updatePosition(effector, skill.getX(), skill.getY(), skill.getZ(), skill.getH());
	}

	@Override
	public void calculate(Effect effect) {
		effect.addSucessEffect(this);
		if (((Player) effect.getEffector()).getRobotId() != 0) {
			if (effect.getSkill().getSkillId() == 3853 || effect.getSkill().getSkillId() == 3818) {
				effect.setDashStatus(DashStatus.RANDOMMOVELOC);
			}
			else {
				effect.setDashStatus(DashStatus.ROBOTMOVELOC);
			}
		}
		else {
			effect.setDashStatus(DashStatus.RANDOMMOVELOC);
			effect.setSkillMoveType(SkillMoveType.MOVEBEHIND);
		}
		final Player effector = (Player) effect.getEffector();
		if (effect.getSkill().getSkillId() == 3853 || effect.getSkill().getSkillId() == 3818) {
			RemoveSkill(effector);
			PacketSendUtility.broadcastPacket(effector, new SM_RIDE_ROBOT(effector, 0), true);
			effector.setUseRobot(false);
			effector.setRobotId(0);
		}

		// Move Effector backwards direction=1 or frontwards direction=0
		double radian = Math.toRadians(MathUtil.convertHeadingToDegree(effector.getHeading()));
		float x1 = (float) (Math.cos(Math.PI * direction + radian) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction + radian) * distance);
		float targetZ = effector.isFlying() ? effector.getZ() : GeoService.getInstance().getZ(effector.getWorldId(), effector.getX() + x1, effector.getY() + y1, effector.getZ() + 1.5f, 0.2f, effector.getInstanceId());
		effector.getEffectController().updatePlayerEffectIcons();
		PacketSendUtility.broadcastPacketAndReceive(effector, new SM_TRANSFORM(effector, true));
		PacketSendUtility.broadcastPacketAndReceive(effector, new SM_TRANSFORM(effector, effector.getTransformedModelId(), true, effector.getTransformedItemId()));
		byte intentions = (byte) (CollisionIntention.PHYSICAL.getId() | CollisionIntention.DOOR.getId());
		Vector3f closestCollision = GeoService.getInstance().getClosestCollision(effector, effector.getX() + x1, effector.getY() + y1, targetZ, false, intentions);
		effect.getSkill().setTargetPosition(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ(), effector.getHeading());
	}

	private void RemoveSkill(Player player) {
		player.getEffectController().removeEffect(3597); // Embark I
		player.getEffectController().removeEffect(3598); // Embark II
		player.getEffectController().removeEffect(3599); // Embark III
		player.getEffectController().removeEffect(3600); // Embark VI
		player.getEffectController().removeEffect(3601); // Embark V
		player.getEffectController().removeEffect(3776); // Mobility Thrusters I
		player.getEffectController().removeEffect(3777); // Mobility Thrusters II
		player.getEffectController().removeEffect(3766); // Kinetic Battery I
		player.getEffectController().removeEffect(3767); // Kinetic Battery II
		player.getEffectController().removeEffect(3768); // Kinetic Battery III
		player.getEffectController().removeEffect(3769); // Kinetic Battery VI
		player.getEffectController().removeEffect(3770); // Kinetic Battery V
		player.getEffectController().removeEffect(3771); // Kinetic Bulwark I
		player.getEffectController().removeEffect(3772); // Kinetic Bulwark II
		player.getEffectController().removeEffect(3773); // Kinetic Bulwark III
		player.getEffectController().removeEffect(3779); // Stability Thrusters I
		player.getEffectController().removeEffect(3780); // Stability Thrusters II
		player.getEffectController().removeEffect(3781); // Stability Thrusters III
		player.getEffectController().removeEffect(3782); // Stability Thrusters IV
		player.getEffectController().removeEffect(3783); // Stability Thrusters V
		player.getEffectController().removeEffect(3784); // Stability Thrusters VI
		player.getEffectController().removeEffect(3785); // Stability Thrusters VII
	}
}
