package com.mk.pro.manage.controller;


import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.mk.pro.commons.ResultResp;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.enums.UserType;
import com.mk.pro.commons.exception.BaseException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.manage.constants.Constants;
import com.mk.pro.model.AgentInfo;
import com.mk.pro.model.AreaDistrictInfo;
import com.mk.pro.model.CardApplyInfo;
import com.mk.pro.model.CardBatchInfo;
import com.mk.pro.model.CardInfo;
import com.mk.pro.model.CardTotalInfo;
import com.mk.pro.model.MemberCardBin;
import com.mk.pro.model.MerchantInfo;
import com.mk.pro.model.Page;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.AgentInfoService;
import com.mk.pro.service.CardApplyInfoService;
import com.mk.pro.service.CardBatchInfoService;
import com.mk.pro.service.CardInfoService;
import com.mk.pro.service.CardTotalService;
import com.mk.pro.service.MemberCardBinService;
import com.mk.pro.service.MerchantService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.ExportUtil;
import com.mk.pro.utils.MyStringUtil;
import com.mk.pro.utils.img.ImageUtil;

/**
 * 
 * @author:ChengKang
 * @date:2015-5-10 下午21:45:00
 * 
 **/
@Controller
@RequestMapping(value = "card")
public class CardBinController extends BaseController {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Resource
	private MemberCardBinService memberCardBinService;
	@Resource
	private CardBatchInfoService cardBatchInfoService;
	@Resource
	private  CardInfoService cardInfoService;
	@Resource
	private CardTotalService cardTotalService;
	@Resource
	private CardApplyInfoService cardApplyInfoService;
	@Resource
	private AgentInfoService agentInfoService;
	@Resource
	private MerchantService merchantService;
	/**
	 * 卡Bin 查询页面
	 * @param memberCardBin
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequiresPermissions("mcard:bin:view")
	@RequestMapping(value = "/cardBinList")
	public String findPageCardBin(MemberCardBin memberCardBin, Model model) throws Exception {
		// 查询列表
		ResultResp resp = null;
		List<MemberCardBin> cardBinList=null;
		try{
			cardBinList= memberCardBinService.findByPageCardBin(memberCardBin);
			resp = ResultResp.getInstance(ResultCode.success);
		}catch (BaseException e) {
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch (Exception e) {
			log.error("cardBinList error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		model.addAttribute("cardBinList", cardBinList);
		model.addAttribute("cardPage", memberCardBin);
		model.addAttribute("result", resp);
		return "jsp/card/cardBinList";
	}
	/**
	 * 制卡审核List页面
	 **/
	@RequiresPermissions("mcard:openaudit:view")
	@RequestMapping(value="cardBatchList")
	public String cardBatchList(CardBatchInfo cardBatchInfo, Model model,HttpServletRequest request){
		ResultResp resp=null;
		List<CardBatchInfo> cardBatchLit=null;
		SysUsers users=this.getCurrentUser(request);
		try{
			if(users.getNotype().intValue()==1){
				cardBatchInfo.setAgentCode(users.getUserno());
			}
			cardBatchLit=cardBatchInfoService.findByPageCardBatchList(cardBatchInfo);
			resp = ResultResp.getInstance(ResultCode.success);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch (Exception e) {
			log.error("cardBatchList error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		model.addAttribute("cardBatchLit", cardBatchLit);
		model.addAttribute("batchpage", cardBatchInfo);
		model.addAttribute("result",resp);
		return "jsp/card/cardBatchList";
	}
//	/**
//	 * 查询所有商户id
//	 * @param agentInfo
//	 * @param response
//	 * @throws Exception
//	 */
//	@RequestMapping(value="/selectAgentCode")
//	public String selectAgentListId(Model model,AgentInfo agentInfo,HttpServletResponse response) throws Exception{
//		List<AgentInfo> agentLists=agentInfoService.findByPage(agentInfo);
//		model.addAttribute("agentLists", agentLists);
//		List<ComboObj> comboList = new ArrayList();
//		for(AgentInfo af:agentLists){
//			ComboObj co = new ComboObj();
//			co.setLabel(af.getAgentName());
//			co.setValue(af.getAgentCode());
//			comboList.add(co);
//		}
//		String a = JSON.toJSONString(comboList);
//		this.writeJson(JSON.toJSONString(comboList), response);
//		return "jsp/card/cardBatchList";
//}

	/**
	 * 打开运营商添加、修改页面
	 * @throws Exception 
	 */
	@RequiresPermissions(value={"mcard:bin:update","mcard:bin:view"},logical=Logical.OR)
	@RequestMapping(value = "/openCardBin")
	public String getAddCard(MerchantInfo merchantInfo,AgentInfo agentInfo,Integer binId, Model model,HttpServletRequest request) throws Exception {
		//运营商名称下拉查询
		ResultResp resp=null;
		List<AgentInfo> agentLists=null;
		List<MerchantInfo> merchantlist=null;
		MemberCardBin mOne=null;
		String cardNo="";
		try{
			agentInfo.setPage(new Page(false));
			agentLists=agentInfoService.findByPage(agentInfo);
			//商户下拉查询
			merchantInfo.setPage(new Page(false));
			//查询OEM商户
			merchantInfo.setMerType(1);
			merchantlist =merchantService.findByPageMerInfo(merchantInfo,request);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}
		if(merchantlist==null||merchantlist.size()==0){
			resp = ResultResp.getInstance(ResultCode.userNotAgent);
		}
		try{
			//卡bin号码
			if (binId != null) {
				mOne = memberCardBinService.selectByIdCardBinOne(binId);
				cardNo=mOne.getCardBinNo().substring(1, mOne.getCardBinNo().length()-1);
			}
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}
		model.addAttribute("agentLists", agentLists);
		model.addAttribute("merchantList", merchantlist);
		model.addAttribute("CardNo", cardNo);
		model.addAttribute("mOne", mOne);
		model.addAttribute("result",resp);
		return "jsp/card/cardBinNew";
	}

	/**
	 * 打开制卡申请页面
	 * @param memberCardBin
	 * @param model
	 * @return
	 */
	@RequiresPermissions("mcard:openapply:create")
	@RequestMapping(value = "/openCardBatch")
	public String getAddCardBatch(MemberCardBin memberCardBin, Model model,HttpServletRequest request) {
		List<MemberCardBin> cardBinListBatch=null;
		ResultResp resp=null;
		try{
			//根据运营商和商户自身的卡BIN
			SysUsers user = this.getCurrentUser(request);
			if(user.getNotype().intValue()==UserType.agent.getValue())
				memberCardBin.setAgentId(user.getUserno());
			if(user.getNotype().intValue()==UserType.merchant.getValue())
				memberCardBin.setMerIds(user.getUserno());
			cardBinListBatch = memberCardBinService.cardBinList(memberCardBin);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("openCardBatch error!", e);
		}
		model.addAttribute("cardBinList", cardBinListBatch);
		model.addAttribute("result",resp);
		return "jsp/card/cardBatchNew";
	}
	/**
	 * 判断卡批次数和卡Bin不能重复
	 * */
	@RequestMapping(value="/remoteBin")
	public void remoteName(String batchId,CardBatchInfo cardInfo,HttpServletResponse response){
		boolean res =false;
		if(batchId!=null){
			cardInfo.setBatchId(batchId);
		}
		List<CardBatchInfo> list=cardBatchInfoService.cardBathList(cardInfo);
		if(list.size()==0){
			res = true;
		}
		this.writeJson(res, response);
	}

	// 前台日期格式字符串传入后台自动转换
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}

	/**
	 * 新增
	 * 
	 * @param cardBin
	 * @param model
	 * @return
	 * @throws Exception 
	 */
	@RequiresPermissions("mcard:bin:create")
	@RequestMapping("/addCardBin")
	public void addCardBin(MemberCardBin cardBin, Model model, HttpServletResponse response) throws Exception {
		ResultResp resp=null;
		try{
			int res = memberCardBinService.insertCardBin(cardBin);
			if(res==1)
				resp = ResultResp.getInstance(ResultCode.success);
			 else 
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("addCardBin error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
		}

	/**
	 * 卡Bin 修改操作
	 * @param binId
	 * @param cardBin
	 * @param model
	 * @param response
	 * @throws Exception
	 */
	@RequiresPermissions("mcard:bin:update")
	@RequestMapping(value = "updateCardBin")
	public void updateCardBin(Integer binId, MemberCardBin cardBin, Model model, HttpServletResponse response) throws Exception {
		ResultResp resp=null;
		try{
			int res = memberCardBinService.updateCardBin(cardBin);
			if(res==1)
				resp = ResultResp.getInstance(ResultCode.success);
			 else 
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("updateCardBin error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
		}
	/**
	 * 卡Bin 删除操作
	 * @param binId
	 * @return
	 */
	@RequiresPermissions("mcard:bin:delete")
	@RequestMapping(value = "deletCardBin")
	public String deleteCardBin(@RequestParam Integer[] binId) {
		if (binId != null && binId.length > 0) {
			for (int i = 0; i < binId.length; i++) {
				memberCardBinService.deleteByIdCardBin(binId[i]);
			}
		}
		return "redirect:/card/cardBinList";
	}
	/**
	 *制卡申请  新增
	 * @param cardBin
	 * @param model
	 * @return
	 */
	@RequiresPermissions("mcard:openapply:create")
	@RequestMapping("/addCardBatch")
	public void addCardBatch(CardBatchInfo cardBatch, Model model,String cardBinName, HttpServletResponse response,HttpServletRequest request) {
		ResultResp resp=null;
		try{
			int res = cardBatchInfoService.insertCardBatch(cardBatch, cardBinName, response, request);
			if(res==1)
				resp = ResultResp.getInstance(ResultCode.success);
			 else 
				resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("addCardBatch error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
	}
	/**
	 * 打开卡Bin添加页面
	 * @throws Exception 
	 */
	@RequiresPermissions(value={"mcard:openaudit:audit"},logical=Logical.OR)
	@RequestMapping(value = "/openCardBatchOne")
	public String openCardBatchOne(String cardBatchId,Model model) throws Exception {
		CardBatchInfo cardBatchOne=null;
		ResultResp resp=null;
		try{
			cardBatchOne=cardBatchInfoService.selectByIdBatch(cardBatchId);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("cardBatchInfo error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		model.addAttribute("cardOne", cardBatchOne);
		model.addAttribute("result", resp);
		return "jsp/card/cardUpdatePrint";
	}
	/**
	 * 打开卡Bin详情页面
	 * @throws Exception 
	 */
	@RequiresPermissions("mcard:openaudit:view2")
	@RequestMapping(value = "/openCardBatchView")
	public String openCardBatchView(String cardBatchId,Model model) throws Exception {
		CardBatchInfo cardBatchOne=null;
		ResultResp resp=null;
		try{
			cardBatchOne=cardBatchInfoService.selectByIdBatch(cardBatchId);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("cardBatchInfo error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		model.addAttribute("cardOne", cardBatchOne);
		model.addAttribute("result", resp);
		return "jsp/card/cardBatchView";
	}
	/**
	 * 制卡审核  通过  驳回
	 * */
	@RequiresPermissions("mcard:openaudit:audit")
	@RequestMapping(value="/updateBatch")
	public void updateBatch(Integer cardBatchId,Integer batchStat,CardBatchInfo cardInfo,Model model,HttpServletResponse response){
		ResultResp resp=null;
		try{
		int res=cardBatchInfoService.updateCardBatch(cardInfo, batchStat,cardBatchId);
		if(res==1)
			resp = ResultResp.getInstance(ResultCode.success);
		 else 
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}
		catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("updateBatch error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
	}
	
	
	/**
	 * 生成制卡文件
	 * @throws IOException 
	 * @throws ServletException 
	 * */
	@RequiresPermissions("mcard:openaudit:audit")
	@RequestMapping(value = "/inertCardInfo")
	public void insertCardInfo(HttpServletRequest request,String cardBatchId,HttpServletResponse response) throws IOException, ServletException {
		ResultResp resp=null;
		try{
			int terRes=cardInfoService.insertCardInfo(request, cardBatchId, response);
		if(terRes==1)
			resp = ResultResp.getInstance(ResultCode.success);
		 else 
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}
		catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("inertCardInfo error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
	}
	/**
	 * 制卡文件txt转成ZIp格式下载
	 * @throws Exception 
	 * */
	@RequiresPermissions("mcard:openaudit:audit")
	@RequestMapping(value = "/downloadZIP")
	public void ZIP(HttpServletRequest request,String cardBatchId,HttpServletResponse response) throws Exception {
		 cardInfoService.downloadZIP(request, cardBatchId, response);
	}
	/**
	 * 卡库存管理List页面
	 **/
	@RequiresPermissions("mcard:carrep:view")
	@RequestMapping(value="cardTotalList")
	public String cardTotalList(CardTotalInfo cardTotalInfo, Model model,HttpServletRequest request){
		ResultResp resp=null;
		List<CardTotalInfo> cardTotalInfoList=null;
		/*
		 * 根据当前用户判定是运营商还是商户 商户查询属于自己商户的卡号
		 * 运营商查询当前运营商的且不是商户的库存
		 */
		SysUsers user = (SysUsers) request.getSession().getAttribute(Constants.CURRENT_USER);
		//运营商
		if(user.getNotype().intValue()==UserType.agent.getValue())
			cardTotalInfo.getMemberCardBin().setAgentId(user.getUserno());
		//商户
		if(user.getNotype().intValue()==UserType.merchant.getValue())
			cardTotalInfo.getMemberCardBin().setMerIds(user.getUserno());
		try{
			cardTotalInfoList=cardTotalService.findByPageCardTotalList(cardTotalInfo);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("cardTotalList error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		
		model.addAttribute("cardTotalInfoList", cardTotalInfoList);
		model.addAttribute("cardTotalpage", cardTotalInfo);
		model.addAttribute("result",resp);
		return "jsp/card/cardTotalList";
	}
	/**
	 * 会员卡查询
	 **/
	@RequiresPermissions("mcard:query:view")
	@RequestMapping(value="cardMemberList")
	public String cardMemberList(CardInfo cardInfo, Model model,HttpServletRequest request){
		
		ResultResp resp=null;
		List<CardInfo> cardMemberList=null;
		
		try{
			/*
			 * 根据当前用户判定是运营商还是商户 商户查询属于自己商户的卡批次的
			 * 运营商查询当前运营商的会员卡
			 */
			SysUsers user = (SysUsers) request.getSession().getAttribute(Constants.CURRENT_USER);
			//运营商
			if(user.getNotype().intValue()==UserType.agent.getValue())
				cardInfo.setAgentCode(user.getUserno());
			//商户
			if(user.getNotype().intValue()==UserType.merchant.getValue())
				cardInfo.setMerIds(user.getUserno());
			cardMemberList=cardInfoService.findByPageCardMemberList(cardInfo);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("cardTotalList error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		model.addAttribute("cardMemberList", cardMemberList);
		model.addAttribute("cardInfopage", cardInfo);
		model.addAttribute("result",resp);
		return "jsp/card/cardMemberList";
	}
	/**
	 * 查看会员卡详细信息
	 */
	@RequiresPermissions("mcard:query:view")
	@RequestMapping(value = "/openCardMemberOne")
	public String openCardMemberOne(String cardId,Model model) {
		CardInfo cardInfo=cardInfoService.findByOneCardMemberById(cardId);
		model.addAttribute("cardInfo", cardInfo);
		return "jsp/card/cardMemberView";
	}
	/**
	 * 推广员录入 转入页面
	 **/
	@RequiresPermissions("mcard:tuseapply:create")
	@RequestMapping(value="toAddCardPromoter")
	public String toAddCardPromoter(HttpServletRequest request,CardBatchInfo cardBatchInfo, Model model){
		//根据运营商和商户自身的卡BIN
		ResultResp resp=null;
		List<CardBatchInfo> mkSeriNoList=null;
		try{
			SysUsers user = (SysUsers) request.getSession().getAttribute(Constants.CURRENT_USER);
			if(user.getNotype().intValue()==UserType.agent.getValue())
				cardBatchInfo.getMemberCardBin().setAgentId(user.getUserno());
			if(user.getNotype().intValue()==UserType.merchant.getValue())
				cardBatchInfo.getMemberCardBin().setMerIds(user.getUserno());
			//查询制卡成功 状态为3的 制卡序号 并根据自身的属性去查询相应的卡
			mkSeriNoList=cardBatchInfoService.selectMkSeriNo(cardBatchInfo);
			}catch(BaseException e){
				log.info(e.getMessage());
				resp = ResultResp.getInstance(false, e.getMessage());
			}catch(Exception e){
				log.error("toAddCardPromoter error!", e);
				resp = ResultResp.getInstance(ResultCode.unKnowErr);
			}
			//model.addAttribute("code", agentCode);
			model.addAttribute("mkSeriNos", mkSeriNoList);
			model.addAttribute("result",resp);
		return "jsp/card/cardPromoterApplyNew";
	}
	/**
	 * 推广员领用审核 List页面
	 * @param model
	 * @param cardApplyInfo
	 * @return
	 */
	@RequiresPermissions("mcard:tuselog:view")
	@RequestMapping(value="/queryPromoterCardApply")
	public String queryPromoterCardApply(Model model,CardApplyInfo cardApplyInfo,HttpServletRequest request){
		//推广人员
		ResultResp resp=null;
		List<CardApplyInfo> applyList=null; 
		try{
			cardApplyInfo.setApplyType(new Byte("1"));
			SysUsers user = (SysUsers) request.getSession().getAttribute(Constants.CURRENT_USER);
			if(user.getNotype().intValue()==UserType.agent.getValue())
				cardApplyInfo.getMemberCardBin().setAgentId(user.getUserno());
			if(user.getNotype().intValue()==UserType.merchant.getValue())
				cardApplyInfo.getMemberCardBin().setMerIds(user.getUserno());
			applyList=cardApplyInfoService.findByPageCardApplyList(cardApplyInfo);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("queryCardApply error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		model.addAttribute("applyList",applyList);
		model.addAttribute("applyPage",cardApplyInfo);
		model.addAttribute("result", resp);
		return "jsp/card/cardPromoterApplyList";
	}
	
	
	/**
	 * 打开领卡信息申请页面
	 */
	@RequiresPermissions("mcard:useapply:create")
	@RequestMapping(value = "/openCardApply")
	public String openCardApply(HttpServletRequest request,Model model,CardBatchInfo cardBatchInfo) {
		//根据运营商和商户自身的卡BIN
		ResultResp resp=null;
		List<CardBatchInfo> mkSeriNoList=null;
		try{
			SysUsers user = (SysUsers) request.getSession().getAttribute(Constants.CURRENT_USER);
			if(user.getNotype().intValue()==UserType.agent.getValue())
				cardBatchInfo.getMemberCardBin().setAgentId(user.getUserno());
			if(user.getNotype().intValue()==UserType.merchant.getValue())
				cardBatchInfo.getMemberCardBin().setMerIds(user.getUserno());
			//查询制卡成功 状态为3的 制卡序号 并根据自身的属性去查询相应的卡
			mkSeriNoList=cardBatchInfoService.selectMkSeriNo(cardBatchInfo);
			}catch(BaseException e){
				log.info(e.getMessage());
				resp = ResultResp.getInstance(false, e.getMessage());
			}catch(Exception e){
				log.error("openCardApply error!", e);
				resp = ResultResp.getInstance(ResultCode.unKnowErr);
			}
			//model.addAttribute("code", agentCode);
			model.addAttribute("mkSeriNos", mkSeriNoList);
			model.addAttribute("result",resp);
		return "jsp/card/cardApplyNew";
	}
	/**
	 * 新增卡片领用申请
	 * @param cardApplyInfo
	 * @param request
	 * @param response
	 * @throws ParseException
	 */
	@RequiresPermissions("mcard:useapply:create")
	@RequestMapping(value="addCardApply")
	public void addCardAppy(CardApplyInfo cardApplyInfo,HttpServletRequest request,HttpServletResponse response) throws ParseException{
		ResultResp resp=null;
		try{
			int res=cardApplyInfoService.insertCardBin(cardApplyInfo, request);
		if(res==1)
			resp = ResultResp.getInstance(ResultCode.success);
		 else 
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}
		catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("inertCardInfo error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
	}
	/**
	 * 卡片领用审核 List页面
	 * @param model
	 * @param cardApplyInfo
	 * @return
	 */
	@RequiresPermissions("mcard:useaudit:view")
	@RequestMapping(value="/queryCardApply")
	public String queryCardApply(Model model,CardApplyInfo cardApplyInfo,HttpServletRequest request ){
		List<CardApplyInfo> applyList=null;
		ResultResp resp=null;
		try{
			SysUsers user = (SysUsers) request.getSession().getAttribute(Constants.CURRENT_USER);
			if(user.getNotype().intValue()==UserType.agent.getValue())
				cardApplyInfo.getMemberCardBin().setAgentId(user.getUserno());
			if(user.getNotype().intValue()==UserType.merchant.getValue())
				cardApplyInfo.getMemberCardBin().setMerIds(user.getUserno());
			applyList=cardApplyInfoService.findByPageCardApplyList(cardApplyInfo);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("queryCardApply error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		model.addAttribute("applyList",applyList);
		model.addAttribute("applyPage",cardApplyInfo);
		model.addAttribute("result", resp);
		return "jsp/card/cardApplyList";
	}
	/**
	 * 打开领卡审核页面
	 * @param cardApplyId
	 * @param request
	 * @param model
	 * @return
	 */
	@RequiresPermissions(value={"mcard:useaudit:*","mcard:tuseaudit:*"},logical=Logical.OR)
	@RequestMapping(value = "/openAuditCardApply")
	public String openAuditCardApply(Integer cardApplyId,HttpServletRequest request,Model model) {
//		//获取当前当了逇运营商名称agentName
//		AgentInfo agent=this.getCurrentAgent(request);
//		String agentCode=agent.getAgentCode();
//		CardApplyInfo apply=cardApplyInfoService.selectByIdApply(cardApplyId);
//		model.addAttribute("applyAudit", apply);
//		model.addAttribute("code", agentCode);
//		//获取卡Bin号和卡批次号
//		List<CardBatchInfo> mk=cardBatchInfoService.selectMkSeriNo();
//		CardBatchInfo mks=mk.get(0);
//		String cardBinNo=mks.getCardBinNo();
//		String  batchId=mks.getBatchId();
//		model.addAttribute("cardBNo",cardBinNo);
//		model.addAttribute("batId",batchId);
//		model.addAttribute("cardType",request.getParameter("cardType"));
		return "jsp/card/cardApplyAuditDetails";
	} 
	/**
	 * 卡片领用审核  驳回、通过
	 * @param cardApplyInfo
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/updateAuditApply")
	public void updateApplyAudit(Model model,Integer cardApplyId,Integer applyStat, CardApplyInfo cardApplyInfo,HttpServletRequest request,HttpServletResponse response){
		ResultResp resp=null;
		try{
			int res=cardApplyInfoService.updateCardApply(cardApplyId, applyStat, cardApplyInfo, request, response);
		if(res==1)
			resp = ResultResp.getInstance(ResultCode.success);
		 else 
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}
		catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("inertCardInfo error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
	}
	/**
	 * 返回卡bin 增加中的制卡序号和制卡批次
	 * @param cardInfo
	 * @param cardBinName
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/batchIdSeriNo")
	public  void batchIdSeriNo(CardBatchInfo cardInfo, String cardBinName, HttpServletResponse response, HttpServletRequest request) {
		String res= cardBatchInfoService.batchIdSeriNo(cardInfo, cardBinName, response, request);
		Json json = new Json();
		json.setResult(true);
		json.setMsg("成功");
		json.setObj(res.split(","));
		 this.writeJson(json, response);
	}
	/**
	 * 查询卡bin号或者卡名称已经存在
	 * @param cardInfo
	 * @param cardBinName
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/checkCardNoOrName")
	public  void checkCardNoOrName(MemberCardBin memberCardBin,HttpServletResponse response, HttpServletRequest request) {
		int res= memberCardBinService.checkCardNoOrName(memberCardBin);
		this.writeJson(res, response);
	}
	/**
	 * 图片下载
	 * @throws Exception 
	 * */
	@RequestMapping(value = "/downloadImg")
	public void downloadImg(HttpServletRequest request,HttpServletResponse response) throws Exception {
		ImageUtil.downloadImg(request, response);
    }
	/**
	 *  制卡审核EXL导出
	 * @param cardBatchInfoList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("mcard:openaudit:download")
	@RequestMapping(value = "/cardBatchEXLDownLoad", method = RequestMethod.GET)
	public String cardBatchEXLDownLoad(CardBatchInfo cardBatchInfoList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("卡批次信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = {"","运营商编号", "卡bin","制卡时间", "数量","批次","起始号","结束号","卡批次状态","入库状态",
								"卡有效期","制卡序号","服务码","数据生成状态","最后修改ID","最后修改时间"};
			exportExcel(cardBatchInfoList, titles, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 制卡审核EXL导出
	 * @param cardBatchInfoList
	 * @param titles
	 * @param outputStream
	 * @param request
	 */
	public void exportExcel(CardBatchInfo cardBatchInfoList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request){
		cardBatchInfoList.setPage(new Page(false));
		List<CardBatchInfo> cardBatchInfoLists=cardBatchInfoService.findByPageCardBatchList(cardBatchInfoList);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook();
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.createSheet("卡批次信息");
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle headStyle = exportUtil.getHeadStyle();
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		// 构建表头
		XSSFRow headRow = sheet.createRow(0);
		XSSFCell cell = null;
		for (int i = 0; i < titles.length; i++){
			cell = headRow.createCell(i);
			cell.setCellStyle(headStyle);
			cell.setCellValue(titles[i]);
		}
		int j = 0;
		// 构建表体数据
		for (CardBatchInfo info : cardBatchInfoLists) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			//运营商编号
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentCode()));
			//卡bin
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardBinNo()));
			//制卡时间
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getMakeDate()==null?"":DateUtils.dateToStr(info.getMakeDate(),"yyyy-MM-dd"));
			//数量
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardCount()));
			//批次
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBatchId()));
			//起始号
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardNoStart()));
			//结束号
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardNoEnd()));
			//卡批次状态
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBatchStat()));
			//入库状态
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getStockStat()));
			//卡有效期
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getExpireDt()));
			//制卡序号
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMkSeriNo()));
			//服务码
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getServerCode()));
			//数据生成状态
			cell = bodyRow.createCell(13);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getDataGenStat()));
			//最后修改Id
			cell = bodyRow.createCell(14);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getLastUpdUid()));
			//最后修改时间
			cell = bodyRow.createCell(15);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getLastUpdTs()==null?"":DateUtils.dateToStr(info.getLastUpdTs(),"yyyy-MM-dd"));
			j++;
		}try{
			workBook.write(outputStream);
			outputStream.flush();
		}catch (IOException e){
			e.printStackTrace();
		}finally{
			try{
				outputStream.close();
			}catch (IOException e){
				e.printStackTrace();
			}
		}
	}
	/**
	 * 卡bin管理信息
	 * @param memberCardBinList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("mcard:bin:download")
	@RequestMapping(value = "/cardBinEXLDownLoad", method = RequestMethod.GET)
	public String cardBinEXLDownLoad(MemberCardBin memberCardBinList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("卡Bin信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = {"","卡Bin号", "名称","类型", "运营商id","商户id","是否带密码","创建日期","最后修改日期"};
			cardBinExportExcel(memberCardBinList, titles, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 卡bin管理信息
	 * @param memberCardBinList
	 * @param titles
	 * @param outputStream
	 * @param request
	 */
	public void cardBinExportExcel(MemberCardBin memberCardBinList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request){
		memberCardBinList.setPage(new Page(false));
		List<MemberCardBin> memberCardBinLists= memberCardBinService.findByPageCardBin(memberCardBinList);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook();
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.createSheet("卡Bin信息");
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle headStyle = exportUtil.getHeadStyle();
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		// 构建表头
		XSSFRow headRow = sheet.createRow(0);
		XSSFCell cell = null;
		for (int i = 0; i < titles.length; i++){
			cell = headRow.createCell(i);
			cell.setCellStyle(headStyle);
			cell.setCellValue(titles[i]);
		}
		int j = 0;
		// 构建表体数据
		for (MemberCardBin info : memberCardBinLists) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			//卡bin号
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardBinNo()));
			//名称
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardBinName()));
			//类型
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBinType()));
			//运营商Id
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentId()));
			//商户id
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerId()));
			//是否带密码
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getHavaPwd()));
			//创建日期
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getCreateDate()==null?"":DateUtils.dateToStr(info.getCreateDate(),"yyyy-MM-dd"));
			//最后修改时间
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getLastUpdTs()==null?"":DateUtils.dateToStr(info.getLastUpdTs(),"yyyy-MM-dd"));
			j++;
		}try{
			workBook.write(outputStream);
			outputStream.flush();
		}catch (IOException e){
			e.printStackTrace();
		}finally{
			try{
				outputStream.close();
			}catch (IOException e){
				e.printStackTrace();
			}
		}
	}
	/**
	 * 卡库存信息
	 * @param cardTotalInfo
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("mcard:carrep:download")
	@RequestMapping(value = "/cardTotalEXLDownLoad", method = RequestMethod.GET)
	public String cardTotalEXLDownLoad(CardTotalInfo cardTotalInfo,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("卡库存汇总信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = {"","统计日期", "卡总数","未启用总数", "出库未激活总数","激活卡总数","冻结卡总数","挂失卡总数","卡批次","卡名称","卡Bin号"};
			cardTotalExportExcel(cardTotalInfo, titles, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 卡库存信息
	 * @param cardTotalInfo
	 * @param titles
	 * @param outputStream
	 * @param request
	 */
	public void cardTotalExportExcel(CardTotalInfo cardTotalInfo,String[] titles, ServletOutputStream outputStream,HttpServletRequest request){
		cardTotalInfo.setPage(new Page(false));
		List<CardTotalInfo> cardTotalInfoList= cardTotalService.findByPageCardTotalList(cardTotalInfo);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook();
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.createSheet("卡库存汇总信息");
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle headStyle = exportUtil.getHeadStyle();
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		// 构建表头
		XSSFRow headRow = sheet.createRow(0);
		XSSFCell cell = null;
		for (int i = 0; i < titles.length; i++){
			cell = headRow.createCell(i);
			cell.setCellStyle(headStyle);
			cell.setCellValue(titles[i]);
		}
		int j = 0;
		// 构建表体数据
		for (CardTotalInfo info : cardTotalInfoList) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			//统计日期
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getTotalDate()==null?"":DateUtils.dateToStr(info.getTotalDate(),"yyyy-MM-dd"));
			//卡总数
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalCount()));
			//未启用总数
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getStockCount()));
			//出库未激活总数
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentStockCount()));
			//激活总数
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getActivityCount()));
			//冻结卡总数
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getFreezeCount()));
			//挂失卡总数
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getLostCount()));
			//卡批次
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getLostCount()));
			//卡名称
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardBinName()));
			//卡Bin号
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardBinNo()));
			j++;
		}try{
			workBook.write(outputStream);
			outputStream.flush();
		}catch (IOException e){
			e.printStackTrace();
		}finally{
			try{
				outputStream.close();
			}catch (IOException e){
				e.printStackTrace();
			}
		}
	}
	/**
	 * 会员卡信息EXL导出
	 * @param cardInfo
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("mcard:carrep:download")
	@RequestMapping(value = "/cardMemberEXLDownLoad", method = RequestMethod.GET)
	public String cardMemberEXLDownLoad(CardInfo cardInfo,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("会员卡信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = {"","卡序列号", "卡号","卡类型", "卡批次号","状态","有效期","卡密码","二磁道","卡密错误次数",
								"会员Id","绑定日期","运营商编号","推广员id","最后修改时刻"};
			cardMemberExportExcel(cardInfo, titles, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 会员卡信息EXL
	 * @param cardInfo
	 * @param titles
	 * @param outputStream
	 * @param request
	 */
	public void cardMemberExportExcel(CardInfo cardInfo,String[] titles, ServletOutputStream outputStream,HttpServletRequest request){
		cardInfo.setPage(new Page(false));
		List<CardInfo> cardInfoList= cardInfoService.findByPageCardMemberList(cardInfo);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook();
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.createSheet("会员卡信息");
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle headStyle = exportUtil.getHeadStyle();
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		// 构建表头
		XSSFRow headRow = sheet.createRow(0);
		XSSFCell cell = null;
		for (int i = 0; i < titles.length; i++){
			cell = headRow.createCell(i);
			cell.setCellStyle(headStyle);
			cell.setCellValue(titles[i]);
		}
		int j = 0;
		// 构建表体数据
		for (CardInfo info : cardInfoList) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			//卡序列号
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardSeriNo()));
			//卡号
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardNo()));
			//卡类型
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardTypeCode()));
			//卡批次号
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardBatchId()));
			//状态
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardStat()));
			//有效期
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getExpireDt()));
			//卡密码
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardPwd()));
			//二磁道
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getC2data()));
			//卡密码错误次数
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getPwdErrCount()));
			//会员id
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMemberId()));
			//绑定日期
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getBindDate()==null?"":DateUtils.dateToStr(info.getBindDate(),"yyyy-MM-dd"));
			//运营商编号
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentCode()));
			//推广员id
			cell = bodyRow.createCell(13);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getExtMemberId()));
			//最后修改时刻
			cell = bodyRow.createCell(14);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getLastUpdTs()==null?"":DateUtils.dateToStr(info.getLastUpdTs(),"yyyy-MM-dd"));
			j++;
		}try{
			workBook.write(outputStream);
			outputStream.flush();
		}catch (IOException e){
			e.printStackTrace();
		}finally{
			try{
				outputStream.close();
			}catch (IOException e){
				e.printStackTrace();
			}
		}
	}
}
