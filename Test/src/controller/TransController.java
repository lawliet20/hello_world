package com.mk.pro.manage.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mk.pro.commons.enums.TradeType;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.AgentInfo;
import com.mk.pro.model.AreaCodeInfo;
import com.mk.pro.model.AreaDistrictInfo;
import com.mk.pro.model.CuptransLogInfo;
import com.mk.pro.model.MemberInfo;
import com.mk.pro.model.MerchantCategory;
import com.mk.pro.model.MerchantInfo;
import com.mk.pro.model.Page;
import com.mk.pro.model.TerminalInfo;
import com.mk.pro.model.TransLogInfo;
import com.mk.pro.model.TransProfitInfo;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.service.AreaCodeInfoService;
import com.mk.pro.service.AreaDistrictInfoService;
import com.mk.pro.service.CuptransLogInfoService;
import com.mk.pro.service.MemberService;
import com.mk.pro.service.MerchantCategoryService;
import com.mk.pro.service.MerchantService;
import com.mk.pro.service.TerminalInfoService;
import com.mk.pro.service.TransLogInfoService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.ExportUtil;
import com.mk.pro.utils.MyStringUtil;

/**
 * 
 * @author:ChengKang
 * @date:2015-5-6 下午5:30:48
 * 
 **/
@Controller
@RequestMapping(value = "/trans")
public class TransController extends BaseController<Object, String> {
	@Resource
	private TransLogInfoService transLogInfoService;
	@Resource
	private MemberService memberService;
	@Resource
	private MerchantService merchantService;
	@Resource
	private TerminalInfoService terminalInfoService;
	@Resource
	private AreaCodeInfoService areaCodeInfoService;
	@Resource
	private AreaDistrictInfoService areaDistrictInfoService;
	@Resource
	private MerchantCategoryService merchantCategoryService;
	@Resource
	private CuptransLogInfoService cuptransLogInfoService;

	/**
	 * 当前交易查询列表信息
	 * */
	@RequiresPermissions("trans:current:view")
	@RequestMapping(value = "/transList")
	public String transFindList(TransLogInfo transLogInfo, Model model, HttpServletRequest request) throws Exception {
		AgentInfo agent = this.getCurrentAgent(request);
		if (agent == null) {
			throw new IllegalStateException("userid is null agent");
		}
		if(transLogInfo.getKssj()==null && transLogInfo.getJssj()==null){
			String nowTime=DateUtils.getNowTime("yyyy-MM-dd");
			model.addAttribute("nowTime", nowTime);
			transLogInfo.setKssj(nowTime);
			transLogInfo.setJssj(nowTime);
		}
		// 查询交易流水列表
		List<TransLogInfo> transLists = transLogInfoService.getPageForTrans(transLogInfo, request);
//		//查询汇总笔数 查询汇总金额
//		Map<String, Object> tranLogInfoAmount=transLogInfoService.getTranLogInfoAmount(transLogInfo);
		//总笔数
		model.addAttribute("allAmount", transLogInfo.getPage().getTotalAmount());
		//总金额
		model.addAttribute("allAmountCount",transLogInfo.getPage().getTotalResult());
		model.addAttribute("transList", transLists);
		model.addAttribute("transPage", transLogInfo);
		return "jsp/trans/transLogInfoList";
	}
	/**
	 * 风险查询列表信息
	 * */
	@RequestMapping(value = "/exptransFindList")
	public String exptransFindList(TransLogInfo transLogInfo, Model model, HttpServletRequest request) throws Exception {
		AgentInfo agent = this.getCurrentAgent(request);
		if (agent == null) {
			throw new IllegalStateException("userid is null agent");
		}
		if(transLogInfo.getKssj()==null && transLogInfo.getJssj()==null){
			String nowTime=DateUtils.getNowTime("yyyy-MM-dd");
			model.addAttribute("nowTime", nowTime);
			transLogInfo.setKssj(nowTime);
			transLogInfo.setJssj(nowTime);
		}
		transLogInfo.setExpType(1);
		// 查询交易流水列表
		List<TransLogInfo> transLists = transLogInfoService.getPageForTrans(transLogInfo, request);
		//总笔数
		model.addAttribute("allAmount", transLogInfo.getPage().getTotalAmount());
		//总金额
		model.addAttribute("allAmountCount",transLogInfo.getPage().getTotalResult());
		model.addAttribute("transList", transLists);
		model.addAttribute("transPage", transLogInfo);
		return "jsp/trans/expLogInfoList";
	}

	@RequiresPermissions("trans:current:view")
	@RequestMapping(value = "selectOneTrans")
	public String getSelectOneTrans(TransLogInfo transList, Model model, HttpServletRequest request) throws Exception {
		if (transList.getTransId() != null) {
			TransLogInfo transOne = transLogInfoService.getByOneTransLog(transList, request);
			TransProfitInfo transProfit = transLogInfoService.getTransProfitByTransLog(transList.getTransId());
			model.addAttribute("transOne", transOne);
			model.addAttribute("transProfit", transProfit);
		} else {
			throw new IllegalStateException("transId is null");
		}
		return "jsp/trans/transDetaila";
	}

