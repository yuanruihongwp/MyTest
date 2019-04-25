package com.sdjs.web.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jdom.Element;

import com.sdjs.web.common.api.GroupEntity;
import com.sdjs.web.common.api.MenuItemEntity;
import com.sdjs.web.common.api.ModuleViewEntity;
import com.zte.ums.aos.api.common.mainframe.entity.BaseEntity;
import com.zte.ums.uep.api.ServiceAccess;
import com.zte.ums.uep.api.util.DebugPrn;


public class WebCommonInit
{
  private static final String         ACTION        = "action";

  private static final String         MENU          = "menu";

  private static final String         GROUP         = "group";

  // private static final String MENUS = "menus";

  private static final String         PAGE          = "page";

  private static final String         ORDER         = "order";

  private static final String         ID            = "id";

  private static final String         I18N_LABEL    = "i18n-label";

  private static final String         MODULE_VIEW   = "module-view";

  private static final String         CONFIGURATION = "configuration";

  private static final String         I18N          = "i18n";

  private static final String         OPERATION     = "operation";

  private static final String         BACKGROUND    = "background";
  
  private static final String           ICON  = "icon";
  
  private static final String           DEFAULTACTION  = "defaultaction";
  
  private static final String           LONGINURL  = "loginurl";

  // 递归解析的结果集
  private List<ModuleViewEntity>      viewList      = new ArrayList<ModuleViewEntity>();

  // 递归使用的内部序号
  private int                         innerOrder    = 1;

  // 递归解析的结果集
  private HashMap<String, BaseEntity> viewMap       = new HashMap<String, BaseEntity>();

  private static final DebugPrn       dMsg          = new DebugPrn(WebCommonInit.class.getName());

  public List<ModuleViewEntity> init() throws Exception
  {
	  String path = ServiceAccess.getSystemLocalService()
				.getInstallRootPath()
				+ "/ums-server/procs/ppus/ws.ppu/sd-common.pmu/sd-common.par/conf/sdjs-menu.xml";
   
      Element rootElement = FileConfigWriter.getRootElementFromXMLFile(new File(path));
      parse(rootElement, null, null);
    
    // 合并
    List<BaseEntity> beforeList = new ArrayList<BaseEntity>();
    // 类型转换成父类为合并
    for (BaseEntity view : viewList)
    {
      beforeList.add(view);
    }
    List<BaseEntity> list = combine(beforeList);
    // 对ModuleView排序
    Collections.sort(list);
    // 合并后将类型转换成子类
    List<ModuleViewEntity> afterList = new ArrayList<ModuleViewEntity>();
    for (BaseEntity view : list)
    {
      ModuleViewEntity viewEntity = (ModuleViewEntity) view;
      // 对ModuleView内部进行排序
      viewEntity.toCheckAndSort();
      afterList.add(viewEntity);
      dMsg.info((ModuleViewEntity) view);
    }
   return afterList;
  }

  /**
   * 对各个相同module id的菜单进行合并，合并组和菜单项
   * 
   * @param list
   *          待合并的菜单、组、菜单项列表
   * @return 合并后的菜单、组、菜单项列表
   */
  private List<BaseEntity> combine(List<BaseEntity> list)
  {
    HashMap<String, BaseEntity> map = new HashMap<String, BaseEntity>();
    for (BaseEntity entity : list)
    {
      String key = entity.getId().equals("") ? entity.getLabel() : entity.getId();// 兼容
      BaseEntity mapEntity = map.get(key);
      if (mapEntity != null)
      {
        if (entity.isBaseConfig())
        {
          map.put(key, entity);
          // 调换顺序
          BaseEntity temp = mapEntity;
          mapEntity = entity;
          entity = temp;
        }
        // 自身合并
        mapEntity.getAllOpCode().addAll(entity.getAllOpCode());
        // 孩子合并
        List<BaseEntity> children1 = mapEntity.getChildren();
        List<BaseEntity> children2 = entity.getChildren();
        if (children1.size() > 0 || children2.size() > 0)
        {
          children1.addAll(children2);
          mapEntity.setChildren(combine(children1));
        }
      }
      else
      {
        map.put(key, entity);
      }
    }
    return Arrays.asList(map.values().toArray(new BaseEntity[0]));
  }

