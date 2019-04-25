package com.sdjs.web.common;

import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import com.sdjs.mainframe.proto.ModuleViewsRequestMsg;
import com.sdjs.mainframe.proto.ModuleViewsResponseMsg;

import com.zte.ums.aos.api.AosServiceAccess;
import com.zte.ums.aos.api.common.mainframe.AosMainframeConst;
import com.zte.ums.aos.api.common.msg.CheckResRightRequest;
import com.zte.ums.aos.api.common.msg.CheckRightRequest;
import com.zte.ums.aos.api.framework.actor.AbstractModule;
import com.zte.ums.aos.api.framework.actor.Message;
import com.zte.ums.aos.api.framework.actor.MsgService;
import com.zte.ums.aos.api.framework.container.ContainerObj;
import com.zte.ums.aos.api.framework.msg.AosBooleanArrayWrapper;
import com.zte.ums.aos.api.unicom.msg.AosActorErrorInfo;
import com.zte.ums.aos.common.logic.mainframe.util.BackHandler;
import com.zte.ums.aos.common.logic.mainframe.util.SecurityUtil;
import com.zte.ums.aos.utils.HttpRequestUtils;
import com.zte.ums.uep.api.util.DebugPrn;

public class WebCommonActor extends AbstractModule
{
  private boolean                 isReady      = false;

  private static final DebugPrn   dMsg         = new DebugPrn(WebCommonActor.class.getName());

  private static final MsgService msgService   = AosServiceAccess.getMsgService();

  private ManageCenter            manageCenter = ManageCenter.getInstance();


  @Override
  public boolean isReady()
  {
    return isReady;
  }

  @Override
  public void onMonitor(PrintWriter arg0, String arg1)
  {

  }

  @Override
  public void start()
  {
    dMsg.info("webcommon-actor start");
    try
    {
      manageCenter.load();
      msgService.interestTopic(MsgService.TOPIC_MODULE_STARTUP);
    }
    catch (Exception e)
    {
      dMsg.error("webcommon-actor start fail.", e);
    }    
  }

  @Override
  public void stop()
  {
    dMsg.info("webcommon-actor");
  }

  @Override
  public void handleMessage(final Message aMessage)
  {
    switch (aMessage.getMessageID())
    {
      // 主题消息命令码
      case MsgService.MSG_SYSTEM_PUTLIC_MODULE_STARTUP_TOPIC:
      {
        String moduleName = aMessage.getMessageBody().getStringProperty(MsgService.MODULE_STARTUP_KEY);
        if (moduleName.equals("aos-sm-service-actor"))
        {
          isReady = true;
          dMsg.keyInfo("web-frame-actor is started.");
        }
        break;
      }
        // 操作鉴权
      case CommonConst.REQ_CHECK_RIGHT:
      {
        CheckRightRequest message = (CheckRightRequest) aMessage.getMessageBody();
        String userName = message.getAosEnv().getUserName();
        List<String> operations = message.getOperationsList();
        SecurityUtil.checkRight(userName, operations, null, new BackHandler<AosBooleanArrayWrapper>()
        {
          @Override
          public void handle(AosBooleanArrayWrapper aResult, Throwable aException)
          {
            if (aException != null)
            {
              AosActorErrorInfo clientErrorMsg = new AosActorErrorInfo();
              clientErrorMsg.setErrorDesc("check right failed!");
              Message responseMsg = msgService.createMessage(AosMainframeConst.RESP_CHECK_RIGHT, null, clientErrorMsg);
              msgService.responsePostMessage(aMessage, responseMsg);
            }
            else
            {
              Message responseMsg = msgService.createMessage(AosMainframeConst.RESP_CHECK_RIGHT, null, aResult);
              msgService.responsePostMessage(aMessage, responseMsg);
            }
          }
        });
        break;
      }
        // 资源鉴权
      case CommonConst.REQ_RES_RIGHT:
      {
        CheckResRightRequest message = (CheckResRightRequest) aMessage.getMessageBody();
        String userName = message.getAosEnv().getUserName();
        List<String> operations = message.getOperationsList();
        List<String> resources = message.getResourcesList();
        SecurityUtil.checkRight(userName, operations, resources, new BackHandler<AosBooleanArrayWrapper>()
        {
          @Override
          public void handle(AosBooleanArrayWrapper aResult, Throwable aException)
          {
            if (aException != null)
            {
              AosActorErrorInfo clientErrorMsg = new AosActorErrorInfo();
              clientErrorMsg.setErrorDesc("check right failed!");
              Message responseMsg = msgService.createMessage(AosMainframeConst.RESP_RES_RIGHT, null, clientErrorMsg);
              msgService.responsePostMessage(aMessage, responseMsg);
            }
            else
            {
              Message responseMsg = msgService.createMessage(AosMainframeConst.RESP_RES_RIGHT, null, aResult);
              msgService.responsePostMessage(aMessage, responseMsg);
            }
          }
        });
        break;
      }
      case CommonConst.REQ_MODULE_VIEWS:
      {
        final long start = System.currentTimeMillis();
        ModuleViewsRequestMsg ms = (ModuleViewsRequestMsg) aMessage.getMessageBody();
        String lang = ms.getClntEnv().getLanguage();
        Locale locale = HttpRequestUtils.getLocal(lang);
        String username = ms.getClntEnv().getUserName();
        String psencode = ms.getClntEnv().getPsencode();
        manageCenter.queryModuleViews(locale, username,psencode, new BackHandler<ModuleViewsResponseMsg>()
        {
          @Override
          public void handle(ModuleViewsResponseMsg aResult, Throwable aException)
          {
            ContainerObj result = null;
            if (aException != null)
            {
              AosActorErrorInfo error = new AosActorErrorInfo();
              error.setErrorDesc("query menu fail.");
              result = error;
            }
            else
            {
              result = aResult;
            }
            Message responseMsg = msgService.createMessage(AosMainframeConst.RESP_MODULE_VIEWS, null, result);
            msgService.responsePostMessage(aMessage, responseMsg);
            long time = System.currentTimeMillis() - start;
            dMsg.info("query views cost " + time);
          }
        });
        break;
      }
     
      default:
      {
        dMsg.warn("i cant deal this message : id=" + aMessage.getMessageID() + ",soruceActorId=" + aMessage.getSourceActorID());
      }
    }
  }
}
