package com.mk.pro.manage.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.CardBinInfo;
import com.mk.pro.service.CardBinService;

/**
 * 卡BIN管理控制器
 */
@Controller
@RequestMapping("/cardBin")
public class CardBinInfoController extends BaseController{
	@Resource(name = "cardBinService")
	private CardBinService cardBinService;

	/**
	 * 按条件查询卡BIN列表
	 */
	@RequiresPermissions("sys:bin:view")
	@RequestMapping("/list")
	public String list(CardBinInfo cardBinInfo, Model model) {
		List<CardBinInfo> mccList = cardBinService.findByPageCardBin(cardBinInfo);
		model.addAttribute("cardBinList", mccList);
		model.addAttribute("cardBinInfo", cardBinInfo);
		return "jsp/sysmanage/cardbin/list";
	}
	
	/**
	 * 按条件查询卡BIN列表
	 */
	@RequiresPermissions("sys:bin:view")
	@RequestMapping("/detail")
	public String detail(String binId, Model model) {
		CardBinInfo cardBinInfo = cardBinService.getCardBinInfoDetail(binId);
		model.addAttribute("cardBinDetail", cardBinInfo);
		return "jsp/sysmanage/cardbin/detail";
	}
	
	/**
	 * 跳转新增页面
	 */
	@RequiresPermissions("sys:bin:create")
	@RequestMapping("/toAddJsp")
	public String toAddJsp(String binId,String type, Model model,HttpServletRequest request){
		return "jsp/sysmanage/cardbin/add";
	}
	
	/**
	 * 新增
	 * 
	 * @param cardBin
	 * @param model
	 * @return
	 */
	@RequiresPermissions("sys:bin:create")
	@RequestMapping("/add")
	public void add(CardBinInfo cardBinInfo, Model model, HttpServletResponse response) {
		int res = cardBinService.insertCardBin(cardBinInfo);
		this.writeJson(res, response);
	}
	
}
