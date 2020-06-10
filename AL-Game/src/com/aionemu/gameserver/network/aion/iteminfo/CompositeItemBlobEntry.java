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

package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.ManaStone;

public class CompositeItemBlobEntry extends ItemBlobEntry {

	CompositeItemBlobEntry() {
		super(ItemInfoBlob.ItemBlobType.COMPOSITE_ITEM);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		Item item = this.ownerItem;

		writeD(buf, item.getFusionedItemId());
		writeFusionStones(buf);
		writeC(buf, item.hasOptionalFusionSocket() ? item.getOptionalFusionSocket() : 0);
        writeH(buf, 0);
	}

	private void writeFusionStones(ByteBuffer buf) {
		Item item = ownerItem;
        int count = 0;

        if (item.hasFusionStones()) {
            Set<ManaStone> itemStones = item.getFusionStones();
            ArrayList<ManaStone> basicStones = new ArrayList<>();
            ArrayList<ManaStone> ancientStones = new ArrayList<>();

            for (ManaStone itemStone : itemStones) {
                if (itemStone.isBasic()) {
                    basicStones.add(itemStone);
                } else {
                    ancientStones.add(itemStone);
                }
            }

            if (item.getFusionedItemTemplate().getSpecialSlots() > 0) {
                if (ancientStones.size() > 0) {
                    for (ManaStone ancientStone : ancientStones) {
                        if (count == 6) {
                            break;
                        }
                        writeD(buf, ancientStone.getItemId());
                        count++;
                    }
                }

                for (int i = count; i < item.getFusionedItemTemplate().getSpecialSlots(); i++) {
                    writeD(buf, 0);
                    count++;
                }
            }

            for (ManaStone basicFusionStone : basicStones) {
                if (count == 6) {
                    break;
                }
                writeD(buf, basicFusionStone.getItemId());
                count++;
            }
            skip(buf, (12 - count) * 4);
        } else {
            skip(buf, 48);
        }
	}

	@Override
	public int getSize() {
		return 55;
	}
}
