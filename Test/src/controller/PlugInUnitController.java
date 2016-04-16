package com.mk.pro.manage.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
import com.mk.pro.model.MemberAcctTransLog;
import com.mk.pro.model.MemberCardBin;
import com.mk.pro.model.MerchantInfo;
import com.mk.pro.model.MerchantYLPay;
import com.mk.pro.model.Page;
import com.mk.pro.service.MerchantService;
import com.mk.pro.service.MerchantYLPayService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.ExportUtil;
import com.mk.pro.utils.FileUtils;
import com.mk.pro.utils.MyStringUtil;
@Controller
@RequestMapping(value = "/plug")
public class PlugInUnitController {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Resource
	MerchantYLPayService merchantYLPayService;
	@Resource
	MerchantService merchantService;
	
	@RequiresPermissions("plug:ylPay:view")
	@RequestMapping(value = "/openmerchantYLPayList")
	public String openmerchantYLPayList(MerchantYLPay merchantYLPay, Model model,HttpServletRequest request){
		List<MerchantYLPay> merchantYLPayList=merchantYLPayService.findByPageMerchantYLPay(merchantYLPay);
		model.addAttribute("mList", merchantYLPayList);
		model.addAttribute("mPage", merchantYLPay);
		return "jsp/PlugInUnit/merchantYLPayList";
	}
	/**
	 * 银联商户对比
	 * @param merchantYLPay
	 */
	@RequestMapping(value = "/insertYLPay")
	public  void insertYLPay(MerchantYLPay merchantYLPay){
		// 查询列表
		ResultResp resp = null;
		try{
			merchantYLPayService.insertSelective();
			resp = ResultResp.getInstance(ResultCode.success);
		}catch (BaseException e) {
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch (Exception e) {
			log.error("cardBinList error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
	}
	/**
	 * 会员EXL导出 
	 * @param memberAcctTransLog
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/YLPayEXLDownLoad", method = RequestMethod.GET)
	public String memberAccTrainLogEXLDownLoad(MerchantYLPay merchantYLPay,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("银联商户对比信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xlsx");// 组装附件名称和格式
			memberAccTrainLogExportExcel(merchantYLPay, null, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	public void  memberAccTrainLogExportExcel(MerchantYLPay merchantYLPay,String[] titles, ServletOutputStream outputStream,HttpServletRequest request) throws FileNotFoundException, IOException{
		//获取项目需要的xlsx文件路劲
		String path=request.getSession().getServletContext().getRealPath("")+"/xlsx/银联商户对比信息.xlsx";
		merchantYLPay.setPage(new Page(false));
		MerchantYLPay mOne=merchantYLPayService.selectByPrimaryKey(merchantYLPay.getId());
		//会员商户号数据
		String memberNo=mOne.getInnerMerchantNo();
		String[] memberArr = memberNo.split(",");
		//非会员商户号数据
		String notmemberNo=mOne.getMerchantNo();
		String[] notMemberArr = notmemberNo.split(",");
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (String memberStr : memberArr) {
			XSSFRow bodyRow = sheet.createRow(j + 2);
				//会员
				cell = bodyRow.createCell(5);
				cell.setCellStyle(bodyStyle);
				System.out.println("会员商户号"+memberStr);
				cell.setCellValue(MyStringUtil.obj2Str(memberStr));
			j++;
		}
		int s=0;
		for (String notmemberStr : notMemberArr) {
			XSSFRow bodyRow = sheet.getRow(s + 2);
				//非会员
				cell = bodyRow.createCell(6);
				cell.setCellStyle(bodyStyle);
				System.out.println("非会员号"+notmemberStr);
				cell.setCellValue(MyStringUtil.obj2Str(notmemberStr));
			s++;
		}
		
		try{
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
