package com.sdjs.web.common.api;



import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.sdjs.mainframe.proto.ModuleMenu;
import com.zte.ums.aos.api.common.mainframe.entity.BaseEntity;



public class MenuItemEntity extends BaseEntity
{
  private String action = "";

  private String opCode = "";
  private String loginurl = "";

  public MenuItemEntity(String id, String i18nFile, String label, String action, String opCode, int order,String loginurl)
  {
    super(id, i18nFile, label, order);
    this.action = action;
    this.opCode = opCode;
    this.loginurl = loginurl;
  }

  public String getAction()
  {
    return action;
  }

  public String getOpCode()
  {
    return opCode;
  }
  
  

  public String getLoginurl() {
	return loginurl;
}

public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("==================MenuItem Start=====================\r\n");
    sb.append(super.toString());
    sb.append("action=" + this.action + "\r\n");
    sb.append("opCode=" + this.opCode + "\r\n");
    sb.append("==================MenuItem End=======================\r\n");
    return sb.toString();
  }

  public ModuleMenu toModuleMenu(Map<String, Boolean> rights, HashMap<String, File> i18nFileMap, Locale locale,String username,String passEncode)
  {
    ModuleMenu menu = new ModuleMenu();
    menu.setAction(this.action);
    menu.setLabel(this.getLocaleLabel(i18nFileMap, locale));
    menu.setId(this.id);
    if(this.loginurl!=null && this.loginurl.length() > 0){
    	String loginurlCopy = this.loginurl;
    	menu.setLoginurl(MessageFormat.format(loginurlCopy,username, passEncode));
    }    
    Boolean hasMenuRight = rights.get(this.opCode);
    if (hasMenuRight != null && !hasMenuRight)
    {
      menu.setRightLevel(MENU_UNVISIBLE);
    }
    else
    {
      menu.setRightLevel(MENU_VISIBLE);
    }
    return menu;
  }
}

