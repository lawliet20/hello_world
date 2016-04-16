package com.mk.pro.manage.controller;

import java.io.IOException;
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

import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.exception.ServiceException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.AgentInfo;
import com.mk.pro.model.CashierManInfo;
import com.mk.pro.model.MerUnionActInfo;
import com.mk.pro.model.MerchantInfo;
import com.mk.pro.model.Page;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.CashierManInfoService;
import com.mk.pro.service.MerchantService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.ExportUtil;
import com.mk.pro.utils.MyStringUtil;
/**
 * 
 * @author:ChengKang
 * @date:2015-8-3 下午1:34:28
 * 
 **/
@Controller
@RequestMapping(value = "/cashier")
public class CashierManInfoController extends BaseController{
	@Resource
	private CashierManInfoService cashierManInfoService;
	@Resource
	private MerchantService MerchantService;
	//收银员信息表
	 CashierManInfo  cashierManInfo=new  CashierManInfo();
	/**
	 * 收银员管理页面
	 * @param model
	 * @param request
	 * @return
	 */
	 @RequiresPermissions("agent:cashier:view")
	 @RequestMapping(value = "/toCashierInfolist")
	public String toCashierInfoList(Model model,HttpServletRequest request,CashierManInfo pageCashier){
		SysUsers users=this.getCurrentUser(request);
		//商户
		if(users.getNotype().intValue()==2){
			pageCashier.setMerchantNo(users.getUserno());
		}
		//运营商登陆
		if(users.getNotype().intValue()==1){
			pageCashier.setAgentCode(users.getUserno());
		}
		List<CashierManInfo> cashList=cashierManInfoService.findByPageCashierInfo(pageCashier);
		model.addAttribute("cashList", cashList);
		model.addAttribute("Pagecashier", pageCashier);
		return "jsp/agent/cashierManInfoList";
	}
	 /**
	  * 收银员详情
	  * @param model
	  * @param cmCode
	  * @return
	  */
	 @RequiresPermissions("agent:cashier:view")
	 @RequestMapping(value = "/cashierInfoView")
	 public String cashierInfoView(Model model,Integer cmCode){
		 cashierManInfo=cashierManInfoService.selectByPrimaryKey(cmCode);
		 model.addAttribute("cashierOneView", cashierManInfo);
		return "jsp/agent/cashierDisturbView";
	 }
	 /**
	  * 打开业务员修改  页面  type类型   3修改
	  * @param model
	  * @param pageCashier
	  * @return
	  */
	 @RequiresPermissions("agent:cashier:update")
	 @RequestMapping(value = "/openUpdateCashierInfo")
	 public String openUpdateCashierInfo(Model model,CashierManInfo pageCashier){
		 if(pageCashier.getType().equals("3")){
			 cashierManInfo=cashierManInfoService.selectByPrimaryKey(pageCashier.getCmCode());
			 model.addAttribute("updateCashierOne", cashierManInfo);
		 }
		 return "jsp/agent/updateCashierInfo";
	 }
	 @RequiresPermissions("agent:cashier:update")
	 @RequestMapping(value = "/updateCashierInfo")
	 public void updateCashierInfo(Model model,HttpServletRequest request,HttpServletResponse response,CashierManInfo cashierManInfo){
		 int res=cashierManInfoService.updateByPrimaryKey(request, cashierManInfo);
		 Json json=new Json();
		 if(res>=1){
			 json.setMsg("修改成功！"); 
			 json.setResult(true);
		 }else{
			 json.setMsg("修改失败！"); 
			 json.setResult(false);
		 }
		 this.writeJson(json, response);
	 }
	 /**
	  * 打开增加页面
	  * @param pageCashier
	  * @return
	  */
	 @RequiresPermissions("agent:cashier:create")
	 @RequestMapping(value = "/addCashier")
	 public String addCashier(Model model,HttpServletRequest request,HttpServletResponse response,CashierManInfo cashierManInfo){
		 return "jsp/agent/addCashierInfo";
	 }
	 /**
	  * 增加收银员信息
	  * @param model
	  * @param request
	  * @param response
	  * @param cashierManInfo
	  */
	 @RequiresPermissions("agent:cashier:create")
	 @RequestMapping(value = "/insertCashierInfo")
	 public void insertCashierInfo(Model model,HttpServletRequest request,HttpServletResponse response,CashierManInfo cashierManInfo){
		 int res=cashierManInfoService.insertSelective(request, cashierManInfo);
		 Json json=new Json();
		 if(res>=1){
			 json.setMsg("增加成功！"); 
			 json.setResult(true);
		 }else{
			 json.setMsg("增加失败！"); 
			 json.setResult(false);
		 }
		 this.writeJson(json, response);
	 }
 	/**
	 * 删除一条业务员信息
	 */
	@RequiresPermissions("agent:cashier:delete")
	@RequestMapping("/deleteCashier")
	public void deleteCashier(Integer cmCode,HttpServletResponse response){
		if(cmCode==null){
			throw new IllegalStateException("cmCode is null");
		}
		int res = cashierManInfoService.deleteByPrimaryKey(cmCode);
		Json json = new Json();
		if(res>=1){
			json.setResult(true);
			json.setMsg("删除成功!");
		}else{
			json.setMsg("删除失败!");
		}
		
		this.writeJson(json, response);
	}
	/**
	 * 修改 开启 关闭操作  1：启用 2：关闭 3：修改
	 * @param cmCode
	 * @param response
	 * @param pageCashier
	 */
	@RequestMapping("/updateCashier")
	public void updateCashier(Integer cmCode,HttpServletResponse response,CashierManInfo pageCashier){
		Json json=new Json();
		String type=pageCashier.getType();
		int res=cashierManInfoService.updateByPrimaryKeySelective(pageCashier);
		if(res>=1){
			if(type.equals("1")){
				json.setMsg("启用成功！");
			}
			if(type.equals("2")){
				json.setMsg("关闭成功！");
			}
			if(type.equals("3")){
				json.setMsg("修改成功！");
			}else if(!type.equals("1")&&!type.equals("2")&&!type.equals("3")){
				json.setMsg("操作失败！");
			}
			this.writeJson(json, response);
		}
	}
	 /**
	  * 打开商户信息页面
	  * @param pageCashier
	  * @return
	  */
	 @RequestMapping(value = "/choiceMerchantInfo")
	 public String choiceMerchantInfo( MerchantInfo merchantInfo,Model model,HttpServletRequest request){
		 SysUsers users=this.getCurrentUser(request);
		 //运营商
		 if(users.getNotype().intValue()==1){
			 merchantInfo.setAgentCode(users.getUserno());
		 }
		 //商户
		 if(users.getNotype().intValue()==2){
			 merchantInfo.setMerchantNo(users.getUserno());
		 }
		 
		 List<MerchantInfo> merList= MerchantService.findByPageMerInfo(merchantInfo,request);
		 model.addAttribute("choicMerList", merList);
		 model.addAttribute("pageChoiceMer", merchantInfo);
		 return "jsp/public/choiceMerchantInfoList";
	 }
	 /**
	  * 业务员EXL导出
	  * @param cashierList
	  * @param response
	  * @param request
	  * @return
	  */
	@RequiresPermissions("agent:cashier:download")
	@RequestMapping(value = "/CashierDownLoad", method = RequestMethod.GET)
	public String CashierDownLoad(CashierManInfo cashierList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("运营商资料信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = {"","商户号", "运营商id","终端号", "会员账号" , "总收益","签到状态","收银员状态","最后修改时刻"};
			exportExcel(cashierList, titles, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 业务员EXL导出
	 * @param cashierList
	 * @param titles
	 * @param outputStream
	 * @param request
	 */
	public void exportExcel(CashierManInfo cashierList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request){
		cashierList.setPage(new Page(false));
		List<CashierManInfo> cashierLists= cashierManInfoService.findByPageCashierInfo(cashierList);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook();
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.createSheet("商户联盟营销信息");
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
		for (CashierManInfo info : cashierLists) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			//商户号
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerchantNo()));
			//运营商id
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentCode()));
			//终端号
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTerminalNo()));
			//会员账号
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMemberNo()));
			//总收益
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalIncome()));
			//签到状态
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getSignInStatus()));
			//收银员状态
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getStatus()));
			//最后修改时刻
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
}
