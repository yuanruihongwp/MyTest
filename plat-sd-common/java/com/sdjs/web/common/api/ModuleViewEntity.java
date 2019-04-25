package com.sdjs.web.common.api;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sdjs.mainframe.proto.ModuleMenuGroup;
import com.sdjs.mainframe.proto.ModuleView;
import com.zte.ums.aos.api.common.mainframe.entity.BaseEntity;



public class ModuleViewEntity extends BaseEntity
{
  private String            pageUrl    = "";

  private String            opCode     = "";

  private boolean           background = false;
  
  private String            defaultaction     = "";
  
  private List<GroupEntity> groups     = new ArrayList<GroupEntity>();

  public ModuleViewEntity(String id, String i18nFile, String label, String opCode, int order, boolean background,String defaultaction)
  {
    super(id, i18nFile, label, order);
    this.opCode = opCode;
    this.background = background;
    this.defaultaction = defaultaction;
  }

  public String getPageUrl()
  {
    return pageUrl;
  }

  public void setPageUrl(String pageUrl)
  {
    this.pageUrl = pageUrl;
  }

  public String getOpCode()
  {
    return opCode;
  }

  public boolean isBackground()
  {
    return background;
  }
  
  

  public String getDefaultaction() {
	return defaultaction;
}

public void setDefaultaction(String defaultaction) {
	this.defaultaction = defaultaction;
}

public List<GroupEntity> getGroups()
  {
    return groups;
  }

  public void setGroups(List<GroupEntity> groups)
  {
    this.groups = groups;
  }

  public void addGroup(GroupEntity group)
  {
    this.groups.add(group);
  }
  

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("=================ModuleView Start====================\r\n");
    sb.append(super.toString());
    sb.append("pageUrl=" + this.pageUrl + "\r\n");
    sb.append("opCode=" + this.opCode + "\r\n");
    sb.append("background=" + this.background + "\r\n");
    for (GroupEntity group : groups)
    {
      sb.append(group.toString());
    }
    sb.append("=================ModuleView End======================\r\n");
    return sb.toString();
  }

  public List<BaseEntity> getChildren()
  {
    List<BaseEntity> list = new ArrayList<BaseEntity>();
    for (GroupEntity group : groups)
    {
      list.add(group);
    }
    return list;
  }

  public void setChildren(List<BaseEntity> children)
  {
    List<GroupEntity> tempGroups = new ArrayList<GroupEntity>();
    for (BaseEntity child : children)
    {
      tempGroups.add((GroupEntity) child);
    }
    groups = tempGroups;
  }

  public ModuleView toModuleView(Map<String, Boolean> rights, HashMap<String, File> i18nFileMap, Locale locale,String username,String passEncode)
  {
    ModuleView view = new ModuleView();
    view.setId(this.getId());
    view.setLabel(this.getLocaleLabel(i18nFileMap, locale));
    view.setPageUrl(this.getPageUrl());
    view.setBackground(this.isBackground());
    if(this.defaultaction!=null){
    	view.setDefaultaction(this.defaultaction);
    }    
    Boolean hasRight = rights.get(this.getOpCode());
    if (hasRight != null && !hasRight)
    {
      view.setRightLevel(MENU_UNVISIBLE);
    }
    else
    {
      view.setRightLevel(MENU_VISIBLE);
    }
    List<ModuleMenuGroup> list = new ArrayList<ModuleMenuGroup>();
    if (this.groups.size() > 0)
    {
      for (GroupEntity groupEntity : groups)
      {
        list.add(groupEntity.toModuleMenuGroup(rights, i18nFileMap, locale, username, passEncode));
      }
    }
    view.setMenuGroupsList(list);
    return view;
  }

  /**
   * 检查分组order指定是否合法并排序
   * 
   * @throws Exception
   */
  public void toCheckAndSort() throws Exception
  {
    if (this.groups.size() > 0)
    {
      // 第一个分组作为是否排序的标志
      boolean isSort = (groups.get(0).getOrder() != Integer.MAX_VALUE);
      for (GroupEntity groupEntity : groups)
      {
        groupEntity.check(isSort);
        groupEntity.toCheckAndSort(isSort);
      }
      Collections.sort(groups);
    }
  }
}
