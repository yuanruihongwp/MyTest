package com.sdjs.web.common.util;

import java.util.ArrayList;
import java.util.List;

import com.zte.ums.aos.api.AosServiceAccess;
import com.zte.ums.aos.api.framework.actor.Message;
import com.zte.ums.aos.api.framework.actor.MessageCallback;
import com.zte.ums.aos.api.framework.msg.AosBooleanArrayWrapper;
import com.zte.ums.aos.api.sm.msg.AosCheckRightMsg;
import com.zte.ums.aos.sm.common.AosSmMsgCodeConstExt;
import com.zte.ums.aos.sm.common.SmConsts;

public class SecurityUtil {
	public static void checkRight(String username, List<String> operations,
			List<String> resources,
			final BackHandler<AosBooleanArrayWrapper> handler) {
		AosCheckRightMsg checkRightMsg = new AosCheckRightMsg();
		checkRightMsg.setUserName(username);
		checkRightMsg.setOperationsList(operations);
		if (resources != null)
			checkRightMsg.setResourcesList(resources);
		Message msg = AosServiceAccess.getMsgService().createMessage(
				AosSmMsgCodeConstExt.REQUEST_BATCHCHECKRIGHT,
				SmConsts.ACTOR_ID_SMSERVICE, checkRightMsg);
		AosServiceAccess.getMsgService().requestPostMessage(msg,
				new MessageCallback<AosBooleanArrayWrapper>() {
					@Override
					protected void doCallback(AosBooleanArrayWrapper aResult,
							Throwable aException) {
						handler.handle(aResult, aException);
					}
				});
	}

	public static void checkRightByOneRes(String username,
			List<String> operations, String resource,
			final BackHandler<AosBooleanArrayWrapper> handler) {
		AosCheckRightMsg body = new AosCheckRightMsg();
		body.setUserName(username);
		body.setOperationsList(operations);
		
		ArrayList<String> resList = new ArrayList<String>();
		resList.add(resource);
		body.setResourcesList(resList);
		
		Message msg = AosServiceAccess.getMsgService().createMessage(AosSmMsgCodeConstExt.REQUEST_BATCHCHECKRESOURCERIGHT, SmConsts.ACTOR_ID_SMSERVICE, body);
		AosServiceAccess.getMsgService().requestPostMessage(msg, new MessageCallback<AosBooleanArrayWrapper>() {
			@Override
			protected void doCallback(AosBooleanArrayWrapper aResult,
					Throwable aException) {
				handler.handle(aResult, aException);
			}
		});
	}
}
