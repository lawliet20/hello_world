package com.mk.pro.manage.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mk.pro.commons.ResultResp;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.enums.TradeType;
import com.mk.pro.commons.exception.BaseException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.AgentProfitSettleInfo;
import com.mk.pro.model.AgentSettleInfo;
import com.mk.pro.model.EdouTotalAcctInfo;
import com.mk.pro.model.MemberAcctTransLog;
import com.mk.pro.model.MerAcctTransLog;
import com.mk.pro.model.MerchantEdouSettleInfo;
import com.mk.pro.model.MerchantScanpaySettleInfo;
import com.mk.pro.model.MerchantSettleInfo;
import com.mk.pro.model.Page;
import com.mk.pro.model.ThirdpayTotalInfo;
import com.mk.pro.model.TransLogInfo;
import com.mk.pro.model.TransProfitTotalInfo;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.AgentProfitSettleInfoService;
import com.mk.pro.service.AgentSettleInfoService;
import com.mk.pro.service.EdouTotalAcctInfoService;
import com.mk.pro.service.MemberAcctTransLogService;
import com.mk.pro.service.MerAcctTransService;
import com.mk.pro.service.MerchantEdouSettleInfoService;
import com.mk.pro.service.MerchantScanpaySettleInfoService;
import com.mk.pro.service.MerchantSettleInfoService;
import com.mk.pro.service.ThirdpayTotalInfoService;
import com.mk.pro.service.TransLogInfoService;
import com.mk.pro.service.TransProfitTotalInfoService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.ExportUtil;
import com.mk.pro.utils.MyStringUtil;

/**
 * 所有报表类Controller
 * @author:ChengKang
 * @date:2015-9-1 下午2:17:55
 * 
 **/
@Controller
@RequestMapping(value = "/reportForms")
public class ReportFormsController extends BaseController{
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Resource
	AgentSettleInfoService agentSettleInfoService;
	@Resource
	EdouTotalAcctInfoService edouTotalAcctInfoService;
	@Resource
	MerchantEdouSettleInfoService merchantEdouSettleInfoService;
	@Resource
	AgentProfitSettleInfoService agentProfitSettleInfoService;
	@Resource
	TransProfitTotalInfoService transProfitTotalInfoService;
	@Resource
	ThirdpayTotalInfoService thirdpayTotalInfoService;
	@Resource
	TransLogInfoService transLogInfoService;
	@Resource
	MerAcctTransService merAcctTransService;
	@Resource
	MerchantSettleInfoService merchantSettleInfoService;
	@Resource
	MemberAcctTransLogService memberAcctTransLogService;
	@Resource
	MerchantScanpaySettleInfoService merchantScanpaySettleInfoService;
	
