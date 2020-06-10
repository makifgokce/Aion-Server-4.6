package com.aionemu.gameserver.network.aion.gmhandler;

import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * Created by Kill3r
 */
public class CmdAddSkill extends AbstractGMHandler {

	public CmdAddSkill(Player admin,String params){
		super(admin,params);
		run();
	}

	public void run(){
		String skillName = params;
		List<SkillTemplate> skill = DataManager.SKILL_DATA.getSkillTemplates();

		for(SkillTemplate s : skill){
			if(s.getNamedesc().equalsIgnoreCase(skillName)){
				admin.getSkillList().addSkill(admin, s.getSkillId(), s.getLvl());
			}
		}
	}
}
