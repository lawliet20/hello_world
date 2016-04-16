package com.mk.pro.manage.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mk.pro.commons.ResultResp;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.exception.ServiceException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.manage.utils.MyStringUtil;
import com.mk.pro.model.CouponInfo;
import com.mk.pro.model.MerchantInfo;
import com.mk.pro.model.TerminalInfo;
import com.mk.pro.model.TransLogInfo;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.persist.TransLogInfoMapper;
import com.mk.pro.service.CouponInfoService;
import com.mk.pro.service.TerminalInfoService;
import com.mk.pro.service.TransLogInfoService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.img.CouponImgUtil;

/**
 * 优惠券信息操作
 * @author wwj
 *2015年5月7日16:04:04
 */
@Controller
@RequestMapping("/couponManage")
public class CouponInfoController extends BaseController<CouponInfo, String> {
	private static final Logger log = Logger.getLogger(CouponInfoController.class);

	@Resource(name="couponInfoService")
	private CouponInfoService couponService;
	@Resource(name="TerminalInfoService")
	private TerminalInfoService terminalInfoService;
	@Resource
	private TransLogInfoService transLogInfoService;

	/**
	 * 查询优惠券信息
	 */
	@RequiresPermissions("market:couponset:view")
	@RequestMapping("/queryCoupon")
	public String queryCouponList(CouponInfo couponInfo,Model model,HttpServletRequest request){
		//日期格式转换
		CouponInfo couponInfo2 = couponInfo;
		if(!MyStringUtil.isEmpty(couponInfo2.getBeginDate()))
		couponInfo2.setBeginDate(couponInfo2.getBeginDate().replaceAll("-", ""));
		if(!MyStringUtil.isEmpty(couponInfo2.getEndDate()))
		couponInfo2.setEndDate(couponInfo2.getEndDate().replaceAll("-", ""));
		List<CouponInfo> couponList = couponService.queryCouponList(couponInfo2,request);
		model.addAttribute("couponInfo", couponInfo);
		model.addAttribute("couponList", couponList);
		return "/jsp/market/coupon/couponList";
	}
	/**
	 * 优惠劵明细
	 * @param transLogInfo
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/openSelectCoup")
	public String openSelectCoupList(String publishDtTime,String expireDt,TransLogInfo transLogInfo,Model model,HttpServletRequest request){
		if(publishDtTime!=null||expireDt!=null){
			transLogInfo.setSateTime2(publishDtTime.replace("-", ""));
			model.addAttribute("publishDtTime", publishDtTime);
			transLogInfo.setEndTime2(expireDt);
			model.addAttribute("expireDtTime", expireDt);
		}
		//日期格式转换
		if(transLogInfo.getSateTime()!=null||transLogInfo.getEndTime()!=null){
			transLogInfo.setSateTime(transLogInfo.getSateTime());
			transLogInfo.setEndTime(transLogInfo.getEndTime());
		}
		//开始-结束 使用时间
		if(transLogInfo.getSateTime2()!=null||transLogInfo.getEndTime2()!=null){
			transLogInfo.setSateTime2(transLogInfo.getSateTime2().replace("-", ""));
			transLogInfo.setEndTime2(transLogInfo.getEndTime2().replace("-", ""));
		}
		model.addAttribute("couponNo", transLogInfo.getCouponNo());
		List<TransLogInfo> couponList = transLogInfoService.findByPageSelectCoupList(transLogInfo);
		model.addAttribute("couponList", couponList);
		model.addAttribute("couponInfoPage", transLogInfo);
		return "/jsp/market/coupon/coupTransMingXiList";
	}
	
	/**
	 * 跳转新增或详情页面
	 */
	@RequiresPermissions("market:couponset:create")
	@RequestMapping("/toAddJsp")
	public String toAddJsp(String couponNo,String type,Model model,HttpServletRequest request){
		if("update".equals(MyStringUtil.trim(type))){
			if(couponNo==null){
				throw new IllegalStateException("couponId is null");
			}
			CouponInfo cp = couponService.getCouponById(couponNo);
			cp.setExpireDate(DateUtils.string2Date(cp.getExpireDt(),null));//设置日期，方便前台展示
			model.addAttribute("couponInfo",cp );
		}
		//System.out.println(JSON.toJSONString(couponService.getCouponById(couponId)));
		return "/jsp/market/coupon/addCoupon";
	}
	
