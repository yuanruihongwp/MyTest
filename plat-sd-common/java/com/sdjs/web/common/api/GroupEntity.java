package com.sdjs.web.common.api;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sdjs.mainframe.proto.ModuleMenu;
import com.sdjs.mainframe.proto.ModuleMenuGroup;
import com.zte.ums.aos.api.common.mainframe.entity.BaseEntity;



public class GroupEntity extends BaseEntity
{
  private List<MenuItemEntity> items = new ArrayList<MenuItemEntity>();

  public GroupEntity(String id, String i18nFile, String label, int order)
  {
    super(id, i18nFile, label, order);
  }

  public List<MenuItemEntity> getItems()
  {
    return items;
  }

  public void addItem(MenuItemEntity item)
  {
    this.items.add(item);
  }

  public List<BaseEntity> getChildren()
  {
    List<BaseEntity> list = new ArrayList<BaseEntity>();
    for (MenuItemEntity item : items)
    {
      list.add(item);
    }
    return list;
  }

  public void setChildren(List<BaseEntity> children)
  {
    List<MenuItemEntity> tempItems = new ArrayList<MenuItemEntity>();
    for (BaseEntity child : children)
    {
      tempItems.add((MenuItemEntity) child);
    }
    items = tempItems;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("=====================Group Start=====================\r\n");
    sb.append(super.toString());
    for (MenuItemEntity item : items)
    {
      sb.append(item.toString());
    }
    sb.append("=====================Group End=======================\r\n");
    return sb.toString();
  }

  public ModuleMenuGroup toModuleMenuGroup(Map<String, Boolean> rights, HashMap<String, File> i18nFileMap, Locale locale,String username,String passEncode)
  {
    ModuleMenuGroup group = new ModuleMenuGroup();
    String glabel = this.getLocaleLabel(i18nFileMap, locale);
    group.setLabel(glabel == null ? "" : glabel);
    List<ModuleMenu> list = new ArrayList<ModuleMenu>();
    if (this.items.size() > 0)
    {
      for (MenuItemEntity item : items)
      {
        list.add(item.toModuleMenu(rights, i18nFileMap, locale,username,passEncode));
      }
    }
    group.setMenusList(list);
    return group;
  }

  /**
   * 检查菜单项order指定是否合法并排序
   * @param isSort 是否排序标志
   * @throws Exception
   */
  public void toCheckAndSort(boolean isSort) throws Exception
  {
    if (this.items.size() > 0)
    {
      for (MenuItemEntity item : items)
      {
        item.check(isSort);
      }
      Collections.sort(items);
    }
  }

  /**
   * 没有指定order，如果是空白分组则设置为0，排在前面 否则使用内部排序
   */
  public void setOrder()
  {
    if (this.getLabel().equals(""))
    {
      this.order = 0;
    }
    else
    {
      this.order = this.innerOrder;
    }
  }
}
