package com.sdjs.web.common.util;

import java.util.*;
import java.io.*;

import com.zte.ums.uep.api.ServiceAccess;
import com.zte.ums.uep.api.util.DebugPrn;

public class PropertyFileUtil
{
  private Properties              filePro      = new Properties();                                           ;
  private static final DebugPrn   dMsg         = new DebugPrn(PropertyFileUtil.class.getName());

  // 将value值写入aos-deploy.properties文件
  private static String           fileFullPath = ServiceAccess.getSystemLocalService().getInstallRootPath() + File.separator
                                                   + "ums-server/works/global/deploy/deploy-aos.properties";

  private static PropertyFileUtil util         = new PropertyFileUtil();

  private PropertyFileUtil()
  {

  }

  public static PropertyFileUtil getInstance()
  {
    return util;
  }

  /**
   * 加载文件
   * 
   * @param proFilePath
   */
  public void loadProFile()
  {
    BufferedReader bufferedreader = null;
    try
    {
      bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(fileFullPath), "GBK"));
      filePro.load(bufferedreader);
    }
    catch (Exception e)
    {
      dMsg.error("Loading properties file(" + fileFullPath + ") error.");
    }
    finally
    {
      try
      {
        if (bufferedreader != null)
          bufferedreader.close();
      }
      catch (Exception ignor)
      {

      }
    }
  }

  /**
   * 将属性写进文件
   * 
   * @param proFilePath
   * @param key
   * @param value
   * @return
   * @throws Exception 
   */
  public void saveProFile(String key, String value) throws Exception
  {
    saveProFile(new String[] { key }, new String[] { value });
  }

  public void saveProFile(String keys[], String values[]) throws Exception
  {
    for (int i = 0; i < keys.length; i++)
    {
      filePro.setProperty(keys[i], values[i]);
    }
    BufferedWriter bw = null;
    try
    {
      bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileFullPath), "GBK"));
      filePro.store(bw, "");
      bw.flush();
    }
    finally
    {
      try
      {
        if (bw != null)
          bw.close();
      }
      catch (Exception ignor)
      {

      }
    }
  }
}