	/**
	 * 新增一条优惠券信息
	 */
	@RequiresPermissions("market:couponset:create")
	@RequestMapping("/addCouponInfo")
	public void addCouponInfo(CouponInfo couponInfo,Model model,HttpServletRequest request,HttpServletResponse response){
		ResultResp resp = null;
		int res = couponService.addCouponInfo(couponInfo,request);
		if(res>0){
			resp = ResultResp.getInstance(ResultCode.success);
		}else{
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}
		
		this.writeJson(resp, response);
	}
	
	/**
	 * 查询当前用户下所有的分店信息(带参数)
	 * @throws Exception 
	 */
	@RequiresPermissions("market:couponset:view")
	@RequestMapping("/queryUserTerms")
	public String queryUserTerms(TerminalInfo terminalInfo,Model model,HttpServletRequest request) throws Exception{
		MerchantInfo merInfo = this.getCurrentMerchant(request);
		//当前用户的id
		if(merInfo==null){
			throw new IllegalStateException("user not Merchant");
		}
		Integer merid = merInfo.getMerId();
		TerminalInfo ter = new TerminalInfo();
		ter.setMerId(merid);
		ter.setTerminalNo(terminalInfo.getTerminalNo());
		ter.setContactName(terminalInfo.getContactName());
		ter.setTermStat(terminalInfo.getTermStat());
		List<TerminalInfo> terminaList = terminalInfoService.queryTerminal(ter);
		model.addAttribute("terminalList", terminaList);
		model.addAttribute("terminalInfo", terminalInfo);
		return "/jsp/market/coupon/terminalInfo";
	}
	
	/**
	 * 删除一条数据
	 */
	@RequiresPermissions("market:couponset:delete")
	@RequestMapping("/deleteCoupon")
	public void deleteCoupon(String couponNo,HttpServletResponse response){
		if(couponNo==null){
			throw new IllegalStateException("couponId is null");
		}
		int res = couponService.delCouponInfo(couponNo);
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
	 * 优惠券模板生成
	 */
	@RequestMapping("/couponModelImage")
	public void couponModelImage(CouponInfo couponInfo,HttpServletRequest request,HttpServletResponse response){
		String saveImgPath = couponService.couponModelImage(couponInfo, request);
		this.writeJson(saveImgPath, response);
	}
	
	/**
	 * 优惠券替换一维码
	 */
	@RequestMapping("/couponOneBarRelace")
	public void couponOneBarRelace(String couponNo, String memberId, HttpServletRequest request,HttpServletResponse response){
		ResultResp resp = null;
		//TODO 添加ip验证
		String saveImgPath = couponService.couponOneBarRelace(couponNo, memberId, request);
		resp = ResultResp.getInstance(ResultCode.success);
		resp.setMsg(saveImgPath);
		this.writeJson(resp, response);
	}
	
	/**
	 * 下载优惠券图片
	 * @throws IOException 
	 */
	@RequestMapping("/downCouponImg")
	public void downCouponImg(String urlString,String filename, HttpServletRequest request,HttpServletResponse response) throws IOException{
		try {
			CouponImgUtil.download(request, response, urlString, filename);
		} catch (Exception e) {
			log.info("优惠券图片下载失败，券号："+filename);
			PrintWriter out = response.getWriter();
		    out.flush();
		    out.println("<script>");
		    out.println("alert('下载劵模板失败，未查询到此模板图片！错误券号："+filename+"')");
		    out.println("</script>");
		    throw new ServiceException(ResultCode.notCoupZipDowln.getIdf());
		}
	}
	/**
	 * 查看详情页面
	 * @param model
	 * @param couponInfo
	 * @param request
	 * @return
	 */
	@RequestMapping("/tocouponDetaila")
	public String couponDetaila(Model model,CouponInfo couponInfo,HttpServletRequest request){
		CouponInfo coupOne=couponService.selectByPrimaryKey(couponInfo);
		model.addAttribute("coupOne",coupOne);
		return "/jsp/market/coupon/couponDetaila";
	}
	/**
	 * 优惠劵记录信息
	 * @param couponInfo
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/queryCouponRecord")
	public String queryCouponRecordList(CouponInfo couponInfo,Model model,HttpServletRequest request){
		SysUsers users=this.getCurrentUser(request);
		//商户登录
		if(users.getNotype().intValue()==2){
			couponInfo.setMerchantNo(users.getUserno());
		}if(users.getNotype().intValue()==1){
			throw new ServiceException(ResultCode.validOperator.getIdf());
		}
		List<CouponInfo> list=couponService.findByPageCouponList(couponInfo);
		model.addAttribute("clist", list);
		model.addAttribute("cPage", couponInfo);
		return "/jsp/market/coupon/couponRecordList";
	}
}
