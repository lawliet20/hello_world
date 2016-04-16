package com.mk.pro.manage.controller;

import java.math.BigDecimal;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mk.pro.commons.ResultResp;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.exception.BaseException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.service.AliPayService;
import com.mk.pro.service.MerAcctTransService;
import com.mk.pro.service.MerchantAccountService;

/**
 * 阿里支付
 * 
 * @author wwj 2015年6月4日14:07:43
 */
@Controller
@RequestMapping("/alipay")
public class ALiPayController extends BaseController<Object, String> {
	@Resource(name = "aplipayService")
	private AliPayService aliPayService;
	@Resource
	private MerAcctTransService merAcctTransService;
	@Resource
	private MerchantAccountService merchantAccountService;
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	/**
	 * 建立请求(请求支付宝开始发生支付操作)，以模拟远程HTTP的POST请求方式构造并获取支付宝的处理结果
	 */
	@RequiresPermissions("market:meraccmanger:chongzhi")
	@RequestMapping("/bulidRequest")
	public String buildRequest(Model model, HttpServletRequest request, HttpServletResponse response) {
		try {
			String sHtmlText = aliPayService.buildRequest(request, response);
			model.addAttribute("sHtmlText", sHtmlText);
			return "jsp/aliPay/alipayapi";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 支付后返回支付结果页面(同步通知)
	 */
	@RequestMapping("/toPayResultGet")
	public String toPayResultGet(Model model, HttpServletRequest request) {
		Map map = aliPayService.operPayResultGet(request);
		model.addAttribute("out_trade_no", map.get("out_trade_no"));
		model.addAttribute("trade_no", map.get("trade_no"));
		model.addAttribute("trade_status", map.get("trade_status"));
		model.addAttribute("total_fee", map.get("total_fee"));
		boolean res = (boolean) map.get("success");
		return "jsp/aliPay/returnResult";
	}

	/**
	 * 支付后返回支付结果页面（异步通知）
	 */
	/*@RequestMapping("/toPayResultPost")
	public String toPayResultPost(Model model, HttpServletRequest request) {
		Map map = aliPayService.operPayResultPost(request);
		model.addAttribute("out_trade_no", map.get("out_trade_no"));
		model.addAttribute("trade_no", map.get("trade_no"));
		model.addAttribute("trade_status", map.get("trade_status"));
		model.addAttribute("total_fee", map.get("total_fee"));
		boolean res = (boolean) map.get("success");
		return "jsp/aliPay/returnResult";
	}
*/
	/**
	 * 商户提现
	 * 
	 * @param wIDtotalFee
	 * @param TiXianLeiXing
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/merTiXian")
	public void mertiXian(BigDecimal wIDtotalFee, Integer TiXianLeiXing, HttpServletRequest request, HttpServletResponse response) {
		ResultResp resp=null;
		try{
			int res = merAcctTransService.insertMerAccTransLogJian(wIDtotalFee, TiXianLeiXing, request, response);
			if(res==1)
				resp = ResultResp.getInstance(ResultCode.success);
			 else 
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("mertiXian error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
	}
}
