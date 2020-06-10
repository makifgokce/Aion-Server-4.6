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

public enum AccessLevelEnum {
	AccessLevel1(1, new int[] { 174, 175, 1904 }),
	AccessLevel2(2, new int[] { 174, 175, 1904 }),
	AccessLevel3(3, new int[] { 174, 175, 1904 }),
	AccessLevel4(4, new int[] { 174, 175, 1904 }),
	AccessLevel5(5, new int[] { 174, 175, 1904, 1911, 3224, 3226, 3227, 3232, 3233, 3234, 3235, 3236, 3237, 3238, 3239, 3240, 3241}),
	AccessLevel6(6, new int[] { 174, 175, 1904, 1911, 3224, 3226, 3227, 3232, 3233, 3234, 3235, 3236, 3237, 3238, 3239, 3240, 3241}),
	AccessLevel7(7, new int[] { 174, 175, 1904, 1911, 3224, 3226, 3227, 3232, 3233, 3234, 3235, 3236, 3237, 3238, 3239, 3240, 3241}),
	AccessLevel8(8, new int[] { 174, 175, 1904, 1911, 3224, 3226, 3227, 3232, 3233, 3234, 3235, 3236, 3237, 3238, 3239, 3240, 3241}),
	AccessLevel9(9, new int[] { 174, 175, 1904, 1911, 3224, 3226, 3227, 3232, 3233, 3234, 3235, 3236, 3237, 3238, 3239, 3240, 3241}),
	AccessLevel10(10, new int[] { 174, 175, 1904, 1911, 3224, 3226, 3227, 3232, 3233, 3234, 3235, 3236, 3237, 3238, 3239, 3240, 3241});

	private final int level;
	private int[] skills;

	AccessLevelEnum(int id, int[] skills) {
		this.level = id;
		this.skills = skills;
	}

	public int getLevel() {
		return level;
	}

	public int[] getSkills() {
		return skills;
	}

	public static AccessLevelEnum getAlType(int level) {
		for (AccessLevelEnum al : AccessLevelEnum.values()) {
			if (level == al.getLevel()) {
				return al;
			}
		}
		return null;
	}
}
