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
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MANTRA_EFFECT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.action.DamageType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcAtkInstantEffect")
public class ProcAtkInstantEffect extends DamageEffect {

	@Override
	public void applyEffect(Effect effect) {
		if (effect.getEffected() != effect.getEffector() && effect.getEffector() instanceof Player) {
			if(!isDelayedSkill(effect.getSkillId())) {
				PacketSendUtility.sendPacket((Player) effect.getEffector(), new SM_SYSTEM_MESSAGE(1301062, new DescriptionId(effect.getSkillTemplate().getNameId())));
			}
		}
		if(effect.getEffector() instanceof Player) {
			PacketSendUtility.broadcastPacket(effect.getEffector(), new SM_MANTRA_EFFECT(effect.getEffected().getObjectId(), effect.getSkillId(), 1));
		}
		effect.getEffected().getController().onAttack(effect.getEffector(), effect.getSkillId(), TYPE.DAMAGE, effect.getReserved1(), false, LOG.PROCATKINSTANT);
	}

	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, DamageType.MAGICAL);
	}

	public boolean isDelayedSkill(int skillId) {
		switch(skillId) {
			case 8225:	// Gözle Görünür İşaretli Etki Geciktirmesi
			case 8773:	// Kullanım: Hetgolem
			case 8809:	// Saldırı Yankısı I
			case 8810:	// Saldırı Yankısı II
			case 8811:	// Saldırı Yankısı III
			case 8808:	// Felç Yankısı I
			case 8700:	// Karanlığın Bakışı
			case 18682:	// Parçalayan Kum
			case 19205:	// Ölümcül Yankı
			case 8724:	// Dönüşüm: Mavi Kristal
			case 8725:	// Dönüşümü kaldır: Mavi Kristal
			case 8726:	// Dönüşüm: Mavi Kristal
			case 8727:	// Dönüşümü kaldır: Mavi Kristal
			case 8757:	// Yer Çekimi Kontrolü
			case 20541:	// Gecikmeli Ölümcül Yara
			case 8763:	// Deliliğin Alameti
			case 20857:	// Artçı Şok Bombası
			case 8774:	// Korku Alevi
			case 20946:	// Ruh Takıntısı
			case 20948:	// İd Erozyonu
			case 21572:	// Distortion Pulse
			case 21624:	// İllüzyon Sisi
				return true;
			default:
				return false;
		}
	}
}
