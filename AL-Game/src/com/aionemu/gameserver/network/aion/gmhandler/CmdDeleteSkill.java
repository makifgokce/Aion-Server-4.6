package com.aionemu.gameserver.network.aion.gmhandler;

import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.skill.PlayerSkillList;
import com.aionemu.gameserver.services.SkillLearnService;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * Created by Kill3r
 */
public class CmdDeleteSkill extends AbstractGMHandler {

    public CmdDeleteSkill(Player admin, String params){
        super(admin,params);
        run();
    }

    public void run(){
        String skillName = params;
        PlayerSkillList playerSkillList = admin.getSkillList();
        List<SkillTemplate> skill = DataManager.SKILL_DATA.getSkillTemplates();

        for(SkillTemplate s : skill){
            if(s.getNamedesc().equalsIgnoreCase(skillName)){
                for (PlayerSkillEntry skillEntry : playerSkillList.getAllSkills()) {
                    if (!skillEntry.isStigma()) {
                        SkillLearnService.removeSkill(admin, skillEntry.getSkillId());
                    }
                }
            }
        }

    }
}
