package com.mk.pro.manage.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mk.pro.commons.ResultResp;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.exception.BaseException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.manage.constants.Constants;
import com.mk.pro.model.AgentSplitRate;
import com.mk.pro.model.MerUnionActInfo;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.AgentSplitRateService;

/**
 * 
 * @author:syd
 * @date:2015-6-1 下午15:00:00
 * 
 **/
@Controller
@RequestMapping(value = "/agentSplitRate")
public class AgentSplitRateController extends BaseController {
	@Resource
	private AgentSplitRateService agentSplitRateService;
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	/**
	 * 查询分润比例列表
	 */
	@RequiresPermissions("agent:rate:view")
	@RequestMapping(value = "/list")
	public String queryBank(AgentSplitRate agentSplitRate, Model model,HttpServletRequest request) throws Exception {
		List<AgentSplitRate> list=null;
    	ResultResp resp = null;
		try {
			list = agentSplitRateService.findByPage(agentSplitRate,request);
			resp = ResultResp.getInstance(ResultCode.success);
		}catch (BaseException e) {
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch (Exception e) {
			log.error("merUnion actReqList error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		model.addAttribute("list", list);
		model.addAttribute("agentSplitRate", agentSplitRate);
		model.addAttribute("resp", resp);
		return "jsp/agentSplitRate/list";
	}

	
//	// 批量删除选中的行
//	@RequiresPermissions("agent:rate:deleste")
//	@RequestMapping(value = "/delete")
//	public void deleteAgent(@RequestParam String[] agentCodes,HttpServletResponse response) throws IOException{
//		Json json = new Json();
//		if (agentCodes != null && agentCodes.length > 0) {
//			// 循环删除
//			for (int i = 0; i < agentCodes.length; i++) {
//				agentSplitRateService.deleteAgentCode(agentCodes[i]);
//			}
//			json.setResult(true);
//			json.setMsg("删除成功");
//		}else{
//			json.setMsg("删除失败");
//		}
//		this.writeJson(json, response);
//	
//	}

	/**
	 * 修改页面
	 * @param agentCode
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequiresPermissions("agent:rate:update")
	@RequestMapping(value = "/updateRate")
	public String updateRate(String rateId,Model model) throws Exception {
    	ResultResp resp = null;
		try {
			AgentSplitRate age = agentSplitRateService.toSelectAgent(rateId);
			model.addAttribute("agentSplitRate", age);
			resp = ResultResp.getInstance(ResultCode.success);
		}catch (BaseException e) {
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch (Exception e) {
			log.error("merUnion actReqList error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		model.addAttribute("resp", resp);
		return "jsp/agentSplitRate/updateRate";
	}
	
	@RequiresPermissions("agent:rate:update")
	@RequestMapping(value = "/toUpdate")
	public void toUpdate(AgentSplitRate agentSplitRate,HttpServletRequest request,HttpServletResponse response) throws Exception {
			int resp = agentSplitRateService.updateSplitRate(agentSplitRate, request);
			this.writeJson(resp, response);
	}
	
	
	@RequestMapping(value = "/queryDetail")
	public String queryDetail(String rateId,Model model){
		ResultResp resp = null;
		try {
			AgentSplitRate age = agentSplitRateService.toSelectAgent(rateId);
			model.addAttribute("agentSplitRate", age);
			resp = ResultResp.getInstance(ResultCode.success);
		}catch (BaseException e) {
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch (Exception e) {
			log.error("merUnion actReqList error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		model.addAttribute("resp", resp);
		return "jsp/agentSplitRate/queryDetail";
	}
}