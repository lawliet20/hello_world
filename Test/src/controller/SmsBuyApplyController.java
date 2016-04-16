package com.mk.pro.manage.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.manage.constants.Constants;
import com.mk.pro.model.SmsOrderInfo;
import com.mk.pro.model.SmsStockInfo;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.SmsBuyApplyService;
import com.mk.pro.utils.MyStringUtil;

/**
 * 短信购买申请
 * 2015年4月29日10:38:01
 * @author wwj
 */
@Controller
@RequestMapping("/smsBuyApply")
public class SmsBuyApplyController extends BaseController<SmsOrderInfo, String> {
	
	@Resource
	private SmsBuyApplyService smsBuyApplyService;

	/**
	 * 跳转到短信购买申请页面
	 */
	@RequiresPermissions("market:smsbuyapply:add")
	@RequestMapping("/toApplyJsp")
	public String toApplyJsp(){
		return "/jsp/market/smsBuy/smsBuyApply";
	}
	
	/**
	 * 新增
	 * @param sms
	 */
	@RequiresPermissions("market:smsbuyapply:add")
	@RequestMapping("/addSmsOrder")
	public void addSmsStock(SmsOrderInfo sms,HttpServletRequest request,HttpServletResponse response){
		SysUsers user = (SysUsers) request.getSession().getAttribute(Constants.CURRENT_USER);
		sms.setAgentcode(this.getCurrentAgent(request).getAgentCode());
		//短信单价
		Double salePrice = MyStringUtil.str2Double(smsBuyApplyService.getSalePrice(new SmsStockInfo(sms.getSmsoperator())));
		//购买条数
		Integer smsCount = sms.getSmscount();
		//设置总价
		sms.setTransamt(MyStringUtil.double2BigDecimal(salePrice*smsCount));
		int res = smsBuyApplyService.addSmsOrder(sms);
		writeJson(res, response);
	}
	
	/**
	 * 得到当前运营商短信单价
	 * @param sms
	 */
	@RequiresPermissions("market:smsbuyapply:add")
	@RequestMapping("/getSalePrice")
	public void getSalePrice(SmsStockInfo sms,HttpServletResponse response){
		String res = smsBuyApplyService.getSalePrice(sms);
		this.writeJson(MyStringUtil.str2Double(res), response);
	}
}
