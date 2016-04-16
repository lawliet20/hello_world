package com.mk.pro.manage.controller;

import java.math.BigDecimal;
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

import com.mk.pro.commons.ResultResp;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.exception.BaseException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.MerchantJoinedIn;
import com.mk.pro.model.OnlineMessage;
import com.mk.pro.model.SysActivityInfo;
import com.mk.pro.model.SysActivityTransLog;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.service.MerchantJoinedInService;
import com.mk.pro.service.OnlineMessageService;
import com.mk.pro.service.SysActivityInfoService;
import com.mk.pro.service.SysActivityTransLogService;
import com.mk.pro.utils.DateUtils;
/**
 * 
 * @author:ChengKang
 * @date:2015-9-12 下午1:54:34
 * 
 **/
@Controller
@RequestMapping(value = "/activity")
public class sysActivity extends BaseController{
	@Resource
	SysActivityInfoService sysActivityInfoService;
	@Resource
	SysActivityTransLogService sysActivityTransLogService;
	@Resource
	OnlineMessageService onlineMessageService;
	@Resource
	MerchantJoinedInService merchantJoinedInService;
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	/**
	 * 平台活动设置List页面
	 * @param sysActivityInfo
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/querysysActivityList")
	public String querysysActivityList(SysActivityInfo sysActivityInfo, Model model,HttpServletRequest request) throws Exception {
		List<SysActivityInfo> sysActivityList=null;
		sysActivityList=sysActivityInfoService.findByPageSysActivityInfoList(sysActivityInfo);
		model.addAttribute("sysActivityList", sysActivityList);
		model.addAttribute("sysActivityInfoPage", sysActivityInfo);
		return "jsp/sysActivity/sysActivityInfoList";
	}
	/**
	 * 打开平台活动信息
	 * @param id
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/querysysActivityView")
	public String querysysActivityView(Integer id,Model model,HttpServletRequest request){
		if(id!=null){
			SysActivityInfo sysActivityInfo=sysActivityInfoService.selectByPrimaryKey(id);
			model.addAttribute("sysActivityInfo", sysActivityInfo);
		}
		return "jsp/sysActivity/sysActivityInfoDetail";
	}
	/**
	 * 打开平台活动信息修改
	 * @param id
	 * @param model
	 * @param request
	 * @return
	 */
	@RequiresPermissions("terrace:activity:update")
	@RequestMapping(value="/updateSysActivity")
	public String updateSysActivity(Integer id,Model model,HttpServletRequest request){
		if(id!=null){
			SysActivityInfo sysActivityInfo=sysActivityInfoService.selectByPrimaryKey(id);
			//活动起始时间和活动结束时间
			String startDate=DateUtils.getFormatDate(sysActivityInfo.getStartDate());
			String endDate=DateUtils.getFormatDate(sysActivityInfo.getEndDate());
			model.addAttribute("startDate", startDate);
			model.addAttribute("endDate", endDate);
			model.addAttribute("sysActivityInfo", sysActivityInfo);
		}
		return "jsp/sysActivity/sysActivityInfoUpdate";
	}
	/**
	 * 修改平台活动
	 * @param sysActivityInfo
	 * @param model
	 * @param response
	 */
	@RequestMapping(value="/sysActivityUpdate")
	public void sysActivityUpdate(SysActivityInfo sysActivityInfo,Model model,HttpServletResponse response){
		Json json=new Json();
		//开始时间和结束时间转换
		String EndDate=DateUtils.getFormatDateString(sysActivityInfo.getEndDate());
		String StatDate=DateUtils.getFormatDateString(sysActivityInfo.getStartDate());
		sysActivityInfo.setStartDate(StatDate);
		sysActivityInfo.setEndDate(EndDate);
		int res=sysActivityInfoService.updateByPrimaryKeySelective(sysActivityInfo);
		if(res==1){
			json.setMsg("修改成功！");
			json.setResult(true);
		}else{
			json.setMsg("修改失败！");
			json.setResult(false);	
		}
		this.writeJson(json, response);
	}
	/**
	 * 平台活动流水信息
	 * @param sysActivityTransLog
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/querySysActivityTrans")
	public String querySysActivityTrans(SysActivityTransLog sysActivityTransLog, Model model,HttpServletRequest request) throws Exception {
		List<SysActivityTransLog> sysActivityTransList=null;
		sysActivityTransList=sysActivityTransLogService.findByPageSysActivityTransLogList(sysActivityTransLog);
		model.addAttribute("sysActivityTransList", sysActivityTransList);
		model.addAttribute("sysActivityTransLogPage", sysActivityTransLog);
		//查询注册、推荐、分享优惠劵的笔数和总金额
		sysActivityTransLog.setFavourType(new Short("0"));
		List<SysActivityTransLog> s0=sysActivityTransLogService.findByPageSysActivityTransLogList(sysActivityTransLog);
		if(s0.size()==0 || s0==null){
			model.addAttribute("s0Size", 0);
			model.addAttribute("Sun0", 0);
		}
		if(s0.size()>0){
			model.addAttribute("s0Size", s0.size());
			//type是0的总金额
			int Sun0=sysActivityTransLogService.getSunGiftAmt(0);
			model.addAttribute("Sun0", Sun0);
		}
		sysActivityTransLog.setFavourType(new Short("1"));
		List<SysActivityTransLog> s1=sysActivityTransLogService.findByPageSysActivityTransLogList(sysActivityTransLog);
		if(s1.size()==0 || s1==null){
			model.addAttribute("Sun1", 0);
			model.addAttribute("s1Size", 0);
		}
		if(s1.size()>0){
			model.addAttribute("s1Size", s1.size());
			//type是1的总金额
			int Sun1=sysActivityTransLogService.getSunGiftAmt(1);
			model.addAttribute("Sun1", Sun1);
		}
		sysActivityTransLog.setFavourType(new Short("2"));
		List<SysActivityTransLog> s2=sysActivityTransLogService.findByPageSysActivityTransLogList(sysActivityTransLog);
		if(s2.size()==0 || s2==null){
			model.addAttribute("Sun2", 0);
			model.addAttribute("s2Size", 0);
		}
		if(s2.size()>0){
			model.addAttribute("s2Size", s2.size());
			//type是2的总金额
			int Sun2=sysActivityTransLogService.getSunGiftAmt(2);
			model.addAttribute("Sun2", Sun2);
		}
		return "jsp/sysActivity/sysActivityTransLogList";
	}
	/**
	 * 在线留言List页面
	 * @param o
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/queryOnlineMessageList")
	public String queryOnlineMessageList(OnlineMessage o, Model model,HttpServletRequest request) throws Exception {
		List<OnlineMessage> oList=null;
		oList=onlineMessageService.findByPageOnlineList(o);
		model.addAttribute("oList", oList);
		model.addAttribute("oPage", o);
		return "jsp/sysmanage/onlineMessage/list";
	}
	/**
	 * 留言查看详情
	 * @param id
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/queryOnlineMessageView")
	public String queryOnlineMessageView(Integer id,Model model,HttpServletRequest request){
		if(id!=null){
			OnlineMessage o=onlineMessageService.selectByPrimaryKey(id);
			model.addAttribute("oOne", o);
		}
		return "jsp/sysmanage/onlineMessage/detail";
	}
	/**
	 * 加盟入住List信息
	 * @param m
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/queryMerchantJoinedInList")
	public String queryMerchantJoinedInList(MerchantJoinedIn m, Model model,HttpServletRequest request) throws Exception {
		List<MerchantJoinedIn> mList=null;
		mList=merchantJoinedInService.findByPageJoinList(m);
		model.addAttribute("mList", mList);
		model.addAttribute("mPage", m);
		return "jsp/sysmanage/joinedIn/list";
	}
	/**
	 * 入住加盟详情
	 * @param id
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/queryMerchantJoinedInView")
	public String queryMerchantJoinedInView(Integer id,Model model,HttpServletRequest request){
		if(id!=null){
			MerchantJoinedIn m=merchantJoinedInService.selectByPrimaryKey(id);
			model.addAttribute("mOne", m);
		}
		return "jsp/sysmanage/joinedIn/detail";
	}
	/**
	 * 跟新在线留言状态
	 * @param onlineMessage
	 * @param model
	 * @param response
	 */
    @RequestMapping(value = "/updateSolveType")
    public void updateSolveType(Integer id,Integer solveType,Model model, HttpServletResponse response,HttpServletRequest request){
    	ResultResp resp=null;
		try{
			OnlineMessage o=onlineMessageService.selectByPrimaryKey(id);
			if(solveType==0){
				o.setSolveType(1);
			}else{
				o.setSolveType(0);
			}
			int res = onlineMessageService.updateByPrimaryKeySelective(o);
			if(res==1){
				resp = ResultResp.getInstance(ResultCode.success);
				resp.setMsg("更新状态成功！");
				resp.setStatus(true);
			}
			 else{
				resp = ResultResp.getInstance(ResultCode.unKnowErr);
				resp.setMsg("更新状态失败！");
				resp.setStatus(false); 
			 }
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("addCardBin error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
    }
    @RequestMapping(value = "/deleteOnlineMessage")
    public void deleteOnlineMessage(Integer id,Model model, HttpServletResponse response,HttpServletRequest request){
    	ResultResp resp=null;
		try{
			int res = onlineMessageService.deleteByPrimaryKey(id);
			if(res==1){
				resp = ResultResp.getInstance(ResultCode.success);
				resp.setMsg("删除成功！");
				resp.setStatus(true);
			}
			 else{
				resp = ResultResp.getInstance(ResultCode.unKnowErr);
				resp.setMsg("删除失败，请联系管理员！");
				resp.setStatus(false); 
			 }
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("addCardBin error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
    }
}