	/**
	 * 查询会员的详情
	 */
	@RequiresPermissions("trans:current:view")
	@RequestMapping("/memberTransDetail")
	public String getMemberTransDetail(MemberInfo member, String memberNo, Model model) {
		if (memberNo == null) {
			throw new IllegalStateException("memberNo is null");
		}
		if(memberNo.length()<=11){
		MemberInfo m = memberService.selectByMobileNo(memberNo);
		model.addAttribute("memberDetail", m);
		}else{
			MemberInfo m = memberService.selectByCardId(Long.valueOf(memberNo));
			model.addAttribute("memberDetail", m);
		}
		return "/jsp/member/memberDetail";
	}

	/**
	 * 根据merchantNo打开商户信息详情页面
	 * 
	 * @throws Exception
	 */
	@RequiresPermissions("trans:current:view")
	@RequestMapping(value = "/merTransOne")
	public String getMerOne(TransLogInfo t, MerchantInfo m, TerminalInfo merTer, String merchantNo, Model model, Integer merId) throws Exception {
		// 根据id查询一条记录
		if (merchantNo != null) {
			//根据id查询一条信息
			MerchantInfo merOne = merchantService.selectMerTransOne(merchantNo);
			model.addAttribute("merOne", merOne);
//			Integer id=merOne.getCategoryId();
//			MerchantCategory cat=merchantCategoryService.selectByPrimaryKey(id);
//			model.addAttribute("categoryView", cat.getCategory());
			if(merOne.getDistrictId()!=null){
				AreaDistrictInfo sen=areaDistrictInfoService.selectByPrimaryKey(merOne.getDistrictId());
				if(sen!=null){
					model.addAttribute("areanames", sen.getDistrictName());
				}
			}
//			if(merOne.getProvId()!=null){
//				AreaCodeInfo addr = areaCodeInfoService.findOne(Integer.valueOf(merOne.getProvId()));
//				String areaName=addr.getAreaName();//地区名称
//				model.addAttribute("areaName",areaName);
//			}
			if (merOne.getMerId() != null) {
				merTer.setMerId(merOne.getMerId());
				// 查询列表
				List<TerminalInfo> merTerminList = terminalInfoService.findByPageTerminal(merTer);
				model.addAttribute("merTerminList", merTerminList);
				model.addAttribute("merTer", merTer);
			}
		}
		return "jsp/merchant/merView";
	}
	/**
	 * 当前交易EXL导出
	 * @param transList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("trans:current:download")
	@RequestMapping(value = "/TransLogDownLoad", method = RequestMethod.GET)
	public String TransLogDownLoad(TransLogInfo transList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("交易流水信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = {"序号","交易日期", "交易时间","运营商", "商户名称" , "商户号","终端号","交易流水号","卡号","会员号","交易金额","交易类型","交易标志","对账标示","交易状态"};
			exportExcel(transList, titles, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 当前交易导出
	 * @param transList
	 * @param titles
	 * @param outputStream
	 * @param request
	 */
	public void exportExcel(TransLogInfo transList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request){
		transList.getPage().setFenye(false);
		List<TransLogInfo> TranList= transLogInfoService.getPageForTrans(transList,request);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook();
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.createSheet("交易流水信息");
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
		for (TransLogInfo info : TranList) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			//内部交易日期
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getIntTxnDt()));
			//内部交易时间
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getIntTxnTm()));
			//终端号
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentNo()));
			//交易流水号
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerName()));
			//交易金额
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerchantNo()));
			//会员号
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTerminalNo()));
			//银行卡号
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTermSeqId()));
			//交易类型
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBankCardNo()));
			
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMemberNo()));
			
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransAmount()));
			
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(TradeType.getExamType(info.getTradeType())));
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			if(info.getTransFlag().equals("00")){
				cell.setCellValue(MyStringUtil.obj2Str("平台"));
			}
			if(info.getTransFlag().equals("01")){
				cell.setCellValue(MyStringUtil.obj2Str("银行卡"));	
			}
			if(info.getTransFlag().equals("02")){
				cell.setCellValue(MyStringUtil.obj2Str("支付宝"));
			}
			if(info.getTransFlag().equals("03")){
				cell.setCellValue(MyStringUtil.obj2Str("微信"));
			}
			if(info.getTransFlag().equals("04")){
				cell.setCellValue(MyStringUtil.obj2Str("现金"));
			}
			if(info.getTransFlag().equals("05")){
				cell.setCellValue(MyStringUtil.obj2Str("e豆"));
			}
			if(info.getTransFlag().equals("06")){
				cell.setCellValue(MyStringUtil.obj2Str("代金卷"));
			}
			if(info.getTransFlag().equals("07")){
				cell.setCellValue(MyStringUtil.obj2Str("app"));
			}
			if(info.getTransFlag().equals("08")){
				cell.setCellValue(MyStringUtil.obj2Str("百度"));
			}
			
			
			cell = bodyRow.createCell(13);
			cell.setCellStyle(bodyStyle);
			if(info.getCheckStatus()==0){
				cell.setCellValue(MyStringUtil.obj2Str("未对账"));
			}
			if(info.getCheckStatus()==1){
				cell.setCellValue(MyStringUtil.obj2Str("对平"));
			}
			if(info.getCheckStatus()==2){
				cell.setCellValue(MyStringUtil.obj2Str("单边"));
			}
			if(info.getCheckStatus()==3){
				cell.setCellValue(MyStringUtil.obj2Str("已补录"));
			}
			if(info.getCheckStatus()==4){
				cell.setCellValue(MyStringUtil.obj2Str("商户打款成功"));
			}
			if(info.getCheckStatus()==5){
				cell.setCellValue(MyStringUtil.obj2Str("商户打款失败"));
			}
			cell = bodyRow.createCell(14);
			cell.setCellStyle(bodyStyle);
			if(info.getTransStat()==0){
				cell.setCellValue(MyStringUtil.obj2Str("未处理"));
			}
			if(info.getTransStat()==1){
				cell.setCellValue(MyStringUtil.obj2Str("失败"));
			}
			if(info.getTransStat()==2){
				cell.setCellValue(MyStringUtil.obj2Str("成功"));
			}
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
	 *  银联交易数据查询
	 * @param cuptransLog
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/cuptransLogList")
	public String cuptransLogList(CuptransLogInfo cuptransLog, Model model, HttpServletRequest request) throws Exception {
		// 银联交易数据查询表
		if(cuptransLog.getBeginTimeStr()!=null && cuptransLog.getBeginTimeStr()!=""){
			cuptransLog.setBeginTimeStr(cuptransLog.getBeginTimeStr().replace("-", ""));
		}
		if(cuptransLog.getEndTimeStr()!=null && cuptransLog.getEndTimeStr()!=""){
			cuptransLog.setEndTimeStr(cuptransLog.getEndTimeStr().replace("-", ""));
		}
		List<CuptransLogInfo> cuptransLogList = cuptransLogInfoService.findByPageCuptransList(cuptransLog);
		model.addAttribute("cuptransLogList", cuptransLogList);
		model.addAttribute("cuptransLogPage", cuptransLog);
		return "jsp/trans/CuptransLogList";
	}
	/**
	 * 银联数据详情页面
	 * @param id
	 * @param cuptransLog
	 * @param model
	 * @return
	 */
	@RequiresPermissions("trans:cuptrans:view")
	@RequestMapping("/getCuptransDetail")
	public String getCuptransDetail(Long id, CuptransLogInfo cuptransLog, Model model) {
		if (id == null ) {
			throw new IllegalStateException("id is null");
		}
		CuptransLogInfo c = cuptransLogInfoService.selectByPrimaryKey(id);
		CuptransLogInfo s = cuptransLogInfoService.selectByCupId(id);
			model.addAttribute("cuptransOne", s);
			model.addAttribute("cuptransDetail", c);
		return "/jsp/trans/cuptransDetaila";
	}
	/**
	 * 跟新对账信息
	 * @param id
	 * @param checkStatus
	 * @param model
	 */
	@RequiresPermissions("trans:current:checkStatus")
	@RequestMapping(value = "/updateCheckStatus")
	@ResponseBody
	public void updateCheckStatus(Long[] transId,Integer checkStatus,Model model) {
		if (transId != null && transId.length > 0) {
			// 循环更新
			for (int i = 0; i < transId.length; i++) {
				transLogInfoService.updateCheckStatus(transId[i],checkStatus);
			}
		}
	}
	/**
	 * 银联表跟新对账信息
	 * @param id
	 * @param checkStatus
	 * @param model
	 */
	@RequiresPermissions("trans:current:YLcheckStatus")
	@RequestMapping(value = "/updateYLCheckStatus")
	@ResponseBody
	public void updateYLCheckStatus(Long[] id,Integer checkStatus,Model model) {
		Json json=new Json();
		if (id != null && id.length > 0) {
			// 循环更新
			for (int i = 0; i < id.length; i++) {
				int res=cuptransLogInfoService.updateYLCheckStatus(id[i],checkStatus);
				if(res==1){
					json.setMsg("对账标示修改成功!");
					json.setResult(true);
					model.addAttribute("json", json);
				}else{
					json.setMsg("对账标示修改失败，请联系管理员!");
					json.setResult(false);
					model.addAttribute("json", json);
				}
			}
		}
	}
}
