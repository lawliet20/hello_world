package com.mk.pro.manage.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.SmsOrderInfo;
import com.mk.pro.service.SmsManageService;
import com.mk.pro.utils.MyStringUtil;

/**
 * 短信管理
 * 2015年4月29日15:56:07
 * @author wwj
 */
@Controller
@RequestMapping("/smsManage")
public class SmsManageController extends BaseController<SmsOrderInfo, String> {
	@Resource(name="smsManageService")
	private SmsManageService smsManageService; 
	
	/**
	 * 查询短信所有申请
	 * @param smsOrder
	 * @param model
	 * @return
	 */
	@RequiresPermissions("market:smsbuym:view")
	@RequestMapping("/querySmsOrderList")
	public String querySmsOrder(SmsOrderInfo smsOrder,Model model){
		List<SmsOrderInfo> smsOrderList = smsManageService.queyrSmsOrderList(smsOrder);
		model.addAttribute("smsOrderList", smsOrderList);
		model.addAttribute("smsOrderInfo", smsOrder);
		return "/jsp/market/smsManage/smsOrderList";
	}
	
	/**
	 * 跳转到审核页面
	 * @return
	 */
	@RequiresPermissions("market:smsbuym:audit")
	@RequestMapping("/toAudithJsp")
	public String toAudithJsp(String smsorderid,Model model){
		SmsOrderInfo smsOrderInfo = smsManageService.querySmsOrderById(MyStringUtil.str2Integer(smsorderid));
		model.addAttribute("smsOrder", smsOrderInfo);
		return "/jsp/market/smsManage/smsAudith";
	}
	
	/**
	 * 审核通过
	 * @param smsOrder
	 * @throws Exception 
	 */
	@RequiresPermissions("market:smsbuym:audit")
	@RequestMapping("/audithSmsAppl")
	public void audithSmsAppl(SmsOrderInfo smsOrder,HttpServletResponse response) {
		try {
			int res = smsManageService.updateAudithSmsAppl(smsOrder);
			this.writeJson(res, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
