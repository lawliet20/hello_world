package com.mk.pro.manage.controller;

import java.util.List;

import javax.annotation.Resource;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mk.pro.model.AreaCodeInfo;
import com.mk.pro.service.AreaCodeInfoService;

/**
 * 地区码控制器
 */
@Controller
@RequestMapping("/areaCode")
public class AreaCodeController {
	@Resource(name = "areaCodeInfoService")
	private AreaCodeInfoService areaCodeInfoService;

	/**
	 * 按条件查询地区码列表
	 */
	@RequiresPermissions("sys:areacode:view")
	@RequestMapping("/list")
	public String list(AreaCodeInfo areaCodeInfo, Model model) {
		List<AreaCodeInfo> areaCodeList = areaCodeInfoService.findByPage(areaCodeInfo);
		model.addAttribute("areaCodeList", areaCodeList);
		model.addAttribute("areaCodeInfo", areaCodeInfo);
		return "jsp/sysmanage/areacode/list";
	}
	
	/**
	 * 按条件查询地区码列表
	 */
	@RequiresPermissions("sys:areacode:view")
	@RequestMapping("/detail")
	public String detail(Integer areaId, Model model) {
		AreaCodeInfo areaCodeInfo = null;
		try {
			areaCodeInfo = areaCodeInfoService.findOne(areaId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("areaCodeDetail", areaCodeInfo);
		return "jsp/sysmanage/areacode/detail";
	}
}
