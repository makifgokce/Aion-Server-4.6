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

package com.aionemu.gameserver.dataholders;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.drop.Drop;
import com.aionemu.gameserver.model.drop.DropGroup;
import com.aionemu.gameserver.model.drop.NpcDrop;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;

/**
 * @author MrPoke
 *
 */
public class NpcDropData {

	private static Logger log = LoggerFactory.getLogger(DataManager.class);
	private List<NpcDrop> npcDrop;
	private static FileChannel roChannel;

	/**
	 * @return the npcDrop
	 */
	public List<NpcDrop> getNpcDrop() {
		return npcDrop;
	}

	/**
	 * @param npcDrop
	 *            the npcDrop to set
	 */
	public void setNpcDrop(List<NpcDrop> npcDrop) {
		this.npcDrop = npcDrop;
	}

	public int size() {
		return npcDrop.size();
	}

	@SuppressWarnings("resource")
	public static NpcDropData load() {

		List<Drop> drops = new ArrayList<>();
		List<String> names = new ArrayList<>();
		List<NpcDrop> npcDrops = new ArrayList<>();
		roChannel = null;
		MappedByteBuffer buffer;
		HashMap<Integer, ArrayList<DropGroup>> xmlGroup = DataManager.XML_NPC_DROP_DATA.getDrops();
		try {
			roChannel = new RandomAccessFile("data/static_data/npc_drops/npc_drop.dat", "r").getChannel();
			int size = (int) roChannel.size();
			buffer = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size).load();
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			int count = buffer.getInt();
			for (int i = 0; i < count; i++) {
				drops.add(Drop.load(buffer));
			}

			count = buffer.getInt();

			for (int i = 0; i < count; i++) {
				int lenght = buffer.get();
				byte[] byteString = new byte[lenght];
				buffer.get(byteString);
				String name = new String(byteString);
				names.add(name);
			}

			count = buffer.getInt();
			for (int i = 0; i < count; i++) {
				int npcId = buffer.getInt();

				int groupCount = buffer.getInt();
				List<DropGroup> dropGroupList = new ArrayList<>(groupCount);
				for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
					Race race;
					byte raceId = buffer.get();
					switch (raceId) {
						case 0:
							race = Race.ELYOS;
							break;
						case 1:
							race = Race.ASMODIANS;
							break;
						default:
							race = Race.PC_ALL;
					}

					boolean useCategory = buffer.get() == 1;
					String groupName = names.get(buffer.getShort());

					int dropCount = buffer.getInt();
					List<Drop> dropList = new ArrayList<>(dropCount);
					for (int dropIndex = 0; dropIndex < dropCount; dropIndex++) {
						dropList.add(drops.get(buffer.getInt()));
					}
					DropGroup dropGroup = new DropGroup(dropList, race, useCategory, groupName);
					dropGroupList.add(dropGroup);
				}
        		if (xmlGroup.get(npcId) != null) {
          			dropGroupList.addAll(xmlGroup.get(npcId));
          			xmlGroup.remove(Integer.valueOf(npcId));
        		}
				NpcDrop npcDrop = new NpcDrop(dropGroupList, npcId);
				npcDrops.add(npcDrop);

				NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(npcId);
				if (npcTemplate != null) {
					npcTemplate.setNpcDrop(npcDrop);
				}
			}
      		if (!xmlGroup.isEmpty()) {
        		Iterator<Map.Entry<Integer, ArrayList<DropGroup>>> iter = xmlGroup.entrySet().iterator();
        		while (iter.hasNext()) {
          			Map.Entry<Integer, ArrayList<DropGroup>> entry = iter.next();
          			NpcDrop npcDrop = new NpcDrop(entry.getValue(), entry.getKey().intValue());
          			npcDrops.add(npcDrop);
          			NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(entry.getKey().intValue());
          			if (npcTemplate != null) {
            			npcTemplate.setNpcDrop(npcDrop);
          			}
        		}
      		}
			drops.clear();
			drops = null;
			names.clear();
			names = null;
      		xmlGroup.clear();
      		xmlGroup = null;
      		DataManager.XML_NPC_DROP_DATA.clear();
      		try {
        		if (roChannel != null) {
          			roChannel.close();
        		}
      		}
      		catch (IOException e) {
        		log.error("Drop loader: IO error in drop Loading.");
      		}
      		new NpcDropData();
		} catch (FileNotFoundException e) {
			log.error("Drop loader: Missing npc_drop.dat!!!");
		} catch (IOException e) {
			log.error("Drop loader: IO error in drop Loading.");
		} finally {
			try {
				if (roChannel != null) {
					roChannel.close();
				}

			} catch (IOException e) {
				log.error("Drop loader: IO error in drop Loading.");
			}
		}
		NpcDropData dropData = new NpcDropData();
		log.info("Drop loader: Npc drops loading done.");
		dropData.setNpcDrop(npcDrops);
		return dropData;

	}

	public static void reload() {
		TIntObjectHashMap<NpcTemplate> npcData = DataManager.NPC_DATA.getNpcData();
		npcData.forEachValue(new TObjectProcedure<NpcTemplate>() {
			@Override
			public boolean execute(NpcTemplate npcTemplate) {
				npcTemplate.setNpcDrop(null);
				return false;
			}
		});
		load();
	}
}
