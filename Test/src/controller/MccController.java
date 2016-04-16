package com.mk.pro.manage.controller;

import java.util.List;

import javax.annotation.Resource;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mk.pro.model.MccCodeInfo;
import com.mk.pro.service.MccService;

/**
 * 商户的类别代码控制器
 */
@Controller
@RequestMapping("/mcc")
public class MccController {
	@Resource(name = "mccService")
	private MccService mccService;

	/**
	 * 按条件查询商户类别码列表
	 */
	@RequiresPermissions("sys:mcc:view")
	@RequestMapping("/list")
	public String list(MccCodeInfo mccCodeInfo, Model model) {
		List<MccCodeInfo> mccList = mccService.findByPageMccCode(mccCodeInfo);
		model.addAttribute("mccList", mccList);
		model.addAttribute("mcc", mccCodeInfo);
		return "jsp/sysmanage/mcc/list";
	}
	
	/**
	 * 按条件查询商户类别码列表
	 */
	@RequiresPermissions("sys:mcc:view")
	@RequestMapping("/detail")
	public String detail(String mccId, Model model) {
		MccCodeInfo mccCodeInfo = mccService.getMccCodeInfoDetail(mccId);
		model.addAttribute("mccDetail", mccCodeInfo);
		return "jsp/sysmanage/mcc/detail";
	}
}
