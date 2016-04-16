package com.mk.pro.manage.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mk.pro.commons.ResultResp;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.exception.BaseException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.AreaCodeInfo;
import com.mk.pro.model.MerchantScanpaySettleInfo;
import com.mk.pro.model.OrganizationInfo;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.AreaCodeInfoService;
import com.mk.pro.service.OrganizationInfoService;
@Controller
@RequestMapping(value = "/organization")
public class OrganizationController extends BaseController{
	@Resource
	 private OrganizationInfoService organizationInfoService;
	@Resource
	private AreaCodeInfoService areaCodeInfoService;
	/**
	 * 机构查询列表
	 * @param organizationInfo
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping("/openOrganizationList")
	public String openOrganizationList(OrganizationInfo organizationInfo,HttpServletRequest request,Model model){
		SysUsers users=this.getCurrentUser(request);
			if(users.getNotype()==3){
				model.addAttribute("orgNotype", users.getNotype());
				organizationInfo.setOrgCode(users.getUserno());
		}
		// 查询地区的编号
		String areaCode = "";
		//第三级
		if (organizationInfo.getProvId() != null && organizationInfo.getProvId() != 0) {
			areaCode = getAreaCodeById(organizationInfo.getProvId(),3);
		//第二级
		} else if (organizationInfo.getProvId2() != null && organizationInfo.getProvId2() != 0) {
			areaCode = getAreaCodeById(organizationInfo.getProvId2(),2);
		//第一级
		} else if (organizationInfo.getProvId1() != null && organizationInfo.getProvId1() != 0) {
			areaCode = getAreaCodeById(organizationInfo.getProvId1(),1);
		}
		organizationInfo.setAreaCode(areaCode);
		List<OrganizationInfo> oList=organizationInfoService.findByPageOrganizationList(organizationInfo);
		model.addAttribute("oList", oList);
		model.addAttribute("oPage", organizationInfo);
		return "jsp/organization/organizationInfoList";
	}
	//获取地区吗码
	private String getAreaCodeById(Integer id,Integer type) {
		AreaCodeInfo info = areaCodeInfoService.findOne(id);
		if (info != null) {
			String code =String.valueOf(info.getAreaId());
			if (code != null && code.length() > 0) {
				code = stringSplitEnd(code,type);
				return code;
			}
		}
		return "";
	}
	//截取字符串 前3位
	private String stringSplitEnd(String str,Integer type) {
		if (str != null) {
			if(type==2){
				str=str.substring(0, 4);
			}else{
				str=str.substring(0, 3);
			}
		}
		return str;
	}
	//打开机构增加页面
	@RequestMapping("/openOrganizationSave")
	public String openOrganizationSave(){
		return "jsp/organization/organizationSave";
	}
	/**
	 * 机构增加
	 * @param record 
	 * @param model
	 * @param response
	 */
	@RequiresPermissions("organization:add:create")
	@RequestMapping(value = "/saveOrgInfo.json")
	@ResponseBody
	public void saveOrgInfo(OrganizationInfo record, Model model,HttpServletResponse response,HttpServletRequest request) {
		ResultResp resp=null;
		try{
			int res=organizationInfoService.insertSelective(record, request);
			if(res==1)
				resp = ResultResp.getInstance(true,String.valueOf(Integer.valueOf(record.getOrgCode())+1));
			 else 
			resp = ResultResp.getInstance(false, "机构增加失败，请联系管理员");
		}catch(BaseException e){
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			resp = ResultResp.getInstance(false,e.getMessage());
		}
		this.writeJson(resp, response);
	}
	/**
	 * 打开机构详情页面
	 * @param orgCode
	 * @return
	 */
	@RequiresPermissions("organization:info:view")
	@RequestMapping(value = "/organizationView")
	public String organizationView(String orgCode,Model model,HttpServletResponse response,HttpServletRequest request){
		ResultResp resp=null;
		try{
			OrganizationInfo o=organizationInfoService.selectByPrimaryKey(orgCode);
			model.addAttribute("oOne", o);
			if(resp!=null)
				 resp = ResultResp.getInstance(true,orgCode);
			 else 
				 resp = ResultResp.getInstance(false, "查询失败！");
		}catch(BaseException e){
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			resp = ResultResp.getInstance(false,e.getMessage());
		}
		return "jsp/organization/organizationView";
	}
}
