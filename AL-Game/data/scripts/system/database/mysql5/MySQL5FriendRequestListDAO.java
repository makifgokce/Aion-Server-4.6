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
package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.FriendRequestListDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.FriendRequest;
import com.aionemu.gameserver.model.gameobjects.player.FriendRequestList;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;

/**
 * @author Gokcee
 */
public class MySQL5FriendRequestListDAO extends FriendRequestListDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5FriendRequestListDAO.class);
	public static final String LOAD_QUERY = "SELECT * FROM `friends_request` WHERE `player`=?";
	public static final String ADD_QUERY = "INSERT INTO `friends_request` (`player`,`friend`,`note`, `expire_time`) VALUES (?, ?, ?, ?)";
	public static final String DEL_QUERY = "DELETE FROM `friends_request` WHERE `player` = ? AND `friend` = ?";
	public static final String AUTO_DEL_QUERY = "DELETE FROM `friends_request` WHERE `player` = ? ";

	@Override
	public FriendRequestList load(final PlayerCommonData pcd) {
		final List<FriendRequest> friends = new ArrayList<>();
		Connection con = null;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(LOAD_QUERY);
			stmt.setInt(1, pcd.getPlayerObjId());
			ResultSet rset = stmt.executeQuery();
			PlayerDAO dao = DAOManager.getDAO(PlayerDAO.class);
			while (rset.next()) {
				int objId = rset.getInt("friend");
				String note = rset.getString("note");
				int expireTime = rset.getInt("expire_time");

				PlayerCommonData friendPcd = dao.loadPlayerCommonData(objId);
				if (friendPcd != null) {
					FriendRequest friend = new FriendRequest(friendPcd, note, expireTime);
					friends.add(friend);
				}
			}
		}
		catch (Exception e) {
			log.error("Could not restore QuestStateList data for player: " + pcd.getPlayerObjId() + " from DB: " + e.getMessage(), e);
		}
		finally {
			DatabaseFactory.close(con);
		}

		return new FriendRequestList(pcd, friends);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addFriends(final int playerOid, final int friendOid, final String message, final int expireTime) {
		return DB.insertUpdate(ADD_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement ps) throws SQLException {
				ps.setInt(1, playerOid);
				ps.setInt(2, friendOid);
				ps.setString(3, message);
				ps.setInt(4, expireTime);
				ps.addBatch();
				ps.executeBatch();
			}
		});

	}

	@Override
	public boolean delFriends(final int playerOid, final int friendOid) {
		return DB.insertUpdate(DEL_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement ps) throws SQLException {
				ps.setInt(1, playerOid);
				ps.setInt(2, friendOid);
				ps.addBatch();
				ps.executeBatch();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}
}