  /**
   * 对每个DESC_FILE_NAME的内容进行解析
   * 
   * @param element
   *          元素
   * @param i18nFile
   *          i18n文件
   * @param obj
   *          上一个对象
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private void parse(Element element, String i18nFile, Object obj) throws Exception
  {
    String name = element.getName();
    if (name.equals(CONFIGURATION))
    {
      String i18nFileValue = getAttributeValue(I18N, element, false);
      List<Element> children = element.getChildren();
      for (Element child : children)
      {
        parse(child, i18nFileValue, null);
      }
    }
    else if (name.equals(MODULE_VIEW))
    {
      String id = getAttributeValue(ID, element, false);
      String label = getAttributeValue(I18N_LABEL, element, true);
      String opCode = getAttributeValue(OPERATION, element, true);
      String defaultaction = getAttributeValue(DEFAULTACTION, element, true);
      int order = getAttributeValue(ORDER, element, true).equals("") ? Integer.MAX_VALUE : Integer.parseInt(getAttributeValue(ORDER,
          element, true));
      boolean backgroud = getAttributeValue(BACKGROUND, element, true).equals("") ? false : Boolean.parseBoolean(getAttributeValue(ORDER,
          element, true));
//      String icon = getAttributeValue(ICON, element, true);
      ModuleViewEntity view = new ModuleViewEntity(id, i18nFile, label, opCode, order, backgroud,defaultaction);
      List<Element> children = element.getChildren();
      for (Element child : children)
      {
        parse(child, i18nFile, view);
      }
      viewList.add(view);
      // 收集操作码
      if (!opCode.equals(""))
        view.getAllOpCode().add(opCode);
      // 检查合法性
      check(id, view);
    }
    else if (name.equals(PAGE)) //page就是图标
    {
      String pageUrl = element.getTextTrim() == null ? "" : element.getTextTrim();
      ModuleViewEntity view = (ModuleViewEntity) obj;
      view.setPageUrl(pageUrl);
    }
    else if (name.equals(GROUP))
    {
      String id = getAttributeValue(ID, element, false);// 兼容 false
      String label = getAttributeValue(I18N_LABEL, element, true);
      int order = getAttributeValue(ORDER, element, true).equals("") ? Integer.MAX_VALUE : Integer.parseInt(getAttributeValue(ORDER,
          element, true));
      GroupEntity group = new GroupEntity(id, i18nFile, label, order);
      group.setInnerOrder(innerOrder++);
      ModuleViewEntity view = (ModuleViewEntity) obj;
      view.addGroup(group);
      List<Element> children = element.getChildren();
      for (Element child : children)
      {
        parse(child, i18nFile, group);
      }
      // 收集操作码
      if (group.getAllOpCode().size() > 0)
        view.getAllOpCode().addAll(group.getAllOpCode());
      // 检查合法性
      check(view.getId() + id, group);
    }
    else if (name.equals(MENU))
    {
      String id = getAttributeValue(ID, element, false);// 兼容 false
      String label = getAttributeValue(I18N_LABEL, element, false);
      String action = getAttributeValue(ACTION, element, false);
      String opCode = getAttributeValue(OPERATION, element, true);
      String loginurl = getAttributeValue(LONGINURL, element, true);
      int order = getAttributeValue(ORDER, element, true).equals("") ? Integer.MAX_VALUE : Integer.parseInt(getAttributeValue(ORDER,
          element, true));
      MenuItemEntity item = new MenuItemEntity(id, i18nFile, label, action, opCode, order,loginurl);
      item.setInnerOrder(innerOrder++);
      GroupEntity group = (GroupEntity) obj;
      group.addItem(item);
      // 收集操作码
      if (!opCode.equals(""))
        group.getAllOpCode().add(opCode);
    }
    else
    {
      throw new Exception(name + " tag should not exist in menu file");
      // 兼容menus标签
      /*
       * List<Element> children = element.getChildren(); if (children != null) {
       * for (Element child : children) { parse(child, i18nFile, obj); } }
       */
    }
  }

  /**
   * 获取属性
   * 
   * @param key
   *          属性key
   * @param element
   *          xml元素
   * @param isPermitNull
   *          是否允许为空标志
   * @return 属性value
   * @throws Exception
   *           标志不允许为空，但值为空
   */
  public String getAttributeValue(String key, Element element, boolean isPermitNull) throws Exception
  {
    String value = element.getAttributeValue(key);
    if (value != null && !value.trim().equals(""))
      return value;
    else
    {
      if (isPermitNull)
      {
        return "";
      }
      else
      {
        throw new Exception(element.getName() + " tag's " + key + " attribute is not permit null in menu file");
      }
    }
  }

  /**
   * 检查合并菜单中是否有多余的i18n-label标签，该标签是判断是否为基准的依据，所以不能多余
   * 该方法无法判断空白分组有重复i18n-label的情况，因为空白分组本身就是不配置该属性或者配置为空串
   * 
   * @param id
   * @param e
   * @throws Exception
   */
  public void check(String id, BaseEntity e) throws Exception
  {
    if (!e.getLabel().equals(""))
    {
      if (viewMap.get(id) != null)
        throw new Exception(e.getId() + " has needless i18n-label");
      else
      {
        viewMap.put(id, e);
      }
    }
  }
}
