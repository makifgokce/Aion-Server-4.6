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
package com.aionemu.gameserver.model.stats.calc.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.ArmorType;

public class StatEnchantFunction extends StatAddFunction {
  private static final Logger log = LoggerFactory.getLogger(StatEnchantFunction.class);
  private Item item;

  public StatEnchantFunction(Item owner, StatEnum stat) {
    this.stat = stat;
    this.item = owner;
  }

  @Override
public final int getPriority() {
    return 30;
  }

  @Override
public void apply(Stat2 stat) {
    if (!this.item.isEquipped()) {
      return;
    }
    int enchantLvl = this.item.getEnchantLevel();
    if (this.item.getItemTemplate().isAccessory() || this.item.getItemTemplate().isPlume()) {
      enchantLvl = this.item.getAuthorize();
    }
    if (enchantLvl == 0) {
      return;
    }
    if ((this.item.getEquipmentSlot() == ItemSlot.MAIN_OFF_HAND.getSlotIdMask()) || (this.item.getEquipmentSlot() == ItemSlot.SUB_OFF_HAND.getSlotIdMask())) {
      return;
    }
    stat.addToBase(getEnchantAdditionModifier(enchantLvl, stat));
  }

  private int getEnchantAdditionModifier(int enchantLvl, Stat2 stat) {
    if (this.item.getItemTemplate().isWeapon()) {
      return getWeaponModifiers(enchantLvl);
    }
    if (this.item.getItemTemplate().isAccessory() || this.item.getItemTemplate().isPlume()) {
      return getAccessoryModifiers(enchantLvl);
    }
    if (this.item.getItemTemplate().isArmor()) {
      return getArmorModifiers(enchantLvl, stat);
    }
    return 0;
  }

  private int getAccessoryModifiers(int autorizeLvl) {
    switch (this.stat) {
    case PVP_ATTACK_RATIO:
      switch (autorizeLvl) {
      case 1:
        return 2;
      case 2:
        return 7;
      case 3:
        return 12;
      case 4:
        return 17;
      case 5:
        return 25;
      case 6:
        return 33;
      case 7:
        return 45;
      }
      return 0;
    case PVP_DEFEND_RATIO:
      switch (autorizeLvl) {
      case 1:
        return 3;
      case 2:
        return 9;
      case 3:
        return 15;
      case 4:
        return 21;
      case 5:
        return 31;
      case 6:
        return 41;
      case 7:
        return 55;
      }
    case MAXHP:
        return 150 * autorizeLvl;
    case PHYSICAL_ATTACK:
        return 4 * autorizeLvl;
    case BOOST_MAGICAL_SKILL:
        return 20 * autorizeLvl;
	default:
		break;
    }
    return 0;
  }

  private int getWeaponModifiers(int enchantLvl) {
    switch (this.stat) {
    case MAIN_HAND_POWER:
    case OFF_HAND_POWER:
    case PHYSICAL_ATTACK:
      switch (this.item.getItemTemplate().getWeaponType()) {
      case DAGGER_1H:
      case SWORD_1H:
        return 2 * enchantLvl;
      case POLEARM_2H:
      case SWORD_2H:
      case BOW:
        return 4 * enchantLvl;
      case MACE_1H:
      case STAFF_2H:
        return 3 * enchantLvl;
	default:
		break;
      }
      return 0;
    case BOOST_MAGICAL_SKILL:
      switch (this.item.getItemTemplate().getWeaponType()) {
      case MACE_1H:
      case STAFF_2H:
      case BOOK_2H:
      case ORB_2H:
      case HARP_2H:
      case GUN_1H:
      case CANNON_2H:
      case KEYBLADE_2H:
        return 20 * enchantLvl;
	default:
		break;
      }
      return 0;
    case MAGICAL_ATTACK:
      switch (this.item.getItemTemplate().getWeaponType()) {
      case GUN_1H:
        return 2 * enchantLvl;
      case BOOK_2H:
      case ORB_2H:
        return 3 * enchantLvl;
      case HARP_2H:
      case CANNON_2H:
      case KEYBLADE_2H:
        return 4 * enchantLvl;
	default:
		break;
      }
      return 0;
	default:
		break;
    }
    return 0;
  }

