package com.mk.pro.manage.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.BankInfo;
import com.mk.pro.service.BankInfoService;
/**
 * 银行信息控制类
 */
@Controller
@RequestMapping(value = "/bankInfo")
public class BankInfoController extends BaseController {

	@Resource
	private BankInfoService bankInfoService;
	
	/**
	 * 查询银行列表
	 */
	@RequestMapping(value = "/popList")
	public String queryBankInfo(BankInfo bankInfo, String textId, Model model) throws Exception {
		List<BankInfo> dataList = bankInfoService.findByPage(bankInfo);
		model.addAttribute("dataList", dataList);
		model.addAttribute("bankInfo", bankInfo);
		model.addAttribute("textId",textId);
		return "jsp/bank/popList";
	}

}