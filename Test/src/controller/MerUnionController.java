package com.mk.pro.manage.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mk.pro.commons.ResultResp;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.CouponInfo;
import com.mk.pro.model.MerUnionActInfo;
import com.mk.pro.model.Page;
import com.mk.pro.service.CouponInfoService;
import com.mk.pro.service.MerUnionService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.ExportUtil;
import com.mk.pro.utils.MyStringUtil;

/**
 * 商户联盟管理控制类
 */
@Controller
@RequestMapping(value = "/merUnion")
public class MerUnionController extends BaseController<MerUnionActInfo, Integer>{
	
	@Autowired
    private MerUnionService merUnionService;
	@Autowired
    private CouponInfoService couponInfoService;

	/**
	 * 查询被动的活动请求
	 */
    @RequiresPermissions("market:merunion:actreqview")
    @RequestMapping(value = "/actReqList")
    public String actReqList(MerUnionActInfo merUnionActInfo, HttpServletRequest request, Model model) {
    	List<MerUnionActInfo> list = null;
    	ResultResp resp = null;
    	list = merUnionService.findActReqs(merUnionActInfo, request);
		resp = ResultResp.getInstance(ResultCode.success);
		
		model.addAttribute("merUnionList", list);
		model.addAttribute("merUnionActInfo", merUnionActInfo);
		model.addAttribute("result", resp);
        return "jsp/market/merunion/actReqList";
    }
    
    /**
	 * 查询联盟待合作列表
	 */
    @RequiresPermissions("market:merunion:unionreqview")
    @RequestMapping(value = "/unionReqList")
    public String unionReqList(MerUnionActInfo merUnionActInfo, HttpServletRequest request, Model model) {
    	List<MerUnionActInfo> list = null;
    	ResultResp resp = null;
    	list = merUnionService.findUnionReqList(merUnionActInfo, request);
		resp = ResultResp.getInstance(ResultCode.success);
		model.addAttribute("merUnionList", list);
		model.addAttribute("merUnionActInfo", merUnionActInfo);
		model.addAttribute("result", resp);
        return "jsp/market/merunion/unionReqList";
    }
    
    @RequiresPermissions("market:merUnion:view")
    @RequestMapping
    public String list(MerUnionActInfo merUnionActInfo, HttpServletRequest request, Model model) {
    	List<MerUnionActInfo> list = null;
    	list = merUnionService.findMerUnons(merUnionActInfo, request);
		model.addAttribute("merUnionList", list);
		model.addAttribute("merUnionActInfo",merUnionActInfo);
        return "jsp/market/merunion/list";
    }
    
    @RequiresPermissions("market:merUnion:view")
    @RequestMapping(value = "/{id}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("id") Integer id, HttpServletRequest request, Model model) {
    	MerUnionActInfo merUnion = merUnionService.findOne(id);
    	//获取子记录
    	List<MerUnionActInfo> merUnionList = merUnionService.finBychUmUnionList(merUnion);
    	
    	//活动优惠信息表
    	CouponInfo couponInfo = new CouponInfo();
    	couponInfo.setActType(2);
    	if(merUnion.getParentUnionId() != 0){
    		couponInfo.setActId(merUnion.getParentUnionId());
    	}else{
    		couponInfo.setActId(id);
    	}
    	List<CouponInfo> coupons = couponInfoService.queryCoupons(couponInfo);
    	model.addAttribute("merUnion", merUnion);
    	model.addAttribute("merUnionList", merUnionList);
    	model.addAttribute("coupons", coupons);
        return "jsp/market/merunion/detail";
    }
    

    @RequiresPermissions("market:merUnion:create")
    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String showCreateForm(Model model) {
        setCommonData(model);
        model.addAttribute("merUnion", new MerUnionActInfo());
        model.addAttribute("op", "新增");
        return "jsp/market/merunion/edit";
    }

    @RequiresPermissions("market:merUnion:create")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public void create(MerUnionActInfo merUnionActInfo, HttpServletRequest request, HttpServletResponse response) {
    	ResultResp resp = null;
    	if(merUnionService.insert(merUnionActInfo, request)) {
			resp = ResultResp.getInstance(ResultCode.success);
		} else {
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}
		
        writeJson(resp, response);
    }

    @RequiresPermissions("market:merUnion:close")
    @RequestMapping(value = "/{id}/close", method = RequestMethod.POST)
    public void close(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
    	ResultResp resp = null;
    	MerUnionActInfo merUnionActInfo = merUnionService.findOne(id);
		merUnionService.close(merUnionActInfo, request);
		resp = ResultResp.getInstance(ResultCode.success);
    	writeJson(resp, response);
    }
    
    /**
	 * 活动激活操作
	 */
	@RequiresPermissions("market:merUnion:start")
	@RequestMapping("/{id}/start")
	public void start(@PathVariable("id") Integer actId, HttpServletRequest request, HttpServletResponse response){
		ResultResp resp = null;
		merUnionService.startActivity(request, actId);
		resp = ResultResp.getInstance(ResultCode.success);
		this.writeJson(resp, response);
	}
    
