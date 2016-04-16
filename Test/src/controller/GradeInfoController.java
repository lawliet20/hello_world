package com.mk.pro.manage.controller;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
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

import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.GradeInfo;
import com.mk.pro.model.MemberCardBin;
import com.mk.pro.model.Page;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.service.GradeService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.ExportUtil;
import com.mk.pro.utils.MyStringUtil;


/**
 * @author wwj 
 * 2015年4月20日09:38:06
 */
@Controller
@RequestMapping("/gradeInfo")
public class GradeInfoController extends BaseController {
	@Resource
	private GradeService gradeService;
	private static final Logger logger = Logger .getLogger(GradeInfoController.class);
	
	/**
	 * 查询
	 * @param gradeInfo
	 * @param model
	 * @return
	 */
	@RequiresPermissions("member:gradeset:view")
	@RequestMapping("/queryGradeList")
	public String queryGradeInfo(GradeInfo gradeInfo,Model model){
		List<GradeInfo> gradeList = gradeService.queryGradeList(gradeInfo);
		model.addAttribute("gradeList", gradeList);
		//model.addAttribute("gradeInfo", gradeInfo);
		return "/jsp/grade/gradeList";
	}
	
	/**
	 * 页面跳转
	 */
	@RequiresPermissions("member:gradeset:view")
	@RequestMapping("/operGrade")
	public String operGrade(String gradeId,String operType,Model model){
		if("update".equals(MyStringUtil.trim(operType))){
			if(gradeId==null || gradeId.equals("")){
				throw new IllegalStateException("memberid is null");
			}
			GradeInfo gradeDetail = gradeService.getGradeDetail(gradeId);
			model.addAttribute("gradeDetail", gradeDetail);
		}
		model.addAttribute("gradeId", gradeId);
		model.addAttribute("operType", operType);
		return "/jsp/grade/gradeDetail";
	}
	
	/**
	 * 新增
	 * @param gradeInfo
	 * @param model
	 * @return
	 */
	@RequiresPermissions("member:gradeset:create")
	@RequestMapping("/addGrade")
	public void addGradeInfo(GradeInfo gradeInfo,Model model,HttpServletResponse response){
		int count =  gradeService.addGrade(gradeInfo);
		Json json = new Json();
		if(count==0){
			json.setMsg("新增失败!");
		}else if(count==1){
			json.setResult(true);
			json.setMsg("新增成功!");
		}
		this.writeJson(json, response);
	}
	
	/**
	 * 修改
	 * @param gradeInfo
	 * @param model
	 * @return
	 */
	@RequiresPermissions("member:gradeset:update")
	@RequestMapping("/updateGrade")
	public void updateGradeInfo(GradeInfo gradeInfo,Model model,HttpServletResponse response){
		int count = gradeService.updateGrade(gradeInfo);
		Json json = new Json();
		if(count==0){
			json.setMsg("修改失败!");
		}else if(count==1){
			json.setResult(true);
			json.setMsg("修改成功!");
		}
		this.writeJson(json, response);
	}
	
	/**
	 * 删除
	 * @param gradeInfo
	 * @param model
	 * @return
	 */
	@RequiresPermissions("member:gradeset:delete")
	@RequestMapping("/delGrade")
	public void delGradeInfo(String gradeId,String operType,HttpServletResponse response){
		int count = gradeService.delGrade(gradeId);
		Json json = new Json();
		if(count==0){
			json.setMsg("删除失败!");
		}else if(count==1){
			json.setResult(true);
			json.setMsg("删除成功!");
		}
		this.writeJson(json, response);
	}
	/**
	 * 会员等级信息
	 * @param gradeInfoList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("member:gradeset:download")
	@RequestMapping(value = "/gradeEXLDownLoad", method = RequestMethod.GET)
	public String gradeEXLDownLoad(GradeInfo gradeInfoList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("会员等级信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = {"","等级名称", "比例设置","消费金额基数", "消费次数计数","最后修改时刻"};
			cardBinExportExcel(gradeInfoList, titles, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 会员等级信息
	 * @param gradeInfoList
	 * @param titles
	 * @param outputStream
	 * @param request
	 */
	public void cardBinExportExcel(GradeInfo gradeInfoList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request){
		gradeInfoList.setPage(new Page(false));
		List<GradeInfo> gradeInfoLists= gradeService.queryGradeList(gradeInfoList);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook();
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.createSheet("会员等级信息");
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
		for (GradeInfo info : gradeInfoLists) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			//等级名称
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getGradename()));
			//比例设置
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getReturnrate()));
			//消费金额基数
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getPayamtbase()));
			//运营商Id
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getPaycountbase()));
			//最后修改时间
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getLastupdts()==null?"":DateUtils.dateToStr(info.getLastupdts(),"yyyy-MM-dd"));
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
