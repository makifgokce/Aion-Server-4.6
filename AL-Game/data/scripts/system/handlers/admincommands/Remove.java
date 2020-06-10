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

package admincommands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Phantom, ATracer
 */
public class Remove extends AdminCommand {

	public Remove() {
		super("remove");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 2) {
			PacketSendUtility.sendMessage(admin, "syntax //remove <player> <item ID> <quantity>");
			return;
		}

		int itemId = 0;
		long itemCount = 1;
		Player target = World.getInstance().findPlayer(Util.convertName(params[0]));
		if (target == null) {
			PacketSendUtility.sendMessage(admin, "Player isn't online.");
			return;
		}

		try {
			String itemStr = params[0];
			// Some item links have space before Id
			if (itemStr.equals("[item:")) {
				itemStr = params[1];
				Pattern id = Pattern.compile("(\\d{9})");
				Matcher result = id.matcher(itemStr);
				if (result.find()) {
					itemId = Integer.parseInt(result.group(1));
				}

				if (params.length == 3) {
					itemCount = Long.parseLong(params[2]);
				}
			} else {
				Pattern id = Pattern.compile("\\[item:(\\d{9})");
				Matcher result = id.matcher(itemStr);

				if (result.find()) {
					itemId = Integer.parseInt(result.group(1));
				} else {
					itemId = Integer.parseInt(params[0]);
				}

				if (params.length == 2) {
					itemCount = Long.parseLong(params[1]);
				}
			}
		} catch (NumberFormatException e) {
			try {
				String itemStr = params[1];
				// Some item links have space before Id
				if (itemStr.equals("[item:")) {
					itemStr = params[2];
					Pattern id = Pattern.compile("(\\d{9})");
					Matcher result = id.matcher(itemStr);
					if (result.find()) {
						itemId = Integer.parseInt(result.group(1));
					}

					if (params.length == 4) {
						itemCount = Long.parseLong(params[3]);
					}
				} else {
					Pattern id = Pattern.compile("\\[item:(\\d{9})");
					Matcher result = id.matcher(itemStr);

					if (result.find()) {
						itemId = Integer.parseInt(result.group(1));
					} else {
						itemId = Integer.parseInt(params[1]);
					}

					if (params.length == 3) {
						itemCount = Long.parseLong(params[2]);
					}
				}
			} catch (NumberFormatException ex) {
				PacketSendUtility.sendMessage(admin, "You must give number to itemid.");
				return;
			} catch (Exception ex2) {
				PacketSendUtility.sendMessage(admin, "Occurs an error.");
				return;
			}
		}

		Storage bag = target.getInventory();

		long itemsInBag = bag.getItemCountByItemId(itemId);
		if (itemsInBag == 0) {
			PacketSendUtility.sendMessage(admin, "Items with that id are not found in the player's bag.");
			return;
		}

		Item item = bag.getFirstItemByItemId(itemId);
		bag.decreaseByObjectId(item.getObjectId(), itemCount);

		PacketSendUtility.sendMessage(admin, "Item(s) removed succesfully");
		PacketSendUtility.sendMessage(target, "Admin removed an item from your bag");
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //remove <player> <item ID> <quantity>");
	}
}
