package com.mk.pro.manage.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;
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
import com.mk.pro.commons.enums.UserType;
import com.mk.pro.commons.exception.BaseException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.manage.constants.Constants;
import com.mk.pro.model.AgentInfo;
import com.mk.pro.model.BankCodeInfo;
import com.mk.pro.model.MarketActivityInfo;
import com.mk.pro.model.MemberInfo;
import com.mk.pro.model.MerchantInfo;
import com.mk.pro.model.Page;
import com.mk.pro.model.TerminalInfo;
import com.mk.pro.model.TransLogInfo;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.MemberService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.ExportUtil;
import com.mk.pro.utils.MyStringUtil;

/**
 * @author wwj
 * 2015年4月17日09:10:32
 */
@Controller
@RequestMapping("/memberInfo")
public class MerberInfoController extends BaseController<MemberInfo, String> {
	/**
	 * Logger for this class
	 */
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Resource(name="memberService") 
	private MemberService memberService;
	
	/**
	 * 按条件查询所有会员
	 */
	@RequiresPermissions("member:query:view")
	@RequestMapping("/queryMember")
	public String queryMember(MemberInfo member,Model model,HttpServletRequest request){
		//商户查询自己的会员 运营商查询所属商户的 管理员查询所有的
		SysUsers user = (SysUsers) request.getSession().getAttribute(Constants.CURRENT_USER);
		if(user.getNotype().intValue()==UserType.agent.getValue())
			member.setAgentCode(user.getUserno());
		if(user.getNotype().intValue()==UserType.merchant.getValue())
			member.setRegMerId(user.getUserno());
		List<MemberInfo> memberList = memberService.queryMember(member);
		model.addAttribute("memberList",memberList);
		model.addAttribute("member",member);
		return "/jsp/member/memberList";
	}
	
	/**
	 * 查询会员的详情
	 */
	@RequiresPermissions("member:query:view")
	@RequestMapping("/memberDetail")
	public String getMemberDetail(String memberid,Model model){
		if(memberid==null || memberid.equals("")){
			throw new IllegalStateException("memberid is null");
		}
		MemberInfo member = memberService.getMemberDetail(memberid);
		model.addAttribute("memberDetail", member);
		return "/jsp/member/memberDetail";
	}
	
//	/**
//	 * 会员资料完善 提供给APP
//	 */
//	@ResponseBody
//	@RequestMapping(value = "/merberInfo/update.json")
//	public void updateMerberInfo(MemberInfo memberInfo, Model model, HttpServletRequest request,HttpServletResponse response) throws Exception {
//			int updateCount=memberService.updateMerberInfo(memberInfo);
//			this.writeJson(updateCount, response);
//	}
	/*
	 * 验证会员手机号码 或者 卡号 是否存在
	 */
	@ResponseBody
	@RequestMapping(value = "/merberInfo/select.json")
	public void getMerberInfo(String mobileNoOrCardNo, Model model, HttpServletRequest request,HttpServletResponse response) throws Exception {
		if(mobileNoOrCardNo!=null&&!"".equals( mobileNoOrCardNo)){
			int count=memberService.getMerberInfoCount(mobileNoOrCardNo);
			this.writeJson(count, response);
		}
	}
	/**
	 * 会员信息EXL导出
	 * @param memberList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("member:query:download")
	@RequestMapping(value = "/MemberLogDownLoad", method = RequestMethod.GET)
	public String MemberLogDownLoad(MemberInfo memberList,HttpServletResponse response,HttpServletRequest request){
		response.setContentType("application/binary;charset=UTF-8");
		try{
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("会员信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = {"","会员姓名", "手机号码","性别", "邮箱","绑定卡号","注册日期","生日","身份证号码","会员等级","会员类型","会员状态",
								"积分余额","可用积分","储值余额","可用余额","短信验证码","会员密码","最近消费时间",
								"消费金额","消费次数","注册商户号","注册终端号","推广员收益率","最后修改时刻"};
			exportExcel(memberList, titles, outputStream,request);
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 会员信息EXL导出
	 * @param memberList
	 * @param titles
	 * @param outputStream
	 * @param request
	 */
	public void exportExcel(MemberInfo memberList,String[] titles, ServletOutputStream outputStream,HttpServletRequest request){
		memberList.setPage(new Page(false));
		List<MemberInfo> memberLists= memberService.queryMember(memberList);
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
		for (MemberInfo info : memberLists) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			//会员姓名
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMembername()));
			//手机号码
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMobileno()));
			//性别
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getGender()));
			//邮箱
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getEmail()));
			//绑定卡号
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardid()));
			//注册日期
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getRegdate()==null?"":DateUtils.dateToStr(info.getRegdate(),"yyyy-MM-dd"));
			//生日
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBirthday()));
			//身份证号码
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getIdentitycard()));
			//会员等级
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getGradeid()));
			//会员类型
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMbtype()));
			//会员状态
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMbstatus()));
			//积分余额
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctscore()));
			//可用积分
			cell = bodyRow.createCell(13);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAvailscore()));
			//储值余额
			cell = bodyRow.createCell(14);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctbal()));
			//可用余额
			cell = bodyRow.createCell(15);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAvailbal()));
			//短信验证码
			cell = bodyRow.createCell(16);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getVerifycode()));
			//会员密码
			cell = bodyRow.createCell(17);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getPassword()));
			//最近消费时间
			cell = bodyRow.createCell(18);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getLastpayts()==null?"":DateUtils.dateToStr(info.getLastpayts(),"yyyy-MM-dd"));
			//消费金额
			cell = bodyRow.createCell(19);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTotalpayamt()));
			//消费次数
			cell = bodyRow.createCell(20);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getPaycount()));
			//注册商户号
			cell = bodyRow.createCell(21);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getRegMerId()));
			//注册终端号
			cell = bodyRow.createCell(22);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getRegTermId()));
			//推广员收益率
			cell = bodyRow.createCell(23);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBmrate()));
			//最后修改时刻
			cell = bodyRow.createCell(24);
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
	/**
	 *赠送e豆操作
	 * @param edouMemberId 会员号id
	 * @param edouNum  赠送的e豆数量
	 */
	@RequiresPermissions("member:query:saveEdou")
	@RequestMapping(value = "/updateMerberInfo")
	public void updateMember(String edouMobileNo,BigDecimal edouNum,MemberInfo memberInfo,HttpServletResponse response){
		ResultResp resp = null;
		try{
			memberInfo.setMobileno(edouMobileNo);
			int res = memberService.updateByPrimaryKeySelective(memberInfo, edouNum);
			if(res==1)
				resp = ResultResp.getInstance(true, "e豆赠送成功！");
			 else 
				resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("updateMerberInfo error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
	}
}
