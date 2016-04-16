package com.mk.pro.manage.controller;

import java.io.IOException;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.mk.pro.commons.ResultResp;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.exception.BaseException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.MemberInfo;
import com.mk.pro.model.MerAcctTransLog;
import com.mk.pro.model.MerchantAccountInfo;
import com.mk.pro.model.MerchantInfo;
import com.mk.pro.model.Page;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.MerAcctTransService;
import com.mk.pro.service.MerchantAccountService;
import com.mk.pro.service.MerchantService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.ExportUtil;
import com.mk.pro.utils.MyStringUtil;

/**
 * 商户营销账户管理
 * @author wwj
 * 2015-6-3 10:40:23 
 */
@Controller
@RequestMapping("/merAcountManger")
public class MerchantAccountController extends BaseController<MerchantAccountInfo, String> {
	@Resource(name="merchantAccountService")
	private MerchantAccountService merchantAccountService;
	@Resource(name="merAcctTransService")
	private MerAcctTransService merAcctTransService;
	@Resource(name="merchantService")
	private MerchantService merchantService;

	/**
	 * 根据商户id查询营销账户
	 */
	@RequiresPermissions("market:meraccmanger:view")
	@RequestMapping("/queryMerAcount")
	public String queryMerAcount(MerAcctTransLog merAcctTrans,MerchantAccountInfo ma,Model model,HttpServletRequest request){
		MerchantInfo mer = this.getCurrentMerchant(request);
		SysUsers user = this.getCurrentUser(request);
		String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		if(merAcctTrans.getKssj() == null && merAcctTrans.getJssj() == null){
			merAcctTrans.setKssj(today);
			merAcctTrans.setJssj(today);
		}
		//判断是否是商户  1.运营商2商户0管理员
		if(2 == user.getNotype().intValue() || 0 == user.getNotype().intValue()){
			if(null != mer){
				//当前商户账户信息
				MerchantAccountInfo maInfo = merchantAccountService.queryMerchantAccount(mer.getMerchantNo());
				model.addAttribute("merAccInfo", maInfo);
				//商户账户list
				List<MerAcctTransLog> list = merAcctTransService.queryMerAcctTrans(merAcctTrans,request); 
				model.addAttribute("list", list);
			}
		}else{
			Json json = new Json();
			json.setMsg("您不是商户！");
			model.addAttribute("json", json);
		}
		model.addAttribute("today",today);
		model.addAttribute("merAcctTransPage", merAcctTrans);
		return "jsp/merchantAcc/merchantAccDetail";
	}
	
	/**
	 * 跳转到充值、提现页面
	 */
	@RequiresPermissions("market:meraccmanger:chongzhi")
	@RequestMapping("/toChongZhiJsp")
	public String toChongZhiJsp(Model model,HttpServletRequest request){
		model.addAttribute("merInfo", this.getCurrentMerchant(request));
		return "jsp/aliPay/index";
	}
	
	/**
	 * 跳转到充值、提现页面
	 */
	@RequiresPermissions("market:meraccmanger:chongzhi")
	@RequestMapping("/toTiXianJsp")
	public String toTiXianJsp(Model model,HttpServletRequest request){
		MerchantInfo mer = this.getCurrentMerchant(request);
		MerchantAccountInfo merAccInfo = merchantAccountService.queryMerchantAccount(mer.getMerchantNo());
		//查询一条商户信息
		MerchantInfo merInfo=merchantService.selectMerTransOne(mer.getMerchantNo());
		//商户姓名 acctName
		String acctName=merInfo.getLegalPersonName();
		//根据姓名，第一个显示姓，后面显示*
		String merName=MyStringUtil.addEndName(acctName.substring(0, 1), acctName.length());
		model.addAttribute("merName", merName);
		//商户银行 accBank
		String accBank=merInfo.getAcctBank();
		model.addAttribute("accBank", accBank);
		//商户银行卡号  acctNo
		String accNo=merInfo.getAcctNo();
		Integer accLen=accNo.length()-9;
		//截取账号前6位
		String accNoBegin6=accNo.substring(0,6);
		//截取账号后4位
		String accNoEnd4=MyStringUtil.getStrLast(accNo,4);
		//除前6位和后面4位，其他显示*
		String accNoXing=MyStringUtil.addEndName(accNo.substring(0, 1), accLen);
		String accNoKaHao=accNoBegin6+accNoXing+accNoEnd4;
		model.addAttribute("accNoKaHao", accNoKaHao);
		model.addAttribute("merInfo", mer);
		model.addAttribute("merAccInfo", merAccInfo);
		return "jsp/merchantAcc/tixian";
	}
	/**
	 * 财务提现审核功能
	 * @param merAcctTransLog
	 * @return
	 */
	@RequestMapping("/updateMerAccTransLog")
	public void updateMerAccTransLog(String type,Integer maId,Model model,HttpServletRequest request,HttpServletResponse response){
		ResultResp resp=null;
		try{
			//查询商户账户流水信息
			MerAcctTransLog merTranLog=merAcctTransService.selectByPrimaryKey(maId);
			int res=merAcctTransService.updateMerAccTransLog(type, merTranLog, request);
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
	 * 财务提现审核功能(付款)
	 * @param merAcctTransLog
	 * @return
	 */
	@RequestMapping("/updateMerAccTransLogPay")
	public void updateMerAccTransLogPay(Integer maId,Model model,HttpServletRequest request,HttpServletResponse response){
		ResultResp resp=null;
		try{
			//查询商户账户流水信息
			MerAcctTransLog merTranLog=merAcctTransService.selectByPrimaryKey(maId);
			int res=merAcctTransService.updateMerAccTransLogPay( merTranLog, request);
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
	 * 商户账户流水信息EXL导出
	 * @param merAcctList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("market:meraccmanger:download")
	@RequestMapping(value = "/merAccDownLoad", method = RequestMethod.GET)
	public String merAccDownLoad(MerAcctTransLog merAcctList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("商户账户流水信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = {"","运营商编号", "商户号","交易日期", "交易时间","交易类型","交易金额","商户e豆余额","商户现金账户余额","交易状态","最后修改时刻"};
			exportExcel(merAcctList, titles, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 商户账户流水信息
	 * @param merAcctList
	 * @param titles
	 * @param outputStream
	 * @param request
	 */
	public void exportExcel(MerAcctTransLog merAcctList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request){
		merAcctList.setPage(new Page(false));
		List<MerAcctTransLog> merAccTranstLists= merAcctTransService.queryMerAcctTrans(merAcctList,request);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook();
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.createSheet("商户账户流水信息");
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
		for (MerAcctTransLog info : merAccTranstLists) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			//运营商编号
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentCode()));
			//商户号
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerchantNo()));
			//交易日期
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getIntTxnDt()));
			//交易时间
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getIntTxnTm()));
			//交易类型
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransType()));
			//交易金额
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransAmount()));
			//商户e豆账户余额
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getEdouAvailBal()));
			//商户现金账户余额
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCashAvailBal()));
			//交易状态
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransStat()));
			//最后修改时刻
			cell = bodyRow.createCell(10);
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
