package com.mk.pro.manage.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tools.zip.ZipOutputStream;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mk.pro.utils.MyUtils;

@Controller
@RequestMapping(value = "down")
public class FileDown extends HttpServlet {


	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	@RequestMapping(value = "/downZip")
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("this is doGet");
		String root = request.getRealPath("/");
		String zipName = request.getParameter("zipname")+".zip";
		String zipPath = root+"temp"+File.separator+zipName;
		try {	
		    File file_zip = new File(zipPath);
		    if(!file_zip.exists()){//如果该zip文件不存在就创建个
				String fileUrl = root+"files"+File.separator+"1.jpg,"+
								 root+"files"+File.separator+"2.txt";//实际文件的'路径名称'，多个用逗号分割
				String fileName = "灯塔.jpg,记事本.txt"; 				 //下载后文件的'名称'，多个用逗号分割
				String[] fileUrlArr = fileUrl.split(",");
				String[] fileNameArr = fileName.split(",");
				File[] files = new File[fileUrlArr.length];
				if(fileUrlArr.length>0){
					for(int i=0;i<fileUrlArr.length;i++){
						File file = new File(fileUrlArr[i]);
						files[i] = file;
					}
				}
				
				ZipOutputStream zosm;
				zosm = new ZipOutputStream(new FileOutputStream(zipPath));
				MyUtils.compressionFiles(zosm, files, "" ,fileNameArr);//创建zip文件
				zosm.close();
		    }
			
			
			InputStream inStream;
			inStream = new FileInputStream(zipPath);
			
			//设置输出的格式
			response.setContentType("application/x-download");// 设置为下载application/x-download
			response.addHeader("content-type ", "application/x-msdownload");
			response.setContentType("application/octet-stream");
			// 设置输出的文件名
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ java.net.URLEncoder.encode(zipName, "UTF-8") + "\"");
			// 循环取出流中的数据
			byte[] b = new byte[100];
			int len;
			OutputStream outStream = response.getOutputStream();
			PrintStream out = new PrintStream(outStream);

			while ((len = inStream.read(b)) > 0) {
				out.write(b, 0, len);
				out.flush();
			}
			out.close();
			inStream.close();
			
		}catch (Exception e){
			try {
				response.setCharacterEncoding("UTF-8");
				response.setContentType("text/html;charset=utf-8");
				PrintWriter out = response.getWriter();
				out.println("您下载的文件已过期！");
				out.close();
				out.flush();
			} catch (IOException e1) {
				//log.error(e1.getMessage());
			}
		}
		/*
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out
				.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");
		out.print(this.getClass());
		out.println(", using the GET method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
		*/
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("this is doPost");
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
