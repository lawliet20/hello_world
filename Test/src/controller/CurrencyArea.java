package com.mk.pro.manage.controller;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.exception.ServiceException;
import com.mk.pro.manage.constants.Constants;
import com.mk.pro.manage.utils.ResultUpload;
import com.mk.pro.service.AreaCodeInfoService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.img.FileUploadVo;
/**
 * 通用方法
 *
 * @author ChengKang
 * @date 2015-3-15
 * 
 */

@Controller
@RequestMapping(value = "/currency")
public class CurrencyArea{
	@Resource
	private AreaCodeInfoService areaCodeInfoService;
	/**
	 * 下拉列表 - 三级联动-地区查询
	 * @param type
	 * @param code
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getProvDataOne")
	public Map<String, Object> getProvDataOne(String type, Integer code, Model model) throws Exception {
		// type 不能为空
		if (type != null) {
			Map<String, Object> result = new HashMap<String, Object>();
			if (type.equals("0")) {
				// 查询三个地区的数据
				// 一级
				List<Map<String, Object>> one = areaCodeInfoService.getAreaOne();
				result.put("one", one);
				// 二级
				Integer fatherOne = -1;
				if (one != null && one.size() > 0) {
					Map<String, Object> map = one.get(0);
					fatherOne = Integer.valueOf(map.get("key") + "");
				}
				List<Map<String, Object>> two = areaCodeInfoService.getAreaTwo(fatherOne);
				if (two == null) {
					// 有一级没有二级
					two = new ArrayList<Map<String, Object>>();
					two.add(one.get(0));
				}
				result.put("two", two);
				// 三级
				Integer fatherTwo = -1;
				if (two != null && two.size() > 0) {
					Map<String, Object> map = two.get(0);
					fatherTwo = Integer.valueOf(map.get("key") + "");
				}
				List<Map<String, Object>> three = areaCodeInfoService.getAreaThree(fatherTwo);
				if (three == null) {
					// 有二级没有一级
					three = new ArrayList<Map<String, Object>>();
					three.add(two.get(0));
				}
				result.put("three", three);
			} else if (type.equals("1") && code != null) {

				// 仅查询二级和三级的数据 + code
				// 二级
				Integer fatherOne = code;
				List<Map<String, Object>> two = areaCodeInfoService.getAreaTwo(fatherOne);
				if (two == null || two.isEmpty()) {
					Map<String, Object> map = areaCodeInfoService.getSingleAreaOne(fatherOne);
					// 有一级没有二级
					two = new ArrayList<Map<String, Object>>();
					two.add(map);
				}
				result.put("two", two);
				// 三级
				Integer fatherTwo = -1;
				if (two != null && two.size() > 0) {
					Map<String, Object> map = two.get(0);
					fatherTwo = Integer.valueOf(map.get("key") + "");
				}
				List<Map<String, Object>> three = areaCodeInfoService.getAreaThree(fatherTwo);
				if (three == null || two.isEmpty()) {
					// 有二级没有一级
					three = new ArrayList<Map<String, Object>>();
					three.add(two.get(0));
				}
				result.put("three", three);
			} else if (type.equals("2") && code != null) {
				// 仅查询三级的数据 + code
				// 三级
				Integer fatherTwo = code;
				List<Map<String, Object>> three = areaCodeInfoService.getAreaThree(fatherTwo);
				if (three == null || three.isEmpty()) {
					Map<String, Object> map = areaCodeInfoService.getSingleAreaOne(fatherTwo);
					// 有一级没有二级
					three = new ArrayList<Map<String, Object>>();
					three.add(map);
				}
				result.put("three", three);
			}
			return result;
		}
		return null;
	}
	@ResponseBody
	@RequestMapping(value = "/getProvData")
	public Map<String, Object> getProvData(String type, Integer code, Model model) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		if (type != null) {
			if(type.equals("0")){
				//初始化 - 查询一级的数据
				List<Map<String, Object>> one = areaCodeInfoService.getAreaOne();
				result.put("one", one);
			}else if(type.equals("1") && code != null){
				List<Map<String, Object>> two = areaCodeInfoService.getAreaTwo(code);
				result.put("two", two);
			}else if(type.equals("2") && code != null){
				List<Map<String, Object>> three = areaCodeInfoService.getAreaThree(code);
				result.put("three", three);
			}
		}
		return result;
	}
	
	/**
	 *  图片上传 并返回上传后的访问路径
	 * @param vo
	 * @param model
	 * @param request
	 * @param res
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value = "/uploadImage")
	public void saveCameraPhoto(FileUploadVo vo, Model model, HttpServletRequest request,HttpServletResponse res) throws Exception {
		//获取项目跟目录
		String fileRoot =(String) request.getAttribute(Constants.IMG_ROOT);
		String imgUrl =(String) request.getAttribute(Constants.IMG_URL);
		String serverFilePath = vo.getSavePath() +"/"+ DateUtils.dateToStr(new Date(), "yyyyMMdd");
		// 最后写入的目录
		StringBuilder sb = new StringBuilder();
		MultipartFile imageFile = vo.getImageFile();
		if(imageFile.getSize()>(1024*0.7*1000)){
			    PrintWriter out = res.getWriter();
			    out.flush();
			    out.println("<script>");
			    out.println("alert('照片大小最多700KB！')");
			    out.println("</script>");
			    throw new Exception();
		}
		//获取原始图片的名字 并且截取  .后面的
		String originalFilename = imageFile.getOriginalFilename();
		String originalIMG=originalFilename.substring(originalFilename.indexOf("."),originalFilename.length()).toLowerCase();
			if(!originalIMG.equals(".jpg")&&!originalIMG.equals(".jpe")&&!originalIMG.equals(".jpeg")&&!originalIMG.equals(".jif")&&
				!originalIMG.equals(".png")&&!originalIMG.equals(".bmp")&&!originalIMG.equals(".ico")&&
				!originalIMG.equals(".svg")&&!originalIMG.equals(".svgz")&&!originalIMG.equals(".tif")&&
				!originalIMG.equals(".tiff")&&!originalIMG.equals(".ai")&&!originalIMG.equals(".drw")&&
				!originalIMG.equals(".pct")&&!originalIMG.equals(".psp")&&!originalIMG.equals(".xcf")&&
				!originalIMG.equals(".psd")&&!originalIMG.equals(".raw")){
				throw new ServiceException(ResultCode.notImg.getIdf());
			}
		if (imageFile.getSize() != 0) {
			// 新的文件名
			String tempFileName = vo.getTimefilename() +originalIMG;
			String filePath = sb.append(serverFilePath).append("/").append(tempFileName).toString();
			try {
				// 保存到指定文件中
				new ImageUtil().saveImageToServer(imageFile.getInputStream(), fileRoot+filePath);
				Map<String,String> map = new HashMap<String,String>();
				map.put("result", "success");
				map.put("urlContext", imgUrl);
				map.put("backUrl", filePath);
				responseToWrite(res, map);
			} catch (IOException e) {
				System.out.println("查询异常");
			}
		}
	}
	//ie下不能解析application/json问题，用txt/html
	private void responseToWrite(HttpServletResponse res,Object result){
		String string = JSON.toJSONString(result);
		try {
			res.setContentType("text/html;charset=UTF-8");
			res.getWriter().write(string);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	//打开到运营商/商户上传资料的页面
	@RequestMapping(value = "/uploadAgentOrMerImg")
	public String uploadAgentOrMerImg(Model model,HttpServletResponse response,HttpServletRequest request) {
		model.addAttribute("imgUrlId", request.getParameter("imgUrlId"));
		model.addAttribute("imgUrlName", request.getParameter("imgUrlName"));
		return "jsp/uploadImg/uploadImg";
	}
	//64base 上传保存 返回图片路径
	@ResponseBody
	@RequestMapping(value = "/uploadImageBy64Base")
	public void uploadImageBy64Base(String data, Model model, HttpServletRequest request, HttpServletResponse res) throws Exception {
		// 获取项目跟目录
		String fileRoot = (String) request.getAttribute(Constants.IMG_ROOT);// (String) contextConfig.get("upload.root");
		String imgUrl =(String) request.getAttribute(Constants.IMG_URL);
		String fileUrl = "/uploadImages/file/temp/" + DateUtils.dateToStr(new Date(), "yyyyMMdd");
		java.io.File targetFile = new java.io.File(fileRoot + fileUrl);
		if (!targetFile.isDirectory()) {
			targetFile.mkdirs();
		}
		/*
		 * String projectRoot = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/"; String serverRoot = request.getSession().getServletContext().getRealPath("/").replace("\\", "/"); String serverFilePath = "file/image/"+ DateUtils.dateToStr(new Date(), "yyyyMMdd");
		 */
		Base64 base64 = new Base64();
		try {
			// 注意点：实际的图片数据是从 data:png/jpeg;base64, 后开始的
			byte[] k = base64.decode(data.substring("data:image/png;base64,".length()));
			InputStream is = new ByteArrayInputStream(k);
			String fileName = System.currentTimeMillis()+"";
			
			// 检查或创建目录
			java.io.File targetFile1 = new java.io.File(fileRoot + fileUrl);
			if (targetFile1.isDirectory()) {
			} else {
				targetFile1.mkdirs();
			}
			String imgFilePath = fileRoot + fileUrl + "/" + fileName + ".png";
			// 以下其实可以忽略，将图片压缩处理了一下，可以小一点
			double ratio = 1.0;
			BufferedImage image = ImageIO.read(is);
			int newWidth = (int) (image.getWidth() * ratio);
			int newHeight = (int) (image.getHeight() * ratio);
			Image newimage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
			BufferedImage tag = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
			Graphics g = tag.getGraphics();
			g.drawImage(newimage, 0, 0, null);
			g.dispose();
			ImageIO.write(tag, "png", new File(imgFilePath));
			
			// 保存头像文件到数据库中
//			MemberInfo memberInfo = this.getCurrentMember(request);
//			memberInfo.setPhotoUrl(fileUrl + "/" + fileName + ".png");
//			memberService.saveCameraPhoto(memberInfo);
			Map<String,String> map = new HashMap<String,String>();
			map.put("result", "success");
			map.put("urlContext", imgUrl);
			map.put("backUrl", fileUrl + "/" + fileName + ".png"); 
			responseToWrite(res, map);
		} catch (Exception e) {
			e.getMessage();
		}
	}

	//swf 上传
	//64base 上传保存 返回图片路径
	@ResponseBody
	@RequestMapping(value = "/uploadImageBySwf")
	public void uploadImageBySwf(String data, Model model, HttpServletRequest request, HttpServletResponse res) throws Exception {
		String contentType = request.getContentType();

		if ( contentType.indexOf("multipart/form-data") >= 0 )
		{
			ResultUpload result = new ResultUpload();
			result.avatarUrls = new ArrayList();
			result.success = false;
			result.msg = "Failure!";
			//获取项目跟目录
			String fileRoot =(String) request.getAttribute(Constants.IMG_ROOT);
			String imgUrl =(String) request.getAttribute(Constants.IMG_URL);
			String fileUrl = "/uploadImages/file/temp/" + DateUtils.dateToStr(new Date(), "yyyyMMdd");
			
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			FileItemIterator fileItems = upload.getItemIterator(request);
			//基于原图的初始化参数
			String initParams = "";
			BufferedInputStream	inputStream;
			BufferedOutputStream outputStream;
			//遍历表单域
			while( fileItems.hasNext() )
			{
				FileItemStream fileItem = fileItems.next();
				String fieldName = fileItem.getFieldName();
				//是否是原始图片 file 域的名称（默认的 file 域的名称是__source，可在插件配置参数中自定义。参数名：src_field_name）
				Boolean isSourcePic = fieldName.equals("__source");
				//当前头像基于原图的初始化参数（只有上传原图时才会发送该数据，且发送的方式为POST），用于修改头像时保证界面的视图跟保存头像时一致，提升用户体验度。
				//修改头像时设置默认加载的原图url为当前原图url+该参数即可，可直接附加到原图url中储存，不影响图片呈现。
				if ( fieldName.equals("__initParams") )
				{
					inputStream = new BufferedInputStream(fileItem.openStream());
					byte[] bytes = new byte [inputStream.available()];
					inputStream.read(bytes); 
					initParams = new String(bytes, "UTF-8");
					inputStream.close();
				}
				//如果是原始图片 file 域的名称或者以默认的头像域名称的部分“__avatar”打头(默认的头像域名称：__avatar1,2,3...，可在插件配置参数中自定义，参数名：avatar_field_names)
				else if ( isSourcePic || fieldName.startsWith("__avatar") )
				{
					String virtualPath = fileUrl+"/"+System.currentTimeMillis()+".png";
					//原始图片（默认的 file 域的名称是__source，可在插件配置参数中自定义。参数名：src_field_name）。
					if( isSourcePic )
					{
						//文件名，如果是本地或网络图片为原始文件名、如果是摄像头拍照则为 *FromWebcam.jpg
						String sourceFileName = fileItem.getName();	
						//原始文件的扩展名(不包含“.”)
						String sourceExtendName = sourceFileName.substring(sourceFileName.lastIndexOf('.') + 1);
						result.sourceUrl =virtualPath;
					}
					//头像图片（默认的 file 域的名称：__avatar1,2,3...，可在插件配置参数中自定义，参数名：avatar_field_names）。
					else
					{
						result.avatarUrls.add(virtualPath);
					}
					java.io.File targetFile = new java.io.File(fileRoot + fileUrl);
					if (!targetFile.isDirectory()) {
						targetFile.mkdirs();
					}
					inputStream = new BufferedInputStream(fileItem.openStream());
					outputStream = new BufferedOutputStream(new FileOutputStream(fileRoot + virtualPath));
					Streams.copy(inputStream, outputStream, true);
					inputStream.close();
		            outputStream.flush();
		            outputStream.close();
				}
				else
				{
					//注释① upload_url中传递的查询参数，如果定义的method为post请使用下面的代码，否则请删除或注释下面的代码块并使用注释②的代码
					inputStream = new BufferedInputStream(fileItem.openStream());
					byte[] bytes = new byte [inputStream.available()];
					inputStream.read(bytes); 
					inputStream.close();
					if (fieldName.equals("userid"))
					{
						result.userid = new String(bytes, "UTF-8");
					}
					else if (fieldName.equals("username"))
					{
						result.username = new String(bytes, "UTF-8");
					}
				}
			}
			//注释② upload_url中传递的查询参数，如果定义的method为get请使用下面注释的代码
			/*
			result.userid = request.getParameter("userid");
			result.username = request.getParameter("username");
			*/

			if ( result.sourceUrl != null )
			{
				result.sourceUrl += initParams;
			}
			result.imgUrl=imgUrl;
			result.success = true;
			result.msg = "Success!";
			/*
				To Do...可在此处处理储存事项
			*/
			//返回图片的保存结果（返回内容为json字符串，可自行构造，该处使用fastjson构造）
		    PrintWriter out = res.getWriter();
			out.println(JSON.toJSONString(result));
		}
	}
}
