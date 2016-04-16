package com.mk.pro.manage.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.BankCodeInfo;
import com.mk.pro.model.BankInfo;
import com.mk.pro.service.BankCodeInfoService;
import com.mk.pro.service.BankInfoService;

@Controller
@RequestMapping(value = "/bank")
public class BankCodeInfoController extends BaseController<Object, String>{

	@Resource
	private BankCodeInfoService bankCodeInfoService;
	@Resource
	private BankInfoService bankInfoService;
	/**
	 * 查询银行列表
	 */
	@RequestMapping(value = "/popBankList")
	public String queryBank(String bankCode,BankCodeInfo bankCodeInfo, String fatherName,String textId,String valId, Model model) throws Exception {
		if(bankCodeInfo.getBankNames()!=null&&!"".equals(bankCodeInfo.getBankNames())){
			BankInfo bank=bankInfoService.selectOne(Integer.valueOf(bankCodeInfo.getBankNames()));
			bankCodeInfo.setBankNames(bank.getBankName());
		}

		List<BankCodeInfo> dataList = bankCodeInfoService.findByPage(bankCodeInfo);
		model.addAttribute("dataList", dataList);
		model.addAttribute("bankCodeInfo", bankCodeInfo);
		model.addAttribute("textId",textId);
		model.addAttribute("valId",valId);
		return "jsp/public/mkBankView";
	}
	@RequestMapping(value="bankInfoList")
	public void bankList(BankInfo bankInfo,HttpServletResponse response){
		//银行总部查询列表
		List<BankInfo> bankList=bankInfoService.BankInfoList(bankInfo);
		this.writeJson(bankList, response);
	}
	
	@RequestMapping(value = "/BankInfoList")
	public String queryBankZong( String bankName,BankCodeInfo bankCodeInfo,Model model) throws Exception {
		if(bankName!=null){
		bankCodeInfo.setBankName(bankName.replaceAll(",", ""));
		}
		List<BankCodeInfo> dataList = bankCodeInfoService.findByPage(bankCodeInfo);
		model.addAttribute("dataList", dataList);
		model.addAttribute("bankCodeInfo", bankCodeInfo);
		return "jsp/public/mkBankView";
	}
	
	/**
	 * 查询银行列表
	 */
	@RequiresPermissions("sys:bankcode:view")
	@RequestMapping(value = "/list")
	public String list(BankCodeInfo bankCodeInfo, Model model) throws Exception {
		List<BankCodeInfo> dataList = bankCodeInfoService.findByPage(bankCodeInfo);
		model.addAttribute("dataList", dataList);
		model.addAttribute("bankCodeInfo", bankCodeInfo);
		return "jsp/sysmanage/bankcode/list";
	}

}