	/**
	 * 运营商分润汇总信息
	 * @param agentSettleInfo
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/queryAgentSettList")
	public String queryAgentSettList(AgentSettleInfo agentSettleInfo, Model model,HttpServletRequest request) throws Exception {
		SysUsers users=this.getCurrentUser(request);
		List<AgentSettleInfo> agentSettList=null;
		if(users.getNotype().intValue()==1){
			agentSettleInfo.setAgentcode(users.getUserno());
		}
		agentSettList=agentSettleInfoService.findByPageAgentSettList(agentSettleInfo);
		model.addAttribute("agentSettList", agentSettList);
		model.addAttribute("agentSettleInfoPage", agentSettleInfo);
		return "jsp/accountMer/agentSettleInfoList";
	}
	/**
	 * 运营商分润汇总信息详情
	 * @param agentSettleInfo
	 * @param model
	 * @param request
	 * @return
	 */
	@RequiresPermissions("account:reportForms:view")
	@RequestMapping(value = "/queryAgentSettView")
	public String queryAgentSettleView(Integer id,Model model,HttpServletRequest request){
		AgentSettleInfo agentSettleInfo=agentSettleInfoService.selectByPrimaryKey(id);
		model.addAttribute("agentSettleInfo", agentSettleInfo);
		return "jsp/accountMer/agentSettleDetail";
	}
	/**
	 * E豆账户张表信息
	 * @param edouTotalAcctInfo
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/queryEdouTotalAcctInfoList")
	public String queryEdouTotalAcctInfoList(EdouTotalAcctInfo edouTotalAcctInfo, Model model,HttpServletRequest request) throws Exception {
		List<EdouTotalAcctInfo> edouTotalAcctInfoList=null;
		edouTotalAcctInfoList=edouTotalAcctInfoService.findByPageEdouTotalAcctInfoList(edouTotalAcctInfo);
		model.addAttribute("edouTotalAcctInfoList", edouTotalAcctInfoList);
		model.addAttribute("edouTotalAcctInfoPage", edouTotalAcctInfo);
		return "jsp/accountMer/edouTotalAcctInfoList";
	}
	/**
	 * E豆账户详情
	 * @param id
	 * @param model
	 * @param request
	 * @return
	 */
	@RequiresPermissions("account:edouAcct:view")
	@RequestMapping(value = "/queryEdouTotalAcctInfoView")
	public String queryEdouTotalAcctInfoView(Integer id,Model model,HttpServletRequest request){
		EdouTotalAcctInfo edouTotalAcctInfo=edouTotalAcctInfoService.selectByPrimaryKey(id);
		model.addAttribute("edouTotalAcctInfo", edouTotalAcctInfo);
		return "jsp/accountMer/edouTotalAcctDetail";
	}
	/**
	 * 商户e豆清算
	 * @param merchantEdouSettleInfo
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/queryMerchantEdouSettleList")
	public String queryMerchantEdouSettleList(MerchantEdouSettleInfo merchantEdouSettleInfo, Model model,HttpServletRequest request) throws Exception {
		List<MerchantEdouSettleInfo> merchantEdouSettleInfoList=null;
		merchantEdouSettleInfoList=merchantEdouSettleInfoService.findByPageMerchantEdouSettleList(merchantEdouSettleInfo);
		model.addAttribute("merchantEdouSettleInfoList", merchantEdouSettleInfoList);
		model.addAttribute("merchantEdouSettleInfoPage", merchantEdouSettleInfo);
		return "jsp/accountMer/merchantEdouSettleList";
	}
	/**
	 * 商户e豆清算
	 * @param id
	 * @param model
	 * @param request
	 * @return
	 */
	@RequiresPermissions("account:merchantEdouSett:view")
	@RequestMapping(value = "/queryMerchantEdouSettleView")
	public String queryMerchantEdouSettleView(Integer id,Model model,HttpServletRequest request){
		MerchantEdouSettleInfo merchantEdouSettleInfo=merchantEdouSettleInfoService.selectByPrimaryKey(id);
		model.addAttribute("merchantEdouSettleInfo", merchantEdouSettleInfo);
		return "jsp/accountMer/merchantEdouSettleDetail";
	}
	/**
	 * 运营商分润清算
	 * @param agentProfitSettleInfo
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/queryAgentProfitSettleList")
	public String queryAgentProfitSettleList(AgentProfitSettleInfo agentProfitSettleInfo, Model model,HttpServletRequest request) throws Exception {
		List<AgentProfitSettleInfo> agentProfitSettleInfoList=null;
		agentProfitSettleInfoList=agentProfitSettleInfoService.findByPageAgentProfitSettleList(agentProfitSettleInfo);
		model.addAttribute("agentProfitSettleInfoList", agentProfitSettleInfoList);
		model.addAttribute("agentProfitSettleInfoPage", agentProfitSettleInfo);
		return "jsp/accountMer/agentProfitSettleList";
	}
	/**
	 * 运营商分润清算详情
	 * @param id
	 * @param model
	 * @param request
	 * @return
	 */
	@RequiresPermissions("account:AgentProfitSettle:view")
	@RequestMapping(value = "/queryAgentProfitSettleView")
	public String queryAgentProfitSettleView(Integer id,Model model,HttpServletRequest request){
		AgentProfitSettleInfo agentProfitSettleInfo=agentProfitSettleInfoService.selectByPrimaryKey(id);
		model.addAttribute("agentProfitSettleInfo", agentProfitSettleInfo);
		return "jsp/accountMer/agentProfitSettleDetail";
	}
	/**
	 * 运营商分润清算
	 * @param agentProfitSettleInfo
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/queryTransProfitTotalList")
	public String queryTransProfitTotalList(TransProfitTotalInfo transProfitTotalInfo, Model model,HttpServletRequest request) throws Exception {
		if(transProfitTotalInfo.getBeginTimeStr()!=null){
			transProfitTotalInfo.setBeginTimeStr(transProfitTotalInfo.getBeginTimeStr().replaceAll("-",""));
		}
		if(transProfitTotalInfo.getEndTimeStr()!=null){
			transProfitTotalInfo.setEndTimeStr(transProfitTotalInfo.getEndTimeStr().replaceAll("-",""));
		}
		List<TransProfitTotalInfo> transProfitTotalInfoList=null;
		transProfitTotalInfoList=transProfitTotalInfoService.findByPageProfitTotalList(transProfitTotalInfo);
		model.addAttribute("agentProfitSettleInfoList", transProfitTotalInfoList);
		model.addAttribute("transProfitTotalInfoPage", transProfitTotalInfo);
		return "jsp/accountMer/transProfitTotalList";
	}
	/**
	 * 运营商分润清算详情
	 * @param id
	 * @param model
	 * @param request
	 * @return
	 */
	@RequiresPermissions("account:transProfitTotal:view")
	@RequestMapping(value = "/queryTransProfitTotalView")
	public String queryTransProfitTotalView(Integer id,Model model,HttpServletRequest request){
		TransProfitTotalInfo transProfitTotalInfo=transProfitTotalInfoService.selectByPrimaryKey(id);
		model.addAttribute("transProfitTotalInfo", transProfitTotalInfo);
		return "jsp/accountMer/transProfitTotalDetail";
	}
	/**
	 * 第三方支付汇总
	 * @param thirdpayTotalInfo
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/queryThirdpayTotalList")
	public String queryThirdpayTotalList(ThirdpayTotalInfo thirdpayTotalInfo, Model model,HttpServletRequest request) throws Exception {
		List<ThirdpayTotalInfo> thirdpayTotalInfoList=null;
		if(thirdpayTotalInfo.getBeginTimeStr()!=null){
			thirdpayTotalInfo.setBeginTimeStr(thirdpayTotalInfo.getBeginTimeStr().replaceAll("-", ""));
		}
		if(thirdpayTotalInfo.getEndTimeStr()!=null){
			thirdpayTotalInfo.setEndTimeStr(thirdpayTotalInfo.getEndTimeStr().replaceAll("-", ""));
		}
		thirdpayTotalInfoList=thirdpayTotalInfoService.findByPageThirdpayTotalList(thirdpayTotalInfo);
		model.addAttribute("thirdpayTotalInfoList", thirdpayTotalInfoList);
		model.addAttribute("thirdpayTotalInfoPage", thirdpayTotalInfo);
		return "jsp/accountMer/thirdpayTotalList";
	}
	/**
	 * 第三方明细
	 * @param transLogInfo
	 * @param model
	 * @param request 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/queryIntTxnDtTypeList")
	public String queryIntTxnDtTypeList(TransLogInfo transLogInfo, Model model,HttpServletRequest request) throws Exception {
		model.addAttribute("IntTxnDt", transLogInfo.getIntTxnDt());
		model.addAttribute("tradeType", transLogInfo.getTradeType());
		List<TransLogInfo> TransLogInfoList=null;
		String IntTxnDt=DateUtils.getFormatDate(transLogInfo.getIntTxnDt());
		Date date=DateUtils.getNextDay(DateUtils.getStringToDate(IntTxnDt,"yyy-mm-dd"));
		transLogInfo.setIntTxnDt(DateUtils.getDateToString(date, "yyymmdd"));
		transLogInfo.setTransStat(new Short("2"));
		TransLogInfoList=transLogInfoService.findByPageIntTxnDt(transLogInfo);
		model.addAttribute("TransLogInfoList", TransLogInfoList);
		model.addAttribute("transLogInfo", transLogInfo);
		return "jsp/accountMer/TransLongZFBList";
	}
	/**
	 * 第三方支付汇总详情
	 * @param id
	 * @param model
	 * @param request
	 * @return
	 */
	@RequiresPermissions("account:thirdpayTotal:view")
	@RequestMapping(value = "/queryThirdpayTotalView")
	public String queryThirdpayTotalView(Integer id,Model model,HttpServletRequest request){
		ThirdpayTotalInfo thirdpayTotalInfo=thirdpayTotalInfoService.selectByPrimaryKey(id);
		model.addAttribute("thirdpayTotalInfo", thirdpayTotalInfo);
		return "jsp/accountMer/thirdpayTotalDetail";
	}
	/**
	 * 运营商分润汇总表导出
	 * @param agentSettleInfoList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("account:reportForms:download")
	@RequestMapping(value = "/agentSettleEXLDownLoad", method = RequestMethod.GET)
	public String agentSettleEXLDownLoad(AgentSettleInfo agentSettleInfoList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("运营商分润汇总表").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			agentSettleExportExcel(agentSettleInfoList, null, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 运营商分润汇总表EXL导出
	 * @param agentSettleInfoList
	 * @param titles
	 * @param outputStream
	 * @param request
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void  agentSettleExportExcel(AgentSettleInfo agentSettleInfoList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/运营商分润汇总表.xlsx";
		agentSettleInfoList.setPage(new Page(false));
		List<AgentSettleInfo> agentSettleInfoLists= agentSettleInfoService.findByPageAgentSettList(agentSettleInfoList);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (AgentSettleInfo info : agentSettleInfoLists) {
			XSSFRow bodyRow = sheet.createRow(j + 3);
			
			//序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			//运营商id
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentcode()));
			//汇总日期
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getSettledt()==null?"":DateUtils.dateToStr(info.getSettledt(),"yyyy-MM-dd"));
			// 非会员消费笔数
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTranstotalcount()));
			//非会员消费金额
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTranstotalamt()));
			//非会员消费分润总额
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransprofit()));
			//会员消费笔数
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMbtranstotalcount()));
			//会员消费金额
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMbtranstotalamt()));
			//会员消费分润
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMbtransprofit()));
			//平台运营商推荐分润总额
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getRecmdtotalprofit()));
			//平台业务员分润总额
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBmtotalprofit()));
			//平台收银员分润总额
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCashiermanprofit()));
			//分润总额
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBmtotalprofit().add(info.getCashiermanprofit())));
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
	 * E豆总账表导出
	 * @param edouTotalAcctInfoList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("account:edouAcct:download")
	@RequestMapping(value = "/edouTotalAcctEXLDownLoad", method = RequestMethod.GET)
	public String edouTotalAcctEXLDownLoad(EdouTotalAcctInfo edouTotalAcctInfoList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("E豆总账表").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			edouTotalAcctExportExcel(edouTotalAcctInfoList, null, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * E豆总账表EXL导出
	 * @param edouTotalAcctInfoList
	 * @param titles
	 * @param outputStream
	 * @param request
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void  edouTotalAcctExportExcel(EdouTotalAcctInfo edouTotalAcctInfoList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/e豆总账报表.xlsx";
		edouTotalAcctInfoList.setPage(new Page(false));
		List<EdouTotalAcctInfo> edouTotalAcctInfoLists= edouTotalAcctInfoService.findByPageEdouTotalAcctInfoList(edouTotalAcctInfoList);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (EdouTotalAcctInfo info : edouTotalAcctInfoLists) {
			XSSFRow bodyRow = sheet.createRow(j + 3);
			//序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			//汇总日期
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getTotaldt()==null?"":DateUtils.dateToStr(info.getTotaldt(),"yyyy-MM-dd"));
			//e豆前日总额
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getLastdayacctbal()));
			// 当日入账e豆总额
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTodayinacctbal()));
			//当日出账积分总额
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTodayoutacctbal()));
			//e豆当日余额
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTodayinacctbal()));
			//冻结e豆总额
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getFreeztotalacctbal()));
			//积分前日总额
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getLastdayscorebal()));
			//当日入账积分总额
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTodayinscorebal()));
			//当日出账积分总额
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTodayoutscorebal()));
			//积分当日余额
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTodayscorebal()));
			//平 
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			if("0".equals(info.getIsBalance()))
				cell.setCellValue("平");
			if(!"0".equals(info.getIsBalance()))
				cell.setCellValue("不平");
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
	 * 商户e豆清算报表EXL
	 * @param merchantEdouSettleList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("account:merchantEdouSett:download")
	@RequestMapping(value = "/merchantEdouSettleEXLDownLoad", method = RequestMethod.GET)
	public String merchantEdouSettleEXLDownLoad(MerchantEdouSettleInfo merchantEdouSettleList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("商户e豆清算报表").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			merchantEdouSettleExportExcel(merchantEdouSettleList, null, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 商户e豆清算报表
	 * @param merchantEdouSettleList
	 * @param titles
	 * @param outputStream
	 * @param request
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void  merchantEdouSettleExportExcel(MerchantEdouSettleInfo merchantEdouSettleList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/商户e豆清算报表.xlsx";
		merchantEdouSettleList.setPage(new Page(false));
		List<MerchantEdouSettleInfo> merchantEdouSettleLists= merchantEdouSettleInfoService.findByPageMerchantEdouSettleList(merchantEdouSettleList);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (MerchantEdouSettleInfo info : merchantEdouSettleLists) {
			XSSFRow bodyRow = sheet.createRow(j + 2);
			//序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			//付款帐号
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str("01600120210017448"));
			//付款帐号名称
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str("南京迈控电子商务有限公司"));
			// 收款帐号
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctNo()));
			//收款帐号名称
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctName()));
			//收款帐号开户行
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctBank()));
			//转账类型(行内 1/行外 2)
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str("行内"));
			//收报行联行号
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBankCode()));
			//e豆清算金额
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getEdousettleamt()));
			//汇款用途
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str("e豆清算"));
			//清算日期
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getSettledt()==null?"":DateUtils.dateToStr(info.getSettledt(),"yyyy-MM-dd"));
			//商户号
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerchantno()));
			//e豆账户余额
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getEdouacctbal()));
			//手续费
			cell = bodyRow.createCell(13);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getFee()));
			//商户名称
			cell = bodyRow.createCell(14);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerName()));
			//清算状态码0 - 处理中1 - 失败 2 - 成功
			cell = bodyRow.createCell(15);
			cell.setCellStyle(bodyStyle);
			if(info.getSettlestat().shortValue()==0)
				cell.setCellValue("未审核");
			else if(info.getSettlestat().shortValue()==1)
				cell.setCellValue("审核未通过");
			else if(info.getSettlestat().shortValue()==2)
				cell.setCellValue("审核通过");
			//交易标志
			cell = bodyRow.createCell(16);
			cell.setCellStyle(bodyStyle);
			if(info.getWxOpenId().equals("1"))
				cell.setCellValue("银行卡");
			else if(info.getWxOpenId().equals("2"))
				cell.setCellValue("企业付款 ");
			
			cell = bodyRow.createCell(17);
			cell.setCellStyle(bodyStyle);
			if(info.getPayState()==0)
				cell.setCellValue("未打款");
			else if(info.getPayState()==1)
				cell.setCellValue("已打款");
			
			//清算日期
			cell = bodyRow.createCell(18);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getPayTime()==null?"":DateUtils.dateToStr(info.getPayTime(),"yyyy-MM-dd hh:MM:ss"));
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
	 * 运营商分润清算表EXL
	 * @param agentProfitSettleList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("account:AgentProfitSettle:download")
	@RequestMapping(value = "/agentProfitSettleEXLDownLoad", method = RequestMethod.GET)
	public String agentProfitSettleEXLDownLoad(AgentProfitSettleInfo agentProfitSettleList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("运营商分润清算报表").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			agentProfitSettleExportExcel(agentProfitSettleList, null, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 运营商分润清算表EXL
	 * @param agentProfitSettleList
	 * @param titles
	 * @param outputStream
	 * @param request
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void  agentProfitSettleExportExcel(AgentProfitSettleInfo agentProfitSettleList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/运营商分润清算报表.xlsx";
		agentProfitSettleList.setPage(new Page(false));
		List<AgentProfitSettleInfo> agentProfitSettleLists= agentProfitSettleInfoService.findByPageAgentProfitSettleList(agentProfitSettleList);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (AgentProfitSettleInfo info : agentProfitSettleLists) {
			XSSFRow bodyRow = sheet.createRow(j + 2);
			//序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			//付款帐号
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str("01600120210017448"));
			//付款帐号名称
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str("南京迈控电子商务有限公司"));
			// 收款帐号
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctNo()));
			//收款帐号名称
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctName()));
			//收款帐号开户行
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctBank()));
			//转账类型(行内 1/行外 2)
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str("行内"));
			//收报行联行号
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBankCode()));
			//清算分润金额
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getSettleprofitbal()));
			//汇款用途
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(""));
			//清算日期
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getSettletime()==null?"":DateUtils.dateToStr(info.getSettletime(),"yyyy-MM-dd"));
			//清算月份
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getSettledt()));
			//城市服务商编号
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentno()));
			//手续费
			cell = bodyRow.createCell(13);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(""));
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
	 * 第三方支付汇总EXL
	 * @param thirdpayTotalList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("account:thirdpayTotal:download")
	@RequestMapping(value = "/thirdpayTotalEXLDownLoad", method = RequestMethod.GET)
	public String thirdpayTotalEXLDownLoad(ThirdpayTotalInfo thirdpayTotalList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("第三方支付汇总表").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			thirdpayTotalExportExcel(thirdpayTotalList, null, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 第三方支付汇总EXL
	 * @param thirdpayTotalList
	 * @param titles
	 * @param outputStream
	 * @param request
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void  thirdpayTotalExportExcel(ThirdpayTotalInfo thirdpayTotalList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/第三方支付汇总表.xlsx";
		thirdpayTotalList.setPage(new Page(false));
		List<ThirdpayTotalInfo> thirdpayTotalLists= thirdpayTotalInfoService.findByPageThirdpayTotalList(thirdpayTotalList);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (ThirdpayTotalInfo info : thirdpayTotalLists) {
			XSSFRow bodyRow = sheet.createRow(j + 3);
			//序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			//汇总日期
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotaldt()));
			//支付宝 消费总笔数
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAlipaytranscount()));
			//支付宝 消费总金额
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAlipaytransamt()));
			//支付宝 分润总额
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAlipayprofitamt()));
			//微信 消费总笔数
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getWeixintranscount()));
			//微信 消费总金额
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getWeixintransamt()));
			//微信 分润总额
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getWeixinprofitamt()));
			//非会员银行卡 消费总笔数
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getUnstranscount()));
			//非会员银行卡消费总金额
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getUnstransamt()));
			//非会员银行卡 分润总额
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getUnsprofitamt()));
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
	 * 原始交易分润汇总表EXL
	 * @param transProfitTotalList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("account:transProfitTotal:download")
	@RequestMapping(value = "/transProfitTotalEXLDownLoad", method = RequestMethod.GET)
	public String transProfitTotalEXLDownLoad(TransProfitTotalInfo transProfitTotalList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("会员消费原始分账报表").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			 transProfitTotalExportExcel(transProfitTotalList, null, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 原始交易分润汇总表EXL
	 * @param transProfitTotalList
	 * @param titles
	 * @param outputStream
	 * @param request
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void  transProfitTotalExportExcel(TransProfitTotalInfo transProfitTotalList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/会员消费原始分账报表.xlsx";
		transProfitTotalList.setPage(new Page(false));
		transProfitTotalList.setBeginTimeStr(transProfitTotalList.getBeginTimeStr().replace("-", ""));
		transProfitTotalList.setEndTimeStr(transProfitTotalList.getEndTimeStr().replace("-", ""));
		List<TransProfitTotalInfo> transProfitTotalLists= transProfitTotalInfoService.findByPageProfitTotalList(transProfitTotalList);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (TransProfitTotalInfo info : transProfitTotalLists) {
			XSSFRow bodyRow = sheet.createRow(j + 2);
			//序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			//清算日期
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotaldate()));
			//消费总笔数
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotaltranscount()));
			//消费总金额
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotaltransamount()));
			//刷卡手续费
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalfeeamount()));
			//商户清算金额
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalsettleamount()));
			//可分配总额
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalprofitamt()));
			//积分成本总额
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalscoreprofit()));
			//e豆返利总额
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalmemberprofit()));
			//运营商分润总额
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalagentprofit()));
			//收银员分润总额
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalcashierprofit()));
			//业务员分润总额
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalbusinesserprofit()));
			//运营商推荐分润
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalagentrecmdprofit()));
			//商户推荐分润
			cell = bodyRow.createCell(13);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalmerrecmdprofit()));
			//会员推荐分润
			cell = bodyRow.createCell(14);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotoalmbrecmdprofit()));
			//优惠分享分润
			cell = bodyRow.createCell(15);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalfavourshareprofit()));
			//平台收益  
			cell = bodyRow.createCell(16);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalplatprofit()));
			cell = bodyRow.createCell(17);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotoalbackupprofit()));
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
	 * 运营商分润汇总明细
	 * @param transLogInfo
	 * @param model
	 * @param request
	 * @return 
	 */
	@RequestMapping("/getMingXiList")
	public String getMingXiList(TransLogInfo transLogInfo,Date settleDt,Model model,HttpServletRequest request){
		//交易日期 运营商编号 类型
		String AgentNo=transLogInfo.getAgentNo();
		String TradeType=transLogInfo.getTradeType();
		model.addAttribute("settleDt",settleDt);
		model.addAttribute("AgentNo", AgentNo);
		model.addAttribute("TradeType", TradeType);
		transLogInfo.setAgentNo(AgentNo);
		Date dateNext=DateUtils.getNextDay(settleDt);
		transLogInfo.setIntTxnDt(DateUtils.getDateToString(dateNext,"yyyyMMdd"));
		List<TransLogInfo> transLogInfoList=null;
		transLogInfoList=transLogInfoService.findByPageProfitInfo(transLogInfo);
		model.addAttribute("transLogInfoList", transLogInfoList);
		model.addAttribute("transLogInfoPage", transLogInfo);
		model.addAttribute("agentSttlMingXiPage", transLogInfo);
		return "jsp/accountMer/agentSettlMingXi";
	}
	/**
	 * e都账户汇总明细
	 * @param merAcctTransLog
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/getMingXiEDouList")
	public String getMingXiEDouList(MemberAcctTransLog memberAcctTransLog,Model model,HttpServletRequest request){
		model.addAttribute("IntTxnDt",memberAcctTransLog.getIntTxnDt());
		memberAcctTransLog.setTransStat(new Short("2"));
		Date date=DateUtils.getNextDay(DateUtils.getStringToDate(memberAcctTransLog.getIntTxnDt(), "yyyy-mm-dd"));
		memberAcctTransLog.setIntTxnDt(DateUtils.getDateToString(date, "yyyymmdd"));
		List<MemberAcctTransLog> memberAcctTransLogList=null;
		memberAcctTransLogList=memberAcctTransLogService.findByPageMemberAcctTransLogList(memberAcctTransLog);
		model.addAttribute("merAcctTransLogList", memberAcctTransLogList);
		model.addAttribute("merAcctTransLogPage", memberAcctTransLog);
		return "jsp/accountMer/edouMingXiList";
		
	}
	/**
	 * 商户e豆明细
	 * @param merAcctTransLog
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/getMingXiMerEDouList")
	public String getMingXiMerEDouList(MerchantSettleInfo merchantSettleInfo,String SettleDt,Model model,HttpServletRequest request){
		model.addAttribute("SettleDt", merchantSettleInfo.getSettleDt());
		model.addAttribute("MerNo", merchantSettleInfo.getMerNo());
		String dateString=DateUtils.getDateToString(merchantSettleInfo.getSettleDt(),"yyyy-MM-dd");
		String Times=dateString;
		//前一个月的第一天和最后一天的日期
		String BeginDate=merchantSettleInfoService.getMonthBegin(Times);
		String EndDate=merchantSettleInfoService.getMonthEnd(Times);
		merchantSettleInfo.setBeginTimeStr(BeginDate);
		merchantSettleInfo.setEndTimeStr(EndDate);
		merchantSettleInfo.setSettleDt(null);
		List<MerchantSettleInfo> merchantSettleInfoList=null;
		merchantSettleInfoList=merchantSettleInfoService.findByPageMerchantSettleInfos(merchantSettleInfo);
		model.addAttribute("merchantSettleInfoList", merchantSettleInfoList);
		model.addAttribute("merchantSettleInfoPage", merchantSettleInfo);
		return "jsp/accountMer/merEdouMingXiList";
		
	}
	/**
	 * 原始交易分润明细
	 * @param transLogInfo
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/getMingXiTransProfit")
	public String getMingXiTransProfit(TransLogInfo transLogInfo,Model model,HttpServletRequest request){
		model.addAttribute("IntTxnDt", transLogInfo.getIntTxnDt());
		Date date=DateUtils.getStringToDate(DateUtils.getFormatDate(transLogInfo.getIntTxnDt()),"yyyy-MM-dd");
		Date DateNext=DateUtils.getNextDay(date);
		String Stringdate=DateUtils.getDateToString(DateNext, "yyyyMMdd");
		transLogInfo.setIntTxnDt(Stringdate);
		List<TransLogInfo> transLogInfoList=null;
		transLogInfoList=transLogInfoService.findByPageRightProfitInfo(transLogInfo);
		model.addAttribute("transLogInfoList", transLogInfoList);
		model.addAttribute("transLogInfoPage", transLogInfo);
		return "jsp/accountMer/transProfitMingXiList";
		
	}
	/**
	 * 清算e豆跟新
	 * @param id
	 * @param model
	 * @return
	 */
	@RequiresPermissions("account:merchantEdouSett:qingsuan")
	@RequestMapping(value = "/LiquidationEDou")
	@ResponseBody
	public void LiquidationEDou(MerchantEdouSettleInfo merchantEdouSettleInfo, Model model,HttpServletResponse response) {
		ResultResp resp=null;
		try{
			//查询商户账户流水信息
			int res=merchantEdouSettleInfoService.updateMerchantEdouSettle(merchantEdouSettleInfo);
			if(res==1)
				resp = ResultResp.getInstance(ResultCode.success);
			 else 
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}catch(BaseException e){
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			resp = ResultResp.getInstance(false,e.getMessage());
		}
		this.writeJson(resp, response);
	}
	/**
	 * 清算e豆更新并付款
	 * @param id
	 * @param model
	 * @return
	 */
	@RequiresPermissions("account:merchantEdouSett:pay")
	@RequestMapping(value = "/LiquidationEDouPay")
	@ResponseBody
	public void LiquidationEDouPay(MerchantEdouSettleInfo merchantEdouSettleInfo, Model model,HttpServletResponse response) {
		ResultResp resp=null;
		try{
			//查询商户账户流水信息
			int res=merchantEdouSettleInfoService.updateMerchantEdouSettlePay(merchantEdouSettleInfo);
			if(res==1)
				resp = ResultResp.getInstance(ResultCode.success);
			 else 
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}catch(BaseException e){
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			resp = ResultResp.getInstance(false,e.getMessage());
		}
		this.writeJson(resp, response);
	}
	/**							
	 * 运营商分润清算 更新
	 * @param id
	 * @param model
	 * @return
	 */
	@RequiresPermissions("account:AgentProfitSettle:qingsuan")
	@RequestMapping(value = "/LiquidationAgentProfit")
	@ResponseBody
	public void LiquidationAgentProfit(Integer[] id, Model model) {
		if (id != null && id.length > 0) {
			// 循环更新
			for (int i = 0; i < id.length; i++) {
				agentProfitSettleInfoService.updateById(id[i]);
			}
		}
	}
	@RequestMapping("/merAccountList")
	public String merAccountList(MerchantSettleInfo merchantSettleInfo,Model model,HttpServletRequest request) {
		model.addAttribute("SettleDt",merchantSettleInfo.getSettleDt());
		model.addAttribute("AgentCode",merchantSettleInfo.getAgentCode());
		Date dateNext =DateUtils.getNextDay(merchantSettleInfo.getSettleDt());
		merchantSettleInfo.setBeginTimeStr(DateUtils.getDateToString(dateNext, "yyyy-MM-dd"));
		merchantSettleInfo.setEndTimeStr(DateUtils.getDateToString(dateNext, "yyyy-MM-dd"));
		merchantSettleInfo.setSettleDt(null);
		//根据当前商户号查询所对应的list信息
		ResultResp resp = null;
		List<MerchantSettleInfo> merchantSettleInfos=null;
		try{
			merchantSettleInfos= merchantSettleInfoService.findByPageMerchantSettleInfos(merchantSettleInfo);
			resp = ResultResp.getInstance(ResultCode.success);
		}catch (BaseException e) {
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch (Exception e) {
			log.error("merchantSettleInfos error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		model.addAttribute("merchantSettleInfos", merchantSettleInfos);
		model.addAttribute("merchantSettleInfo", merchantSettleInfo);
		model.addAttribute("result", resp);
		return "jsp/accountMer/MerchangtMingXi";
	}
	/**
	 * 会员交易分润汇总明细报表
	 * @param transLogInfoList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/transProfitMingXiEXLDownLoad", method = RequestMethod.GET)
	public String transProfitMingXiEXLDownLoad(TransLogInfo transLogInfoList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("会员交易分润汇总明细报表").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			transProfitMingXiExportExcel(transLogInfoList, null, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 会员交易分润汇总明细报表
	 * @param transLogInfoList
	 * @param titles
	 * @param outputStream
	 * @param request
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void  transProfitMingXiExportExcel(TransLogInfo transLogInfoList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/会员交易分润汇总明细报表.xls";
		transLogInfoList.setPage(new Page(false));
		Date date=DateUtils.getStringToDate(DateUtils.getFormatDate(transLogInfoList.getIntTxnDt()),"yyyy-MM-dd");
		Date DateNext=DateUtils.getNextDay(date);
		String Stringdate=DateUtils.getDateToString(DateNext, "yyyyMMdd");
		transLogInfoList.setIntTxnDt(Stringdate);
		List<TransLogInfo> transLogInfoLists= transLogInfoService.findByPageRightProfitInfo(transLogInfoList);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (TransLogInfo info : transLogInfoLists) {
			XSSFRow bodyRow = sheet.createRow(j + 2);
			//序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			//运营商编号
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentNo()));
			//商户号
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerchantNo()));
			//终端号
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTerminalNo()));
			//交易金额
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransAmount()));
			//商户结算金额
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getSettleAmount()));
			//刷卡手续费
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getFeeAmount()));
			//分润金额
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalProfitAmt()));
			//运营商分润
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentProfit()));
			//平台分润
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getPlatProfit()));
			//推广分润
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getPromoteProfit()));
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
	 * 会员交易分润汇总明细报表
	 * @param transLogInfoList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/transMerchangtMingXiEXLDownLoad", method = RequestMethod.GET)
	public String transMerchangtMingXiEXLDownLoad(TransLogInfo transLogInfoList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("商户账户汇总明细报表").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			transMerMingXiExportExcel(transLogInfoList, null, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 会员交易分润汇总明细报表
	 * @param transLogInfoList
	 * @param titles
	 * @param outputStream
	 * @param request
	 * @throws FileNotFoundException
	 * @throws IOException replyCd transStat
	 */
	public void  transMerMingXiExportExcel(TransLogInfo transLogInfoList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/商户账户汇总明细报表.xls";
		transLogInfoList.setPage(new Page(false));
		transLogInfoList.setReplyCd("0000");
		transLogInfoList.setTransStat(new Short("2"));
		transLogInfoList.setMerchantNo(transLogInfoList.getMerchantNo());
		Date dateNext=DateUtils.getNextDay(DateUtils.getStringToDate(transLogInfoList.getIntTxnDt(),"yyyy-MM-dd"));
		transLogInfoList.setIntTxnDt(DateUtils.getDateToString(dateNext,"yyyyMMdd"));
		List<TransLogInfo> transLogInfoLists=null;
		transLogInfoLists=transLogInfoService.getPageForTrans(transLogInfoList, request);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (TransLogInfo info : transLogInfoLists) {
			XSSFRow bodyRow = sheet.createRow(j + 2);
			//序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			//交易日期
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getIntTxnDt()));
			//商户号
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getIntTxnTm()));
			//终端号
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerName()));
			//交易金额
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerchantNo()));
			//商户结算金额
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTerminalNo()));
			//刷卡手续费
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTermSeqId()));
			//分润金额
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBankCardNo()));
			//运营商分润
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMemberNo()));
			//平台分润
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransAmount()));
			//推广分润
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(TradeType.getExamType(info.getTradeType())));
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
	 * 运营商分润明细报表导出
	 * @param transLogInfoList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/merchantSettleMingXiEXLDownLoad", method = RequestMethod.GET)
	public String merchantSettleMingXiEXLDownLoad(MerchantSettleInfo merchantSettleInfo,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("运营商分润明细报表").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			merchantSettleMingXiExportExcel(merchantSettleInfo, null, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 会员交易分润汇总明细报表
	 * @param transLogInfoList
	 * @param titles
	 * @param outputStream
	 * @param request
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void  merchantSettleMingXiExportExcel(MerchantSettleInfo merchantSettleInfo,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/运营商分润明细报表.xls";
		merchantSettleInfo.setPage(new Page(false));
		Date dateNext =DateUtils.getNextDay(merchantSettleInfo.getSettleDt());
		merchantSettleInfo.setSettleDt(null);
		merchantSettleInfo.setBeginTimeStr(DateUtils.getDateToString(dateNext, "yyyy-MM-dd"));
		merchantSettleInfo.setEndTimeStr(DateUtils.getDateToString(dateNext, "yyyy-MM-dd"));
		List<MerchantSettleInfo> merchantSettleInfoList= merchantSettleInfoService.findByPageMerchantSettleInfos(merchantSettleInfo);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (MerchantSettleInfo info : merchantSettleInfoList) {
			XSSFRow bodyRow = sheet.createRow(j + 2);
			//序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			//交易日期
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerNo())+"-"+MyStringUtil.obj2Str(info.getMerName()));
			//商户号
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getSettleDt()==null?"":DateUtils.dateToStr(info.getSettleDt(),"yyyy-MM-dd"));
			//终端号
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransTotalCount()));
			//交易金额
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransTotalAmt()));
			//商户结算金额
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransTotalFee()));
			//刷卡手续费
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMbTransTotalCount()));
			//分润金额
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMbTransTotalAmt()));
			//运营商分润
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMbTransTotalFee()));
			//平台分润
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getEdouTotalCount()));
			//推广分润
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getEdouTotalAmt()));
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
	 * 运营商消费分润明细导出
	 * @param transLogInfoList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/transListMingXiEXLDownLoad", method = RequestMethod.GET)
	public String transListMingXiEXLDownLoad(TransLogInfo transLogInfo,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("运营商消费分润明细报表").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			transListMingXiExportExcel(transLogInfo, null, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 运营商消费分润明细EXL导出
	 * @param transLogInfoList
	 * @param titles
	 * @param outputStream
	 * @param request
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void  transListMingXiExportExcel(TransLogInfo transLogInfo,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/运营商消费分润明细报表.xls";
		transLogInfo.setPage(new Page(false));
		String AgentNo=transLogInfo.getAgentNo();
		transLogInfo.setAgentNo(AgentNo);
		Date dateNext=DateUtils.getNextDay(transLogInfo.getSettleDt());
		transLogInfo.setIntTxnDt(DateUtils.getDateToString(dateNext,"yyyyMMdd"));
		List<TransLogInfo> transLogInfoList=null;
		transLogInfoList=transLogInfoService.findByPageProfitInfo(transLogInfo);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (TransLogInfo info : transLogInfoList) {
			XSSFRow bodyRow = sheet.createRow(j + 2);
			//序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			//交易日期
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentNo()));
			//商户号
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransAmount()));
			//终端号
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentProfit()));
			//交易金额
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getPlatProfit()));
			//商户结算金额
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getPromoteProfit()));
			//刷卡手续费
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCashierProfit()));
			//分润金额
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBusinesserProfit()));
			//运营商分润
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentRecmdProfit()));
			//平台分润
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerRecmdProfit()));
			//推广分润
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBackupProfit()));
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMbRecmdProfit()));
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getFavourShareProfit()));
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
	 * 会员交易流水信息
	 * @param memberAcctTransLog
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/memberAccTrainLogList")
	public String memberAccTrainLogList(MemberAcctTransLog memberAcctTransLog,Model model,HttpServletRequest request){
		if(memberAcctTransLog.getKssj()!=null){
			memberAcctTransLog.setKssj(memberAcctTransLog.getKssj().replace("-", ""));
		}
		if(memberAcctTransLog.getJssj()!=null){
			memberAcctTransLog.setJssj(memberAcctTransLog.getJssj().replace("-",""));
		}
		List<MemberAcctTransLog> memberAcctTransLogList=null;
		memberAcctTransLogList=memberAcctTransLogService.findByPageMemberAcctTransLogList(memberAcctTransLog);
		model.addAttribute("merAcctTransLogList", memberAcctTransLogList);
		model.addAttribute("merAcctTransLogPage", memberAcctTransLog);
		return "jsp/accountMer/memberAccTransLogList";
	}
	/**
	 * 会员EXL导出 
	 * @param memberAcctTransLog
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/memberAccTrainLogEXLDownLoad", method = RequestMethod.GET)
	public String memberAccTrainLogEXLDownLoad(MemberAcctTransLog memberAcctTransLog,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("会员交易流水报表").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			memberAccTrainLogExportExcel(memberAcctTransLog, null, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	public void  memberAccTrainLogExportExcel(MemberAcctTransLog memberAcctTransLog,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/会员交易流水报表.xls";
		memberAcctTransLog.setPage(new Page(false));
		if(!memberAcctTransLog.getKssj().equals("")){
			memberAcctTransLog.setKssj(memberAcctTransLog.getKssj().replace("-", ""));
		}
		if(!memberAcctTransLog.getJssj().equals("")){
			memberAcctTransLog.setJssj(memberAcctTransLog.getJssj().replace("-",""));
		}
		List<MemberAcctTransLog> memberAcctTransLogList=null;
		memberAcctTransLogList=memberAcctTransLogService.findByPageMemberAcctTransLogList(memberAcctTransLog);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (MemberAcctTransLog info : memberAcctTransLogList) {
			XSSFRow bodyRow = sheet.createRow(j + 2);
			//序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			//交易流水表id
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransLogId()));
			//会员号
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMemberId()));
			//商户号
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerchantNo()));
			//交易日期
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getIntTxnDt()));
			//交易时间
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getIntTxnTm()));
			//交易类型
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			if(info.getTransType().equals("01")){
				cell.setCellValue(MyStringUtil.obj2Str("e豆返利"));
			}
			if(info.getTransType().equals("02")){
				cell.setCellValue(MyStringUtil.obj2Str("e豆消费"));
			}
			if(info.getTransType().equals("03")){
				cell.setCellValue(MyStringUtil.obj2Str("积分返利"));
			}
			if(info.getTransType().equals("04")){
				cell.setCellValue(MyStringUtil.obj2Str("积分抵扣"));
			}
			if(info.getTransType().equals("05")){
				cell.setCellValue(MyStringUtil.obj2Str("推荐分润"));
			}
			if(info.getTransType().equals("06")){
				cell.setCellValue(MyStringUtil.obj2Str("优惠分享粉润"));
			}
			if(info.getTransType().equals("07")){
				cell.setCellValue(MyStringUtil.obj2Str("活动返利"));
			}
			if(info.getTransType().equals("08")){
				cell.setCellValue(MyStringUtil.obj2Str("推荐注册"));
			}
			
			//交易金额
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransAmount()));
			//e豆账户余额
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getEdouAvailBal()));
			//积分账户余额
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getScoreAvailBal()));
			//
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			if(info.getTransStat().shortValue()==new Short("0")){
				cell.setCellValue(MyStringUtil.obj2Str("处理中"));
			}
			if(info.getTransStat().shortValue()==new Short("1")){
				cell.setCellValue(MyStringUtil.obj2Str("失败"));
			}
			if(info.getTransStat().shortValue()==new Short("2")){
				cell.setCellValue(MyStringUtil.obj2Str("成功"));
			}
			if(info.getTransStat().shortValue()==new Short("4")){
				cell.setCellValue(MyStringUtil.obj2Str("冲正"));
			}
			cell = bodyRow.createCell(11);
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
	 * 商户扫码清算表列表
	 * @param mList
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/scanpaySettleInfoList")
	public String openScanpaySettleInfoList(MerchantScanpaySettleInfo mList, Model model, HttpServletRequest request) throws Exception {
		// 商户扫码清算表列表
		List<MerchantScanpaySettleInfo> scanpaySettleInfoList = merchantScanpaySettleInfoService.findByPageMSSMIList(mList);
		model.addAttribute("scanpaySettleInfoList", scanpaySettleInfoList);
		model.addAttribute("mListPage", mList);
		return "jsp/accountMer/scanpaySettleInfoList";
	}
	/**
	 * 商户扫码清算
	 * @param merchantScanpaySettleInfo
	 * @param request
	 * @param response
	 */
	@RequiresPermissions("account:merchantSSI:update")
	@RequestMapping(value = "/updateScanpaySettleInfo")
	public void updateScanpaySettleInfo(MerchantScanpaySettleInfo merchantScanpaySettleInfo, HttpServletRequest request, HttpServletResponse response) {
		ResultResp resp=null;
		try{
			int res = merchantScanpaySettleInfoService.updateMerchantScanPaySettle(merchantScanpaySettleInfo);
			if(res>0)
				resp = ResultResp.getInstance(ResultCode.success);
			 else 
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("updateScanpaySettleInfo error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
	}
	/**
	 * 商户扫码清算详情
	 */
	@RequiresPermissions("account:merchantSSI:update")
	@RequestMapping(value = "/merchantScanSettleView")
	public String merchantScanSettleView(Integer id,Model model,HttpServletRequest request){
		MerchantScanpaySettleInfo merchantScanpaySettleInfo=merchantScanpaySettleInfoService.selectByPrimaryKey(id);
		model.addAttribute("merchantScanpaySettleInfo", merchantScanpaySettleInfo);
		return "jsp/accountMer/merchantSettleDetail";
	}
	
	/**
	 * 扫码清算更新
	 * @param id
	 * @param model
	 * @return
	 */
	@RequiresPermissions("account:merchantSSI:update")
	@RequestMapping(value = "/merchantScanpaySettle")
	@ResponseBody
	public void merchantScanpaySettle(MerchantScanpaySettleInfo merchantScanpaySettleInfo, Model model,HttpServletResponse response) {
		ResultResp resp=null;
		try{
			int res=merchantScanpaySettleInfoService.updateScanpaySettle(merchantScanpaySettleInfo);
			if(res==1)
				resp = ResultResp.getInstance(ResultCode.success);
			 else 
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}catch(BaseException e){
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			resp = ResultResp.getInstance(false,e.getMessage());
		}
		this.writeJson(resp, response);
	}
	/**
	 * 清算e豆更新并付款
	 * @param id
	 * @param model
	 * @return
	 */
	@RequiresPermissions("account:merchantSSI:pay")
	@RequestMapping(value = "/merchantScanpaySettlePay")
	@ResponseBody
	public void merchantScanpaySettlePay(MerchantScanpaySettleInfo merchantScanpaySettleInfo, Model model,HttpServletResponse response) {
		ResultResp resp=null;
		try{
			int res=merchantScanpaySettleInfoService.updateScanpaySettlePay(merchantScanpaySettleInfo);
			if(res==1)
				resp = ResultResp.getInstance(ResultCode.success);
			 else 
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}catch(BaseException e){
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			resp = ResultResp.getInstance(false,e.getMessage());
		}
		this.writeJson(resp, response);
	}
	/**
	 * EXL导出
	 * @param merchantScanpaySettleInfo
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/scanpaySettleInfoEXLDownLoad", method = RequestMethod.GET)
	public String scanpaySettleInfoEXLDownLoad(MerchantScanpaySettleInfo merchantScanpaySettleInfo,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("商户扫码清算表").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			scanpaySettleInfoEX(merchantScanpaySettleInfo, null, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	public void  scanpaySettleInfoEX(MerchantScanpaySettleInfo merchantScanpaySettleInfo,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/商户扫码清算表.xlsx";
		merchantScanpaySettleInfo.setPage(new Page(false));
		List<MerchantScanpaySettleInfo> mList=null;
		mList=merchantScanpaySettleInfoService.findByPageMSSMIList(merchantScanpaySettleInfo);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (MerchantScanpaySettleInfo info : mList) {
			XSSFRow bodyRow = sheet.createRow(j + 2);
			//序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			//清算日期
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getSettleDt()==null?"":DateUtils.dateToStr(info.getSettleDt(),"yyyy-MM-dd"));
			//商户号-商户名称
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerchantNo()+"-"+info.getMerchantName()));
			//账户名称
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctName()));
			//开户行名称
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctBank()));
			//银联号
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBankCode()));
			//账号
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctNo()));
			//支付宝笔数
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAlipayCount()));
			//支付宝金额
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAlipayTotal()));
			//支付宝手续费
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAlipayFee()));
			//微信笔数
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getWeixinCount()));
			//微信交易金额
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getWeixinTotal()));
			//微信手续费
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getWeixinFee()));
			//清算总金额
			cell = bodyRow.createCell(13);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getSettleTotal()));
			//状态
			cell = bodyRow.createCell(14);
			cell.setCellStyle(bodyStyle);
			if(info.getSettleStat().shortValue()==new Short("0")){
				cell.setCellValue(MyStringUtil.obj2Str("未清算"));
			}
			if(info.getSettleStat().shortValue()==new Short("2")){
				cell.setCellValue(MyStringUtil.obj2Str("已清算"));
			}
			//交易单号
			cell = bodyRow.createCell(15);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getPartnerTradeNo()));
			//打款状态
			cell = bodyRow.createCell(16);
			cell.setCellStyle(bodyStyle);
			if(info.getPayState()==0){
				cell.setCellValue(MyStringUtil.obj2Str("未打款"));
			}
			if(info.getPayState()==1){
				cell.setCellValue(MyStringUtil.obj2Str("已打款"));
			}
			//清算日期
			cell = bodyRow.createCell(17);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getPayTime()==null?"":DateUtils.dateToStr(info.getPayTime(),"yyyy-MM-dd hh:MM:ss"));
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