  private int getArmorModifiers(int enchantLvl, Stat2 applyStat) {
    ArmorType armorType = this.item.getItemTemplate().getArmorType();
    if (armorType == null) {
      log.warn("Missing item ArmorType itemId: " + this.item.getItemId() + " EquipmentSlot: " + this.item.getEquipmentSlot() + " playerObjectId: " + applyStat.getOwner().getObjectId());
      return 0;
    }
    int equipmentSlot = (int)(this.item.getEquipmentSlot() & 0xFFFFFFFF);
    switch (this.item.getItemTemplate().getArmorType()) {
    case ROBE:
      switch (equipmentSlot) {
      case 16:
      case 32:
      case 2048:
        switch (this.stat) {
        case PHYSICAL_DEFENSE:
          return enchantLvl;
        case MAXHP:
          return 10 * enchantLvl;
        case PHYSICAL_CRITICAL_RESIST:
          return 2 * enchantLvl;
        case MAGICAL_DEFEND:
          return 2 * enchantLvl;
		default:
			break;
        }
        return 0;
      case 4096:
        switch (this.stat) {
        case PHYSICAL_DEFENSE:
          return 2 * enchantLvl;
        case MAXHP:
          return 12 * enchantLvl;
        case PHYSICAL_CRITICAL_RESIST:
          return 3 * enchantLvl;
        case MAGICAL_DEFEND:
          return 2 * enchantLvl;
		default:
			break;
        }
        return 0;
      case 8:
        switch (this.stat) {
        case PHYSICAL_DEFENSE:
          return 3 * enchantLvl;
        case MAXHP:
          return 14 * enchantLvl;
        case PHYSICAL_CRITICAL_RESIST:
          return 4 * enchantLvl;
        case MAGICAL_DEFEND:
          return 3 * enchantLvl;
		default:
			break;
        }
        return 0;
      }
      return 0;
    case LEATHER:
      switch (equipmentSlot) {
      case 16:
      case 32:
      case 2048:
        switch (this.stat) {
        case PHYSICAL_DEFENSE:
          return 2 * enchantLvl;
        case MAXHP:
          return 8 * enchantLvl;
        case PHYSICAL_CRITICAL_RESIST:
          return 2 * enchantLvl;
        case MAGICAL_DEFEND:
          return 2 * enchantLvl;
		default:
			break;
        }
        return 0;
      case 4096:
        switch (this.stat) {
        case PHYSICAL_DEFENSE:
          return 3 * enchantLvl;
        case MAXHP:
          return 10 * enchantLvl;
        case PHYSICAL_CRITICAL_RESIST:
          return 3 * enchantLvl;
        case MAGICAL_DEFEND:
          return 2 * enchantLvl;
		default:
			break;
        }
        return 0;
      case 8:
        switch (this.stat) {
        case PHYSICAL_DEFENSE:
          return 4 * enchantLvl;
        case MAXHP:
          return 12 * enchantLvl;
        case PHYSICAL_CRITICAL_RESIST:
          return 4 * enchantLvl;
        case MAGICAL_DEFEND:
          return 3 * enchantLvl;
		default:
			break;
        }
        return 0;
      }
      return 0;
    case CHAIN:
      switch (equipmentSlot) {
      case 16:
      case 32:
      case 2048:
        switch (this.stat) {
        case PHYSICAL_DEFENSE:
          return 3 * enchantLvl;
        case MAXHP:
          return 6 * enchantLvl;
        case PHYSICAL_CRITICAL_RESIST:
          return 2 * enchantLvl;
        case MAGICAL_DEFEND:
          return 2 * enchantLvl;
		default:
			break;
        }
        return 0;
      case 4096:
        switch (this.stat) {
        case PHYSICAL_DEFENSE:
          return 4 * enchantLvl;
        case MAXHP:
          return 8 * enchantLvl;
        case PHYSICAL_CRITICAL_RESIST:
          return 3 * enchantLvl;
        case MAGICAL_DEFEND:
          return 2 * enchantLvl;
		default:
			break;
        }
        return 0;
      case 8:
        switch (this.stat) {
        case PHYSICAL_DEFENSE:
          return 5 * enchantLvl;
        case MAXHP:
          return 10 * enchantLvl;
        case PHYSICAL_CRITICAL_RESIST:
          return 4 * enchantLvl;
        case MAGICAL_DEFEND:
          return 3 * enchantLvl;
		default:
			break;
        }
        return 0;
      }
      return 0;
    case PLATE:
      switch (equipmentSlot) {
      case 16:
      case 32:
      case 2048:
        switch (this.stat) {
        case PHYSICAL_DEFENSE:
          return 4 * enchantLvl;
        case MAXHP:
          return 4 * enchantLvl;
        case PHYSICAL_CRITICAL_RESIST:
          return 2 * enchantLvl;
        case MAGICAL_DEFEND:
          return 2 * enchantLvl;
		default:
			break;
        }
        return 0;
      case 4096:
        switch (this.stat) {
        case PHYSICAL_DEFENSE:
          return 5 * enchantLvl;
        case MAXHP:
          return 6 * enchantLvl;
        case PHYSICAL_CRITICAL_RESIST:
          return 3 * enchantLvl;
        case MAGICAL_DEFEND:
          return 2 * enchantLvl;
		default:
			break;
        }
        return 0;
      case 8:
        switch (this.stat) {
        case PHYSICAL_DEFENSE:
          return 6 * enchantLvl;
        case MAXHP:
          return 8 * enchantLvl;
        case PHYSICAL_CRITICAL_RESIST:
          return 4 * enchantLvl;
        case MAGICAL_DEFEND:
          return 3 * enchantLvl;
		default:
			break;
        }
        return 0;
      }
      return 0;
    case SHIELD:
      switch (this.stat) {
      case DAMAGE_REDUCE:
        float reduceRate = enchantLvl > 10 ? 0.2F : enchantLvl * 0.02F;
        return Math.round(reduceRate * applyStat.getBase());
      case BLOCK:
        if (enchantLvl > 10) {
          return 30 * (enchantLvl - 10);
        }
        return 0;
	default:
		break;
      }
      break;
	default:
		break;
    }
    return 0;
  }
}
