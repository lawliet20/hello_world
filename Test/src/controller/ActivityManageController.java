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
import com.mk.pro.model.CardInfo;
import com.mk.pro.model.CouponInfo;
import com.mk.pro.model.MarketActivityInfo;
import com.mk.pro.model.Page;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.ActivityManageService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.ExportUtil;
import com.mk.pro.utils.MyStringUtil;

/**
 * 营销活动管理
 * @author wwj
 *2015年5月4日13:38:11
 */
@Controller
@RequestMapping("/activityManage")
public class ActivityManageController extends BaseController<MarketActivityInfo, String> {
	@Resource(name="activityManageService")
	private ActivityManageService activityManageService;
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	/**
	 *	查询当前用户参与的活动 
	 */
	@RequiresPermissions("market:actset:view")
	@RequestMapping("/activityQuery")
	public String activityQuery(MarketActivityInfo marketActivityInfo,Model model,HttpServletRequest request,HttpServletResponse response){
		SysUsers user = this.getCurrentUser(request);
//		marketActivityInfo.setUser(user);
		List<MarketActivityInfo> MarketActivityList = activityManageService.queryMarketActivityList(marketActivityInfo,user);
		model.addAttribute("date", new Date());
		model.addAttribute("MarketActivityList", MarketActivityList);
		model.addAttribute("marketActivityInfo", marketActivityInfo);
		return "/jsp/market/activityManage/activityList";
	}
	
	/**
	 * 跳转新增或详情页面
	 */
	@RequiresPermissions("market:actset:view")
	@RequestMapping("/toAddJsp")
	public String toAddJsp(Integer actid,String type,Model model,HttpServletRequest request){
		if("update".equals(MyStringUtil.trim(type))){
			if(actid==null || actid==0){
				throw new IllegalStateException("actid is null");
			}
			model.addAttribute("activityInfo", activityManageService.getMarketActivityById(actid));
		}
		SysUsers user = this.getCurrentUser(request);
		model.addAttribute("user", user);
		//活动优惠信息表
		model.addAttribute("operType", type);
		return "/jsp/market/activityManage/addMarketActivity";
	}
	
	/**
	 * 删除一条记录
	 * 商户不允许删除（此功能注销）
	 */
	@RequiresPermissions("market:actset:delete")
	@RequestMapping("/delteMarketActivity")
	public void delteMarketActivity(MarketActivityInfo marketActivityInfo,HttpServletResponse response){
		/*int res = activityManageService.deleteMarketActivityInfo(marketActivityInfo);
		Json json = new Json();
		json.setSuccess(true);
		json.setMsg("成功删除了"+res+"条记录!");
		this.writeJson(json, response);*/
	}
	
	/**
	 * 新增一条记录
	 */
	@RequiresPermissions("market:actset:create")
	@RequestMapping("/addMarketActivity")
	public void addMarketActivity(MarketActivityInfo marketActivityInfo,HttpServletRequest request,HttpServletResponse response){
		int res = activityManageService.addMarketActivityInfo(marketActivityInfo,request);
		Json json = new Json();
		if(res>0){
			json.setResult(true);
			json.setMsg("添加成功");
		}else{
			json.setMsg("增加失败");
		}
		this.writeJson(json, response);
	}
	
	/**
	 * 修改一条记录
	 */
	@RequiresPermissions("market:actset:update")
	@RequestMapping("/modifyMarketActivityInfo")
	public void modifyMarketActivityInfo(MarketActivityInfo marketActivityInfo,HttpServletRequest request,HttpServletResponse response){
//		int res = activityManageService.modifyMarketActivityInfo(marketActivityInfo,request);
//		Json json = new Json();
//		if(res>0){
//			json.setResult(true);
//			json.setMsg("成功修改了"+res+"条记录!");
//		}else{
//			json.setMsg("修改失败");
//		}
//		this.writeJson(json, response);
	}
	/**
	 * 修改一条记录
	 */
	@RequiresPermissions("market:actset:update")
	@RequestMapping("/closeOrOpenActivity")
	public void closeOrOpenActivity(MarketActivityInfo marketActivityInfo,HttpServletRequest request,HttpServletResponse response){
		
		ResultResp resp=null;
		try{
			int res = activityManageService.closeOrOpenActivity(marketActivityInfo);
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
	 * 佣金任务活动激活操作
	 */
	@RequiresPermissions("market:actset:update")
	@RequestMapping("/startActivity")
	public void startActivity(Integer actId, HttpServletRequest request, HttpServletResponse response){
		ResultResp resp = null;
		activityManageService.startActivity(request, actId);
		resp = ResultResp.getInstance(ResultCode.success);
		this.writeJson(resp, response);
	}
	
	/**
	 * 查询当前用户下所有未绑定的优惠券
	 */
	@RequiresPermissions("market:couponset:view")
	@RequestMapping("/queryCouponList")
	public String queryCouponList(CouponInfo couponInfo ,String operType,Model model,HttpServletRequest request) throws Exception{
		if(!MyStringUtil.isEmpty(couponInfo.getBeginDate())){
			couponInfo.setBeginDate(couponInfo.getBeginDate().replaceAll("-", ""));
		}
		if(!MyStringUtil.isEmpty(couponInfo.getEndDate())){
			couponInfo.setEndDate(couponInfo.getEndDate().replaceAll("-", ""));
		}
		List<CouponInfo> CouponInfoList = activityManageService.queryCurrUserCouponList(couponInfo,request);
		model.addAttribute("couponList", CouponInfoList);
		model.addAttribute("operTppe",operType);
		return "/jsp/market/activityManage/couponList";
	}
	/**
	 * 会员卡信息EXL导出
	 * @param cardInfo
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("market:actset:download")
	@RequestMapping(value = "/activityEXLDownLoad", method = RequestMethod.GET)
	public String activityEXLDownLoad(MarketActivityInfo marketActivityInfo,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("营销活动信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = {"","发起类型", "活动状态","发布渠道", "运营商编号","商户编号","活动标题","活动说明","支付酬劳单价","最大数量",
								"使用门店","门店清单","活动起始时间","活动结束时间","最后修改时刻"};
			activityExportExcel(marketActivityInfo, titles, outputStream,request);
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
	public void activityExportExcel(MarketActivityInfo marketActivityInfo,String[] titles, ServletOutputStream outputStream,HttpServletRequest request){
		marketActivityInfo.setPage(new Page(false));
		SysUsers user = this.getCurrentUser(request);
		List<MarketActivityInfo> marketActivityList= activityManageService.queryMarketActivityList(marketActivityInfo,user);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook();
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.createSheet("营销活动信息");
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
		for (MarketActivityInfo info : marketActivityList) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			//发起类型
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getPubtype()));
			//活动状态
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getActstatus()));
			//发布渠道
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getActType()));
			//运营商编号
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentcode()));
			//商户编号
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerchantno()));
			//活动标题
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getActtitle()));
			//活动说明
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getActdesc()));
			//支付酬劳单价
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getPaidValue()));
			//最大数量
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getActNum()));
			//使用门店
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getJointype()));
			//门店清单 
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getJointerms()));
			//活动起始时间
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getStartdate()==null?"":DateUtils.dateToStr(info.getStartdate(),"yyyy-MM-dd"));
			//活动结束时间
			cell = bodyRow.createCell(13);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getEnddate()==null?"":DateUtils.dateToStr(info.getEnddate(),"yyyy-MM-dd"));
			//最后修改时刻
			cell = bodyRow.createCell(14);
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
