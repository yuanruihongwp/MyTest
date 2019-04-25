package com.sdjs.web.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.sdjs.mainframe.proto.ModuleView;
import com.sdjs.mainframe.proto.ModuleViewsResponseMsg;
import com.sdjs.web.common.api.ModuleViewEntity;
import com.sdjs.web.common.util.FileConfigLoader;
import com.zte.ums.aos.api.framework.msg.AosBooleanArrayWrapper;
import com.zte.ums.aos.common.logic.mainframe.util.BackHandler;
import com.zte.ums.aos.common.logic.mainframe.util.SecurityUtil;
import com.zte.ums.api.common.resource.ppu.ResourceException;
import com.zte.ums.uep.api.ServiceAccess;
import com.zte.ums.uep.api.ServiceNotFoundException;
import com.zte.ums.uep.api.psl.systemsupport.SystemSupportService;
import com.zte.ums.uep.api.util.DebugPrn;

public class ManageCenter {

	private static ManageCenter center = new ManageCenter();


//	// 这个是和前台页面约定的键值，不会出现在配置文件中
//	private static final String USERNAME_KEY = "/aos/username";
//
//	private static final String IS_FROM_PORTAL_KEY = "/aos/isfromportal";
//
//	private static final String AOS_DISPLAY_VERSION = "ums.version.display";
//
//	private static final String AOS_MAIN_VERSION = "ums.version.main";
//
//	private static final String AOS_PATCH_VERSION = "ums.version.patch";

	private static final DebugPrn dMsg = new DebugPrn(
			ManageCenter.class.getName());

	// private static Properties props = new Properties();

	private List<ModuleViewEntity> viewEntityList = null;

	private static SystemSupportService systemSupportService = ServiceAccess
			.getSystemSupportService();

	private HashMap<String, File> i18nFileMap = null;

	public static ManageCenter getInstance() {
		return center;
	}

	private ManageCenter() {

	}
	

	@SuppressWarnings("unchecked")
	public void load() throws Exception {
		viewEntityList = new WebCommonInit().init();
		i18nFileMap = FileConfigLoader.loadI18nFile();
	}

	public void queryModuleViews(final Locale locale, final String username,final String passEncode,
			final BackHandler<ModuleViewsResponseMsg> handler) {
		Set<String> moduleViewOperations = new HashSet<String>();
		for (ModuleViewEntity entity : viewEntityList) {
			moduleViewOperations.addAll(entity.getAllOpCode());
		}
		if (moduleViewOperations.size() == 0) {
			Map<String, Boolean> map = Collections.emptyMap();
			handler.handle(convertViews(locale, map,username, passEncode), null);
		} else {
			final List<String> operationsList = new ArrayList<String>(
					moduleViewOperations);
			String strRootOid = "";
			try {
				strRootOid = ServiceAccess.getResourcePPUServerService()
						.getRoot().getOid();
			} catch (ResourceException e) {
				dMsg.error("Get Resource PPU fail.", e);
			} catch (ServiceNotFoundException e) {
				dMsg.error("Get Resource PPU fail.", e);
			}
			// SecurityUtil.checkRightByOneRes(username, operationsList,
			// strRootOid, new BackHandler<AosBooleanArrayWrapper>() {
			// @Override
			// public void handle(AosBooleanArrayWrapper aResult,
			// Throwable aException) {
			// if (aException != null) {
			// dMsg.error("check right failed", aException);
			// handler.handle(null, aException);
			// } else {
			// Map<String, Boolean> rights = new HashMap<String, Boolean>();
			// Iterator<Boolean> resultIter = aResult
			// .getValueList().iterator();
			// for (String op : operationsList) {
			// rights.put(op, resultIter.next());
			// }
			// handler.handle(convertViews(locale, rights),
			// null);
			// }
			// }
			// });
			List<String> listResource = new ArrayList<String>();
			listResource.add(strRootOid);
			SecurityUtil.checkRight(username, operationsList, listResource,
					new BackHandler<AosBooleanArrayWrapper>() {
						@Override
						public void handle(AosBooleanArrayWrapper aResult,
								Throwable aException) {
							if (aException != null) {
								dMsg.error("check right failed", aException);
								handler.handle(null, aException);
							} else {
								Map<String, Boolean> rights = new HashMap<String, Boolean>();
								Iterator<Boolean> resultIter = aResult
										.getValueList().iterator();
								for (String op : operationsList) {
									rights.put(op, resultIter.next());
								}
								handler.handle(convertViews(locale, rights,username, passEncode),
										null);
							}
						}
					});
		}
	}

	private ModuleViewsResponseMsg convertViews(final Locale locale,
			Map<String, Boolean> rights,String username,String passEncode) {
		List<ModuleView> views = new ArrayList<ModuleView>();
		for (ModuleViewEntity viewEntity : viewEntityList) {
			views.add(viewEntity.toModuleView(rights, i18nFileMap, locale, username, passEncode));
		}
		ModuleViewsResponseMsg moduleViewsResponseMsg = new ModuleViewsResponseMsg();
		moduleViewsResponseMsg.setViewsList(views);
		return moduleViewsResponseMsg;
	}
}
