package com.mk.pro.manage.controller;

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

import com.mk.pro.commons.ResultResp;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.enums.UserType;
import com.mk.pro.commons.exception.ServiceException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.manage.constants.Constants;
import com.mk.pro.model.MemberInfo;
import com.mk.pro.model.MemberInfoResponse;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.MerchantAccountService;
import com.mk.pro.service.TransLogInfoService;

@Controller
@RequestMapping("/memberMarket")
public class MemberMarketController extends BaseController<MemberInfo, String> {
	/**
	 * Logger for this class
	 */
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Resource
	private TransLogInfoService transLogInfoService;
	@Resource
	private MerchantAccountService merchantAccountService;
	/**
	 * 查看营销会员
	 */
	@RequiresPermissions("market:memberMarket:view")
	@RequestMapping("/list")
	public String list(MemberInfo member,Model model,HttpServletRequest request){
		//商户查询自己的会员 (次之前仅开发到商户)
		SysUsers user = (SysUsers) request.getSession().getAttribute(Constants.CURRENT_USER);
		if(member.getRegMerId()==null || "".equals(member.getRegMerId()))
			if(user.getNotype().intValue()==UserType.merchant.getValue())
				member.setRegMerId(user.getUserno());
			else{
				//throw new ServiceException(ResultCode.userNotMer.getIdf());
				//管理员或者运营商直接跳转到页面上进行商户号查询 不进行页面其它删选查询
				model.addAttribute("memberType", 1);
				return "/jsp/market/memberMarket/memberMarketList";
			}
		List<MemberInfoResponse> memberMarketList = transLogInfoService.queryMemberList(member);
		model.addAttribute("memberMarketList",memberMarketList);
		model.addAttribute("member",member);
		return "/jsp/market/memberMarket/memberMarketList";
	}
	/**
	 * 发送短信页面
	 * @return
	 */
	@RequiresPermissions("market:memberMarket:sendMsg")
	@RequestMapping("/sendMemberMsg")
	public String sendMemberMsg(MemberInfo member,Model model,HttpServletRequest request){
		//查询当前所需要发送的手机号码对象
		SysUsers user = (SysUsers) request.getSession().getAttribute(Constants.CURRENT_USER);
		if(user.getNotype().intValue()==UserType.merchant.getValue())
			member.setRegMerId(user.getUserno());
		else
			throw new ServiceException(ResultCode.userNotMer.getIdf());
		member.getPage().setFenye(false);
		List<MemberInfoResponse> memberMarketList = transLogInfoService.queryMemberList(member);
		String mobileNoList="";
		Integer mobileNoCount=0;
		for(MemberInfoResponse obj:memberMarketList){
			if(obj.getMobileNo()!=null&&!"".equals(obj.getMobileNo())){
				mobileNoCount++;
				mobileNoList=mobileNoList+obj.getMobileNo()+",";
			}
		}
		model.addAttribute("mobileNoList",mobileNoList);
		model.addAttribute("mobileNoCount", mobileNoCount);
		return "jsp/market/memberMarket/memberMarketSendMsg";
		
	}
	/**
	 * 发送短信
	 */
	@RequiresPermissions("market:memberMarket:sendMsg")
	@RequestMapping("/doSendMsg")
	public void doSendMsg(Model model,HttpServletRequest request,HttpServletResponse response){
		ResultResp resp = null;
		int res=merchantAccountService.doSendMsg(request);
		if(res>0){
			resp = ResultResp.getInstance(ResultCode.success);
		}else{
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}
		
		this.writeJson(resp, response);
	}
}
