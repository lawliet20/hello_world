package com.mk.pro.manage.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.SmsStockInfo;
import com.mk.pro.service.SmsStockService;

/**
 * 短信库存管理
 * 2015年4月29日10:39:01
 * @author wwj
 */
@Controller
@RequestMapping("/smsStock")
public class SmsStockController extends BaseController<SmsStockInfo, String> {
	@Resource(name="smsStockService")
	private SmsStockService smsStockService;

	/**
	 * 查询短信库存
	 * @param sms
	 * @param mode
	 * @return
	 */
	@RequiresPermissions("market:smsrep:view")
	@RequestMapping("/querySmsStock")
	public String querySmsStock(SmsStockInfo sms,Model model){
		List<SmsStockInfo>smsStockList = smsStockService.querySmsStock(sms);
		model.addAttribute("smsStockInfo", sms);
		model.addAttribute("smsStockList", smsStockList);
		return "/jsp/market/smsStockManage/smsStockList";
	}
	
	/**
	 * 页面跳转到新增页面
	 */
	@RequiresPermissions("market:smsrep:add")
	@RequestMapping("/toAddJsp")
	public String toAddJsp(){
		return "/jsp/market/smsStockManage/addSmsStock";
	}
	
	
	/**
	 * 新增
	 * @param sms
	 */
	@RequiresPermissions("market:smsrep:add")
	@RequestMapping("/addSmsStock")
	public void addSmsStock(SmsStockInfo sms,HttpServletResponse response){
		int res = smsStockService.addSmsStock(sms);
		writeJson(res, response);
	}
}
