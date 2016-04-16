package com.mk.pro.manage.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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

import com.mk.pro.commons.enums.MerAcctTransType;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.enums.UserType;
import com.mk.pro.commons.exception.ServiceException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.MerAcctTransLog;
import com.mk.pro.model.MerchantEdouSettleInfo;
import com.mk.pro.model.MerchantInfo;
import com.mk.pro.model.Page;
import com.mk.pro.model.TransLogInfo;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.MerAcctTransService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.ExportUtil;
import com.mk.pro.utils.MyStringUtil;

/**
 * 商户交易历史记录
 * @author syd
 * 2015-6-29
 */
@Controller
@RequestMapping("/meraccttrans")
public class MerAcctTransController extends BaseController<MerAcctTransLog, String> {
	@Resource(name="merAcctTransService")
	private MerAcctTransService merAcctTransService;

	/**
	 * 根据商户id查询营销账户
	 */
	@RequestMapping("/queryMerAcctTrans")
	public String queryMerAcctTrans(MerAcctTransLog merAcctTrans,Model model,HttpServletRequest request){
		SysUsers user = this.getCurrentUser(request);
		//判断是否是商户  1.运营商2商户0管理员
		if(2 == user.getNotype().intValue() || 0 == user.getNotype().intValue()){
			List<MerAcctTransLog> list = merAcctTransService.queryMerAcctTrans(merAcctTrans,request); 
			model.addAttribute("list", list);
//			//查询汇总笔数 查询汇总金额
//			Map<String, Object> merAccTransLogAmount=merAcctTransService.getMerAccTransLogAmount(merAcctTrans);
			//总笔数
			model.addAttribute("allAmount", merAcctTrans.getPage().getTotalAmount());
			//总金额
			model.addAttribute("allAmountCount",merAcctTrans.getPage().getTotalResult());
		}else{
			Json json = new Json();
			json.setMsg("您不是商户！");
			model.addAttribute("json", json);
		}
		model.addAttribute("merAcctTransPage", merAcctTrans);
		return "jsp/meraccttrans/list";
	}
	
	@RequiresPermissions("trans:cashaudit:update")
	@RequestMapping(value="selectOneTrans")
	public String getSelectOneTrans(Integer maId,MerAcctTransLog merAcctTrans,Model model,HttpServletRequest request)throws Exception{
		if(merAcctTrans.getMaId()!=null){
			SysUsers user = this.getCurrentUser(request);
			//判断是否是商户  1.运营商2商户0管理员
			if(2 == user.getNotype().intValue() || 0 == user.getNotype().intValue()){
				if(user.getNotype().intValue()==UserType.merchant.getValue()){
					merAcctTrans.setMerchantNo(user.getUserno());
				}
				MerAcctTransLog transOne=merAcctTransService.selectByPrimaryKey(maId);
				model.addAttribute("transOne", transOne);
			}else{
				Json json = new Json();
				json.setMsg("您不是商户！");
				model.addAttribute("json", json);
			}
		}else{
			throw new IllegalStateException("maId is null");
		}
		 return "jsp/meraccttrans/transDetaila";
	}
	
	@RequestMapping(value="toOneTransAudit")
	public String toOneTransAudit(Integer maId,MerAcctTransLog merAcctTrans,Model model,HttpServletRequest request)throws Exception{
		if(merAcctTrans.getMaId()!=null){
			SysUsers user = this.getCurrentUser(request);
			//判断是否是商户  1.运营商2商户0管理员
			if(2 == user.getNotype().intValue() || 0 == user.getNotype().intValue()){
				if(user.getNotype().intValue()==UserType.merchant.getValue()){
					merAcctTrans.setMerchantNo(user.getUserno());
				}
				MerAcctTransLog transOne=merAcctTransService.selectByPrimaryKey(maId);
				model.addAttribute("transOne", transOne);
			}else{
				Json json = new Json();
				json.setMsg("您不是商户！");
				model.addAttribute("json", json);
			}
		}else{
			throw new IllegalStateException("maId is null");
		}
		 return "jsp/meraccttrans/merchantAccDetailView";
	}
	
