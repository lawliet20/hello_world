package com.mk.pro.manage.controller;




import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mk.pro.commons.ResultResp;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.exception.BaseException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.MerchantInfo;
import com.mk.pro.model.MerchantSettleInfo;
import com.mk.pro.model.Page;
import com.mk.pro.model.TransLogInfo;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.MerchantSettleInfoService;
import com.mk.pro.service.TransLogInfoService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.ExportUtil;
import com.mk.pro.utils.MyStringUtil;


/**
 * 
 * @author:JG
 * 清算控制层（
 * 银联渠道对账
—— 基础费率设置
—— 清算报表下载
—— 商户清算报表 （商户权限）
—— 运营商清算报表 （运营商权限））
 * 
 **/
@Controller
@RequestMapping(value = "account")
public class AccountReportsController extends  BaseController<Object, String> {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Resource
	private MerchantSettleInfoService merchantSettleInfoService;
	@Resource
	private TransLogInfoService transLogInfoService;
	/**
	 * 查询当前商户清算报表
	 */
	@RequiresPermissions("account:mer:view")
	@RequestMapping("/merAccountList")
	public String merAccountList(MerchantSettleInfo merchantSettleInfo, Model model,HttpServletRequest request) {
		SysUsers users=this.getCurrentUser(request);
		model.addAttribute("userType", users.getNotype().intValue());
		if(users.getNotype().intValue()==2){
			merchantSettleInfo.setMerNo(users.getUserno());
		}
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
		return "jsp/accountMer/list";
	}
	@RequiresPermissions("account:mer:view")
	@RequestMapping("/merAccountDetail")
	public String merAccountDetail(MerchantSettleInfo merchantSettleInfo, Model model,HttpServletRequest request) {
		MerchantInfo merchantInfo=this.getCurrentMerchant(request);
		merchantSettleInfo.setMerNo(merchantInfo.getMerchantNo());
		
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
		if(merchantSettleInfos!=null&&merchantSettleInfos.size()!=0)
			merchantSettleInfo=merchantSettleInfos.get(0);
		model.addAttribute("merchantSettleInfo", merchantSettleInfo);
		model.addAttribute("result", resp);
		return "jsp/accountMer/detail";
	}
	/**
	 * 导出
	 * @param merchantSettleInfo
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("account:mer:download")
	@RequestMapping(value = "/merAccountDownLoad", method = RequestMethod.GET)
	public String merAccountDownLoad(MerchantSettleInfo merchantSettleInfo,HttpServletResponse response,HttpServletRequest request){
		MerchantInfo merchantInfo=this.getCurrentMerchant(request);
		merchantSettleInfo.setMerNo(merchantInfo.getMerchantNo());
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("商户账户汇总表").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			exportExcel(merchantSettleInfo, null, outputStream,request);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	public void exportExcel( MerchantSettleInfo merchantSettleInfo,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/商户账户汇总表.xlsx";
		merchantSettleInfo.setPage(new Page(false));
		List<MerchantSettleInfo> merchantSettleInfos= merchantSettleInfoService.findByPageMerchantSettleInfos(merchantSettleInfo);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (MerchantSettleInfo info : merchantSettleInfos) {
			XSSFRow bodyRow = sheet.createRow(j + 3);
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(j+1);
			//商户号
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerNo().toString()));
			//汇总日期
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getSettleDt()==null?"":DateUtils.dateToStr(info.getSettleDt(),"yyyy-MM-dd"));
			//非会员消费笔数
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransTotalCount().toString()));
			//非会员消费金额
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransTotalAmt().toString()));
			//非会员交易手续费
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransTotalFee().toString()));
			//非会员消费结算金额
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTransSettleAmt().toString()));
			//会员消费笔数
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMbTransTotalCount().toString()));
			//会员消费金额
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMbTransTotalAmt().toString()));
			//会员交易手续费
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMbTransTotalFee().toString()));
			//会员消费结算金额
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMbTransSettleAmt().toString()));
			//e豆消费笔数
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getEdouTotalCount().toString()));
			//e豆消费金额
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getEdouTotalAmt().toString()));
			//e豆消费账户余额
			cell = bodyRow.createCell(13);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getEdouAvailBal().toString()));
			j++;
		}
		try
		{
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
	 * 查询当前商户清算报表 明细 根据日期和商户号
	 * @param transLogInfo
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/getMingXiList")
	public String getMingXiList(TransLogInfo transLogInfo,Model model,HttpServletRequest request){
		Date date=DateUtils.getNextDay(DateUtils.getStringToDate(transLogInfo.getIntTxnDt(), "yyyy-MM-dd"));
		model.addAttribute("IntTxnDt", DateUtils.getStringToDate(transLogInfo.getIntTxnDt(), "yyyy-MM-dd"));
		model.addAttribute("MerchantNo", transLogInfo.getMerchantNo());
		model.addAttribute("TradeType", transLogInfo.getTradeType());
		transLogInfo.setIntTxnDt(DateUtils.getDateToString(date, "yyyyMMdd"));
		transLogInfo.setTransStat(new Short("2"));
		transLogInfo.setReplyCd("0000");
		List<TransLogInfo> transLogInfoList=null;
		transLogInfoList=transLogInfoService.getPageForTrans(transLogInfo, request);
		model.addAttribute("transLogInfoList", transLogInfoList);
		model.addAttribute("transLogInfoPage", transLogInfo);
		return "jsp/accountMer/transLogInfoList";
		
	}
}
