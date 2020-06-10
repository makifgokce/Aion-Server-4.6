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

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MpAttackEffect")
public class MpAttackEffect extends AbstractOverTimeEffect {

	@XmlAttribute(name = "percent")
	protected boolean percent;
	@XmlAttribute(name = "value")
	protected int value;

	// TODO bosses are resistent to this?
	@Override
	public void onPeriodicAction(Effect effect) {
		Creature effected = effect.getEffected();
		int maxMP = effected.getLifeStats().getMaxMp();
		int newValue = value;
		// Support for values in percentage
		if (percent) {
			newValue = (maxMP * value) / 100;
		}

		if(newValue < 0) {
			newValue = 0;
		}
		effect.setReserved2(newValue);
		effected.getLifeStats().reduceMp(TYPE.HEAL_MP, newValue, 0, LOG.REGULAR);
	}
}