	/**
	 * 商户提现审核
	 */ 
	/**
	 * 根据商户id查询营销账户
	 */
	@RequiresPermissions("trans:cashaudit:view")
	@RequestMapping("/queryMerAcctTransAudit")
	public String queryMerAcctTransAudit(MerAcctTransLog merAcctTrans,Model model,HttpServletRequest request){
		SysUsers user = this.getCurrentUser(request);
		//merAcctTrans.setTransType(MerAcctTransType.merCash.getValue());
		//merAcctTrans.setTransType(MerAcctTransType.edouCash.getValue());
		//判断是否是商户  1.运营商2商户0管理员
		if(2 == user.getNotype().intValue() || 0 == user.getNotype().intValue()){
			merAcctTrans.setTransType("02,05");
			List<MerAcctTransLog> list = merAcctTransService.queryMerAcctTrans(merAcctTrans,request); 
			model.addAttribute("list", list);
		}else{
			Json json = new Json();
			json.setMsg("您不是商户！");
			model.addAttribute("json", json);
		}
		model.addAttribute("merAcctTransPage", merAcctTrans);
		return "jsp/merchantAcc/tixianshenhe";
	}
	
	
	/**
	 * 商户交易EXL导出
	 * @param transList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("trans:meracct:download")
	@RequestMapping(value = "/TransLogMerDownLoad", method = RequestMethod.GET)
	public String TransLogMerDownLoad(MerAcctTransLog MerAcctTransList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try
		{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("商户交易流水信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = {"序号","交易日期", "交易时间","交易类型", "交易金额" , "e豆账户余额","现金账户余额","交易状态"};
			exportMerExcel(MerAcctTransList, titles, outputStream,request);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 商户交易导出
	 * @param transList
	 * @param titles
	 * @param outputStream
	 * @param request
	 */
	public void exportMerExcel(MerAcctTransLog MerAcctTransList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request){
		MerAcctTransList.getPage().setFenye(false);
		List<MerAcctTransLog> merAccList= merAcctTransService.queryMerAcctTrans(MerAcctTransList,request);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook();
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.createSheet("商户交易流水信息");
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle headStyle = exportUtil.getHeadStyle();
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		// 构建表头
		XSSFRow headRow = sheet.createRow(0);
		XSSFCell cell = null;
		for (int i = 0; i < titles.length; i++)
		{
			cell = headRow.createCell(i);
			cell.setCellStyle(headStyle);
			cell.setCellValue(titles[i]);
		}
		int j = 0;
		// 构建表体数据
		for (MerAcctTransLog info : merAccList) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			//交易日期
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			//交易日期
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getIntTxnDt()));
			//交易时间
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getIntTxnTm()));
			//交易类型
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			if(info.getTransType().equals("01")){
				cell.setCellValue(MyStringUtil.obj2Str("e豆入账"));
			}
			if(info.getTransType().equals("02")){
				cell.setCellValue(MyStringUtil.obj2Str("商户提现"));
			}
			if(info.getTransType().equals("03")){
				cell.setCellValue(MyStringUtil.obj2Str("商户充值"));
			}
			if(info.getTransType().equals("04")){
				cell.setCellValue(MyStringUtil.obj2Str("推荐分润"));
			}
			if(info.getTransType().equals("05")){
				cell.setCellValue(MyStringUtil.obj2Str("e豆提现"));
			}
			if(info.getTransType().equals("06")){
				cell.setCellValue(MyStringUtil.obj2Str("冻结"));
			}
			if(info.getTransType().equals("07")){
				cell.setCellValue(MyStringUtil.obj2Str("解冻"));
			}
			if(info.getTransType().equals("08")){
				cell.setCellValue(MyStringUtil.obj2Str("联盟营销收入"));
			}
			if(info.getTransType().equals("09")){
				cell.setCellValue(MyStringUtil.obj2Str("联盟营销支出"));
			}
			//交易金额
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransAmount()));
			//e豆账户余额
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getEdouAvailBal()));
			//现金账户余额
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCashAvailBal()));
			//交易状态
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			if(info.getTransStat().shortValue()==0){
				cell.setCellValue(MyStringUtil.obj2Str("处理中"));
			}
			if(info.getTransStat().shortValue()==1){
				cell.setCellValue(MyStringUtil.obj2Str("失败"));
			}
			if(info.getTransStat().shortValue()==2){
				cell.setCellValue(MyStringUtil.obj2Str("成功"));
			}
			if(info.getTransStat().shortValue()==3){
				cell.setCellValue(MyStringUtil.obj2Str("冲正"));
			}
			j++;
		}
		try{
			workBook.write(outputStream);
			outputStream.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}finally{
			try{
				outputStream.close();
			}
			catch (IOException e){
				e.printStackTrace();
			}
		}

	}
	/**
	 * 商户提现导出
	 * @param merchantEdouSettleList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("trans:cashaudit:download")
	@RequestMapping(value = "/merchantTxEXLDownLoad", method = RequestMethod.GET)
	public String merchantTxEXLDownLoad(MerAcctTransLog merAcctTransLog,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("商户提现报表").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			merchantTxExportExcel(merAcctTransLog, null, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 商户提现导出
	 * @param merchantEdouSettleList
	 * @param titles
	 * @param outputStream
	 * @param request
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void  merchantTxExportExcel(MerAcctTransLog merAcctTransLog,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/商户提现报表.xlsx";
		merAcctTransLog.getPage().setFenye(false);
		merAcctTransLog.setTransType("02,05");
		List<MerAcctTransLog> list = merAcctTransService.queryMerAcctTrans(merAcctTransLog,request); 
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (MerAcctTransLog info : list) {
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
			//交易金额
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransAmount()));
			//交易类型
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			if(info.getTransType().equals("01")){
				cell.setCellValue("e豆入账");
			}
			if(info.getTransType().equals("02")){
				cell.setCellValue("商户提现");
			}
			if(info.getTransType().equals("03")){
				cell.setCellValue("商户充值");
			}
			if(info.getTransType().equals("04")){
				cell.setCellValue("推荐分润");
			}
			if(info.getTransType().equals("05")){
				cell.setCellValue("e豆提现");
			}
			if(info.getTransType().equals("06")){
				cell.setCellValue("冻结");
			}
			if(info.getTransType().equals("07")){
				cell.setCellValue("解冻");
			}
			if(info.getTransType().equals("08")){
				cell.setCellValue("联盟营销收入");
			}
			if(info.getTransType().equals("09")){
				cell.setCellValue("联盟营销支出");
			}
			//交易日期
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getIntTxnDt()==null?"":info.getIntTxnDt());
			//交易时间
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getIntTxnTm()==null?"":info.getIntTxnTm());
			//商户号
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerchantNo()));
			//现金账户余额
			cell = bodyRow.createCell(13);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCashAvailBal()));
			//交易状态码0 - 处理中1 - 失败 2 - 成功
			cell = bodyRow.createCell(14);
			cell.setCellStyle(bodyStyle);
			if(info.getTransStat()==0)
				cell.setCellValue("未审核");
			else if(info.getTransStat()==1)
				cell.setCellValue("审核未通过");
			else if(info.getTransStat()==2)
				cell.setCellValue("审核通过");
			else if(info.getTransStat()==3)
				cell.setCellValue("审核通过未付款");
			//商户名称
			cell = bodyRow.createCell(15);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerName()));
			
			cell = bodyRow.createCell(16);
			cell.setCellStyle(bodyStyle);
			if(info.getWxOpenId().equals("1"))
				cell.setCellValue("银行卡");
			else if(info.getWxOpenId().equals("2"))
				cell.setCellValue("企业付款");
			//打款方式
			cell = bodyRow.createCell(17);
			cell.setCellStyle(bodyStyle);
			if(info.getPayState()==0)
				cell.setCellValue("未打款");
			else if(info.getPayState()==1)
				cell.setCellValue("已打款");
			//打款时间
			cell = bodyRow.createCell(18);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getPayTime()==null?"":DateUtils.dateToStr(info.getPayTime(),"yyyy-MM-dd hh:MM:ss"));
			//交易单号
			cell = bodyRow.createCell(19);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getPartnerTradeNo()));
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