    /**
     *接受 
     */
    @RequiresPermissions("market:merUnion:access")
    @RequestMapping(value = "/{id}/access", method = RequestMethod.POST)
    public void access(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
    	ResultResp resp = null;	
    	MerUnionActInfo merUnionActInfo = merUnionService.findOne(id);
		merUnionService.access(merUnionActInfo, request);
		resp = ResultResp.getInstance(ResultCode.success);
    	writeJson(resp, response);
    }
    
    @RequiresPermissions("market:merUnion:refuse")
    @RequestMapping(value = "/{id}/refuse", method = RequestMethod.POST)
    public void refuse(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
    	ResultResp resp = null;
    	MerUnionActInfo merUnionActInfo = merUnionService.findOne(id);
		merUnionService.refuse(merUnionActInfo, request);
		resp = ResultResp.getInstance(ResultCode.success);
    	writeJson(resp, response);
    }
    
    @RequiresPermissions("market:merUnion:refuse")
    @RequestMapping(value = "/{id}/refuseReceive", method = RequestMethod.POST)
    public void refuseReceive(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
    	ResultResp resp = null;
    	MerUnionActInfo merUnionActInfo = merUnionService.findOne(id);
		merUnionService.refuseReceive(merUnionActInfo, request);
		resp = ResultResp.getInstance(ResultCode.success);
    	writeJson(resp, response);
    }
    
    /**
     * 领取
     * @param id
     * @param request
     * @param response
     */
    @RequiresPermissions("market:merUnion:receive")
    @RequestMapping(value = "/{id}/receive", method = RequestMethod.POST)
    public void receive(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
    	ResultResp resp = null;
    	MerUnionActInfo merUnionActInfo = merUnionService.findOne(id);
		merUnionService.receive(merUnionActInfo, request);
		resp = ResultResp.getInstance(ResultCode.success);
    	writeJson(resp, response);
    }
    
    /**
     * 确认领取
     * @param id
     * @param request
     * @param response
     */
    @RequiresPermissions("market:merUnion:confirm")
    @RequestMapping(value = "/{id}/confirm", method = RequestMethod.POST)
    public void confirm(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
    	ResultResp resp = null;
    	MerUnionActInfo merUnionActInfo = merUnionService.findOne(id);
		merUnionService.confirm(merUnionActInfo, request);
		resp = ResultResp.getInstance(ResultCode.success);
    	writeJson(resp, response);
    }
    
 
    @RequestMapping(value = "/getByMerUnion")
    public String getByMerUnion(Model model,HttpServletRequest request){
    	List<MerUnionActInfo> list = null;
		list = merUnionService.findByMerUnion(request);
		model.addAttribute("list", list);
		return "admin/index/merUnionList";
    }
    
    /**
     * 联盟活动记录
     * @param merUnionActInfo
     * @param request
     * @param model
     * @return
     */
    @RequiresPermissions("market:unionlog:view")
    @RequestMapping(value = "/listLog")
    public String listLog(MerUnionActInfo merUnionActInfo, HttpServletRequest request, Model model) {
    	List<MerUnionActInfo> list = null;
    	list = merUnionService.findMerUnonLog(merUnionActInfo, request);
		model.addAttribute("merUnionList", list);
		model.addAttribute("merUnionActInfo",merUnionActInfo);
        return "jsp/market/merunion/listLog";
    }
   /**
    * 商户联盟营销EXL导出
    * @param merUnionActList
    * @param response
    * @param request
    * @return
    */
	@RequiresPermissions("market:unionlog:download")
	@RequestMapping(value = "/unionlogDownLoad", method = RequestMethod.GET)
	public String unionlogDownLoad(MerUnionActInfo merUnionActList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("商户联盟营销信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = {"","发起商户编号", "目标商户编号","请求类型","状态","发起日期",
								"支付酬劳单价","有效期","最大数量","接受日期","结算数量","结算总价","结算时间","父联盟营销Id","最后修改时刻"};
			exportExcel(merUnionActList, titles, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 商户联盟营销EXl导出
	 * @param merUnionActList
	 * @param titles
	 * @param outputStream
	 * @param request
	 */
	public void exportExcel(MerUnionActInfo merUnionActList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request){
		merUnionActList.setPage(new Page(false));
		List<MerUnionActInfo> merUnionActLists= merUnionService.findMerUnonLog(merUnionActList,request);
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
		for (MerUnionActInfo info : merUnionActLists) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			//发起商户编号
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getSourceMerId()));
			//目标商户编号
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTargetMerId()));
			//请求类型
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getReqType()));
			//状态
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getStatus()));
			//发起日期
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getRequestDate()));
			//支付酬劳单价
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getPayValue()));
			//有效期
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getExpireDt()));
			//最大数量
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getActNum()));
			//接受日期
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getAcceptDate()==null?"":DateUtils.dateToStr(info.getAcceptDate(),"yyyy-MM-dd"));
			//结算数量
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getSettleNum()));
			//结算总价
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getSettleAmt()));
			//结算时间
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getSettleDate()==null?"":DateUtils.dateToStr(info.getSettleDate(),"yyyy-MM-dd"));
			//父联盟营销id
			cell = bodyRow.createCell(13);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getParentUnionId()));
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
