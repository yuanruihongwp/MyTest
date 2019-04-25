package com.sdjs.web.common.util;

import java.io.File;
import java.util.HashMap;

import com.zte.ums.uep.api.ServiceAccess;
import com.zte.ums.usf.bsf.system.server.DeployInfoTool;
import com.zte.ums.usf.bsf.system.server.ServerProcessTool;

public class FileConfigLoader
{

 
  public static HashMap<String, File> loadI18nFile()
  {
    HashMap<String, File> i18nFileMap = new HashMap<String, File>();
    String pattern = "*-i18n.xml";
    String installRootPath = ServiceAccess.getSystemLocalService().getInstallRootPath();
    ServerProcessTool serverProcessTool = new ServerProcessTool(installRootPath);
    String[] availableProcesses = serverProcessTool.listAvailableProcesses();
    for (String processName : availableProcesses)
    {
      try
      {
        DeployInfoTool deployInfoTool = new DeployInfoTool(installRootPath, processName);
        File[] filesInPPUs = deployInfoTool.searchFilesInPPUs(pattern);
        File[] filesInUSF = deployInfoTool.searchFilesInUSF(pattern);
        if (filesInPPUs != null && filesInPPUs.length > 0)
        {
          for (File file : filesInPPUs)
          {
            i18nFileMap.put(file.getName(), file);
          }
        }
        if (filesInUSF != null && filesInUSF.length > 0)
        {
          for (File file : filesInUSF)
          {
            i18nFileMap.put(file.getName(), file);
          }
        }
      }
      catch (Exception ignor)
      {
      }
    }
    return i18nFileMap;
  }
}
