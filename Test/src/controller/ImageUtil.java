package com.mk.pro.manage.controller;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 图片处理工具类
 * 1. 保存图片 - saveImageToServer
 * 2. 生成缩放图（待补充）
 */
public class ImageUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtil.class);

	public String suportImageSize = "60X60,80X80,115X115,150X150,175X175,210X210,460X460";

	public static String role = "MIN";

	// 支持的图片类型
	private static String supportTypesStr = "JPEG,JPG,PNG";
	// default image type
	private static String imgType = "";

	// 识别分隔符
	private static String fileSeparator = "/";

	private static String generateFileSeparator = "_";

	/**
	 * 开关 - 设置生成图片的类型
	 * 
	 * @param imageTypeUp
	 *            : 图片类型（大写）
	 */
	private static void setImageType(String imageTypeUp) {
		// 默认
		if (imageTypeUp == null) {
			imgType = "JPG";
			return;
		}
		// 不包含
		if (!supportTypesStr.contains(imageTypeUp.toUpperCase())) {
			imgType = "JPG";
			return;
		}
		// 包含
		if (supportTypesStr.contains(imageTypeUp.toUpperCase())) {
			imgType = imageTypeUp.toUpperCase();
		}
	}

	/**
	 * 切换开关 flag=true:取
	 * 
	 * @param flag
	 */
	private static void setRule(boolean flag) {
		if (flag) {
			role = "MAX";
		} else {
			role = "MIN";
		}
	}

	/**
	 * 重置图片大小
	 */
	private static BufferedImage resizeImage(BufferedImage source, int targetWith, int targetHeigth, boolean zoomFlag) {

		double widthRate = (double) targetWith / source.getWidth();
		double heightRate = (double) targetHeigth / source.getHeight();

		// 图像的高度和宽度的处理 - 实现在targetW，targetH范围内实现等比缩放。
		if (zoomFlag) {
			if (role.equals("MAX")) {
				if (widthRate > heightRate) {
					widthRate = heightRate;
					targetWith = (int) (widthRate * source.getWidth());
				} else {
					heightRate = widthRate;
					targetHeigth = (int) (heightRate * source.getHeight());
				}
			} else {
				if (!(widthRate > heightRate)) {
					widthRate = heightRate;
					targetWith = (int) (widthRate * source.getWidth());
				} else {
					heightRate = widthRate;
					targetHeigth = (int) (heightRate * source.getHeight());
				}
			}
		} else {
			targetWith = (int) (widthRate * source.getWidth());
			targetHeigth = (int) (heightRate * source.getHeight());
		}

		int type = source.getType();
		BufferedImage target = null;
		if (type == BufferedImage.TYPE_CUSTOM) { // handmade
			ColorModel cm = source.getColorModel();
			WritableRaster raster = cm.createCompatibleWritableRaster(targetWith, targetHeigth);
			boolean alphaPremultiplied = cm.isAlphaPremultiplied();
			target = new BufferedImage(cm, raster, alphaPremultiplied, null);
		} else {
			target = new BufferedImage(targetWith, targetHeigth, type);
		}
		Graphics2D g = target.createGraphics();
		// smoother than exlax:
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawRenderedImage(source, AffineTransform.getScaleInstance(widthRate, heightRate));
		g.dispose();
		return target;
	}

	private String getFileType(String filePath) {
		if (!isEmpty(filePath)) {
			String fileType = filePath.substring(filePath.lastIndexOf(".") + 1);
			return fileType;
		} else {
			LOGGER.error("getFileType() -- 参数为空: filePath=" + filePath);
			return null;
		}
	}

	private String getOnlyFileName(String filePath) {
		if (!isEmpty(filePath)) {
			LOGGER.debug("参数：文件路径=" + filePath);
			// 识别分隔符
			fileSeparator = getFilePathSeparator(filePath);
			String fileName = filePath.substring(filePath.lastIndexOf(fileSeparator) + 1, filePath.lastIndexOf("."));
			LOGGER.debug("识别到的文件名=" + fileName);
			return fileName;
		} else {
			System.err.println("getFileName() -- 参数为空: filePath=" + filePath);
			return "nofileName";
		}
	}

	private String getFileFullName(String filePath) {
		if (!isEmpty(filePath)) {
			String fileFullName = getOnlyFileName(filePath) + "." + getFileType(filePath);
			return fileFullName;
		}
		return null;
	}

	private String getFolder(String filePath) {
		if (!isEmpty(filePath)) {
			String fileFullName = getOnlyFileName(filePath) + "." + getFileType(filePath);
			return filePath.substring(0, filePath.indexOf(fileFullName));
		}
		return null;
	}

	private static String getFilePathSeparator(String filePath) {
		// 识别分隔符
		if (filePath.contains("\\")) {
			return "\\";
		} else if (filePath.contains("/")) {
			return "/";
		} else {
			LOGGER.error("fileSeparator 获取失败，文件路径参数异常");
			return "/";
		}
	}

	private static void saveNewImageToDisk(InputStream fromFileIS, String fromFileType, String saveToFileStr, int width, int hight, boolean zoomFlag)
			throws Exception {
		BufferedImage srcImage = null;
		// 生成与源文件相同的文件格式类型
		imgType = fromFileType;
		srcImage = ImageIO.read(fromFileIS);
		LOGGER.debug("重读原图文件内容流：" + srcImage);
		if (width > 0 || hight > 0) {
			srcImage = resizeImage(srcImage, width, hight, zoomFlag);
		}

		File saveFile = new File(saveToFileStr);
		LOGGER.debug("上传图片信息：" + saveToFileStr);
		ImageIO.write(srcImage, imgType, saveFile);
		srcImage.flush();
		fromFileIS.close();
		LOGGER.debug("图片文件：saveToFileStr 保存完成。");
	}

	/**
	 * 生成需要的 等比缩放图
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	private List<String> buildToMultImages(File file) throws Exception {
		if (file != null) {
			String filePath = file.getAbsolutePath();
			String fromFileType = getFileType(filePath);
			String fromFileName = getOnlyFileName(filePath);
			String targetFolder = getFolder(filePath);
			// 开始生成文件
			String[] imageSize = suportImageSize.split(",");
			List<String> list = new ArrayList<String>();
			list.add(file.getAbsolutePath());
			if (imageSize != null && imageSize.length > 0) {
				for (int i = 0; i < imageSize.length; i++) {
					String spec = imageSize[i];
					if (!isEmpty(spec)) {
						String[] temp = spec.split("X");
						int width = Integer.valueOf(temp[0]);
						int hight = Integer.valueOf(temp[1]);
						targetFolder = targetFolder.endsWith(fileSeparator) ? targetFolder : targetFolder + fileSeparator;
						String newFilePath = targetFolder + fromFileName + generateFileSeparator + spec + "." + fromFileType; // etc:
						// aaa_60X60.JPG
						LOGGER.debug("将要生成的文件路径=" + newFilePath);
						InputStream is = new FileInputStream(file);
						saveNewImageToDisk(is, fromFileType, newFilePath, width, hight, false);
						list.add(newFilePath);
					}
				}
			}
			return list;
		} else {
			LOGGER.error("源图像文件 file 参数对象为空！！！");
		}
		return null;
	}

	private File newFoleder(String targetFilePath) {
		targetFilePath = targetFilePath.toString();
		java.io.File targetFile = new java.io.File(targetFilePath);
		if (targetFile.isDirectory()) {
			LOGGER.debug("创建新的目录 - 目录已存在");
		} else {
			targetFile.mkdirs();
			LOGGER.debug("创建新的目录 - 新建目录成功");
		}
		return targetFile;
	}

	private static File newFile(String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			LOGGER.debug("文件不存在，创建新空文件");
			file.createNewFile();
		}
		return file;
	}

	private boolean isEmpty(String str) {
		if (str == null || str.trim().equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * 将图片流写入文件中
	 */
	private File writeImageStreamToFile(File file, InputStream is, String fileType) throws IOException {
		LOGGER.debug(">>将图片流写入文件中  writeImageStreamToFile");
		BufferedImage image = ImageIO.read(is);
		FileOutputStream fout = new FileOutputStream(file);
		ImageIO.write(image, fileType, fout);
		fout.flush();
		fout.close();
		LOGGER.debug("<<将图片流写入文件中  writeImageStreamToFile");
		return file;
	}

	/**
	 * 保存图片到指定的(新)文件中
	 */
	public String saveImageToServer(InputStream imageInputStream, String fileFullPath) throws IOException {

		String fileFolder = getFolder(fileFullPath);
		//检查或创建目录
		newFoleder(fileFolder);
		//检查或创建文件
		newFile(fileFullPath);

		// 获取文件类型名称
		String fileType = getFileType(fileFullPath);
		// 检查并创建指定目录
		File file = newFoleder(fileFullPath);
		// 将图片流写入文件中
		file = writeImageStreamToFile(file, imageInputStream, fileType);

		return fileFullPath;
	}

}
