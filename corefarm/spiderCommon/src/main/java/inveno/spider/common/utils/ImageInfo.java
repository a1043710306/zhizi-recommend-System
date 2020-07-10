package inveno.spider.common.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pragone.jphash.image.radial.RadialHash;
import com.pragone.jphash.image.radial.RadialHashAlgorithm;

public class ImageInfo implements java.io.Serializable {
	private static final long serialVersionUID = 9119583448349109639L;

	private static final Logger log = Logger.getLogger(ImageInfo.class);

	private String url;
	private String format;
	private int width;
	private int height;

	public ImageInfo(String json) throws Exception {
		JsonElement je = NotMapModelBuilder.getInstance().build(json, JsonElement.class);
		JsonObject obj = je.getAsJsonObject();
		url = obj.getAsJsonPrimitive("url").getAsString();
		format = obj.getAsJsonPrimitive("format").getAsString();
		width = obj.getAsJsonPrimitive("width").getAsInt();
		height = obj.getAsJsonPrimitive("height").getAsInt();
		if (url == null)
			throw new Exception("Invalid url parsed from" + json);
	}

	public ImageInfo(String _url, String _format, int _width, int _height) {
		url = _url;
		format = _format;
		width = _width;
		height = _height;
	}

	public static ImageInfo fromUrl(String imageLink) {
		try {
			String url = imageLink;
			String query = (new java.net.URL(imageLink)).getQuery();
			String[] param = query.split("&");
			String format = null;
			int width = -1;
			int height = -1;
			for (int j = 0; j < param.length; j++) {
				String[] pair = param[j].split("=");
				if (pair[0].equalsIgnoreCase("size")) {
					String[] d = pair[1].split("\\*");
					width = Integer.parseInt(d[0]);
					height = Integer.parseInt(d[1]);
					// modify by Symon@2017年1月4日15:31:14，所有图片url都不移除size跟fmt参数
					/*
					 * String replacement = ((j > 0) ? "&" : "") + param[j]; url
					 * = url.replace(replacement, "");
					 */
				} else if (pair[0].equalsIgnoreCase("fmt")) {
					int idx = (pair[1].startsWith(".")) ? 1 : 0;
					format = pair[1].substring(idx);
					/*
					 * String replacement = ((j > 0) ? "&" : "") + param[j]; url
					 * = url.replace(replacement, "");
					 */
				}
			}
			if (format != null && width > 0 && height > 0) {
				return new ImageInfo(url, format, width, height);
			}
		} catch (Exception e) {
			log.error("fromUrl " + imageLink + "has exception:", e);
		}
		return null;
	}

	public String getUrl() {
		return url;
	}

	public String getFormat() {
		return format;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public String normalizeUrl() {
		StringBuffer sb = new StringBuffer();
		sb.append(url);
		if (url.indexOf("&size=") < 0) {
			sb.append("&size=");
			sb.append(width + "*" + height);
		}
		if (url.indexOf("&fmt=") < 0) {
			sb.append("&fmt=.");
			sb.append(format);
		}
		return sb.toString();
	}

	public HashMap<String, Object> toMap() {
		HashMap<String, Object> mImage = new HashMap<String, Object>();
		mImage.put("src", url);
		mImage.put("format", format);
		mImage.put("width", width);
		mImage.put("height", height);
		mImage.put("desc", "");
		return mImage;
	}

	/**
	 * 獲取图片特征值
	 * 
	 * @param imageUrl
	 * @return
	 */
	public static String getFeatureValue(String imageUrl) {
		// 缩小尺寸，简化色彩
		int[][] grayMatrix = getGrayPixel(imageUrl, 32, 32);
		if(grayMatrix == null)
			return null;
		// 计算DCT
		grayMatrix = DCT(grayMatrix, 32);
		// 缩小DCT，计算平均值
		int[][] newMatrix = new int[8][8];
		double average = 0;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				newMatrix[i][j] = grayMatrix[i][j];
				average += grayMatrix[i][j];
			}
		}
		average /= 64.0;
		// 计算hash值
		String hash = "";
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (newMatrix[i][j] < average) {
					hash += '0';
				} else {
					hash += '1';
				}
			}
		}
		return hash;
	}
	
	public static RadialHash getFeatureValueByRadiaHash(String imageUrl) {
		if(StringUtils.isEmpty(imageUrl))
			return null;
		RadialHash radialHash = null;
		Map<String, Object> map;
		InputStream is = null;
		try {
			map = HttpClientUtils.getInputStreamByUrl(imageUrl.trim());
			is = (InputStream)map.get("stream");
			radialHash = RadialHashAlgorithm.getHash(is);
		} catch (Exception e) {
			log.error("get inputstream from url:" + imageUrl.trim() + " has exception:", e);
		} finally {
			if(is != null)
				try {
					is.close();
				} catch (IOException e) {
					log.error("close inputstream has exception:", e);
					is = null;
				}
		}
		return radialHash;
	}

	/**
	 * 缩小尺寸，简化色彩
	 * 
	 * @param imagePath
	 * @param width
	 * @param height
	 * @return
	 */
	private static int[][] getGrayPixel(String imageUrl, int width, int height) {
		BufferedImage bi = null;
		try {
			bi = resizeImage(imageUrl, width, height, BufferedImage.TYPE_INT_RGB);
		} catch (Exception e) {
			log.error("resizeImage URL: "+ imageUrl + " has exception: ", e);
			return null;
		}
		int minx = bi.getMinX();
		int miny = bi.getMinY();
		int[][] matrix = new int[width - minx][height - miny];
		for (int i = minx; i < width; i++) {
			for (int j = miny; j < height; j++) {
				int pixel = bi.getRGB(i, j);
				int red = (pixel & 0xff0000) >> 16;
				int green = (pixel & 0xff00) >> 8;
				int blue = (pixel & 0xff);
				int gray = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
				matrix[i][j] = gray;
			}
		}
		return matrix;
	}

	private static BufferedImage resizeImage(String imageUrl, int width, int height, int imageType)
			throws IOException {
		BufferedImage srcImg = ImageIO.read(new URL(imageUrl));
		BufferedImage buffImg = null;
		buffImg = new BufferedImage(width, height, imageType);
		buffImg.getGraphics().drawImage(srcImg.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
		return buffImg;
	}

	/**
	 * 离散余弦变换
	 * 
	 * @param pix
	 *            原图像的数据矩阵
	 * @param n
	 *            原图像(n*n)的高或宽
	 * @return 变换后的矩阵数组
	 */
	private static int[][] DCT(int[][] pix, int n) {
		double[][] iMatrix = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				iMatrix[i][j] = (double) (pix[i][j]);
			}
		}
		double[][] quotient = coefficient(n); // 求系数矩阵
		double[][] quotientT = transposingMatrix(quotient, n); // 转置系数矩阵

		double[][] temp = new double[n][n];
		temp = matrixMultiply(quotient, iMatrix, n);
		iMatrix = matrixMultiply(temp, quotientT, n);

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				pix[i][j] = (int) (iMatrix[i][j]);
			}
		}
		return pix;
	}

	/**
	 * 求离散余弦变换的系数矩阵
	 * 
	 * @param n
	 *            n*n矩阵的大小
	 * @return 系数矩阵
	 */
	private static double[][] coefficient(int n) {
		double[][] coeff = new double[n][n];
		double sqrt = 1.0 / Math.sqrt(n);
		for (int i = 0; i < n; i++) {
			coeff[0][i] = sqrt;
		}
		for (int i = 1; i < n; i++) {
			for (int j = 0; j < n; j++) {
				coeff[i][j] = Math.sqrt(2.0 / n) * Math.cos(i * Math.PI * (j + 0.5) / (double) n);
			}
		}
		return coeff;
	}

	/**
	 * 矩阵转置
	 * 
	 * @param matrix
	 *            原矩阵
	 * @param n
	 *            矩阵(n*n)的高或宽
	 * @return 转置后的矩阵
	 */
	private static double[][] transposingMatrix(double[][] matrix, int n) {
		double nMatrix[][] = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				nMatrix[i][j] = matrix[j][i];
			}
		}
		return nMatrix;
	}

	/**
	 * 矩阵相乘
	 * 
	 * @param A
	 *            矩阵A
	 * @param B
	 *            矩阵B
	 * @param n
	 *            矩阵的大小n*n
	 * @return 结果矩阵
	 */
	private static double[][] matrixMultiply(double[][] A, double[][] B, int n) {
		double nMatrix[][] = new double[n][n];
		int t = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				t = 0;
				for (int k = 0; k < n; k++) {
					t += A[i][k] * B[k][j];
				}
				nMatrix[i][j] = t;
			}
		}
		return nMatrix;
	}
	
	/**
	 * 用于计算pHash的相似度<br>
	 * 相似度为1时，图片最相似
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static double calculateSimilarity(String str1, String str2) {
		if((str1.length()!=64)||(str2.length()!=64))  
	        return -1;  
		int num = 0;
		for(int i = 0; i < 64; i++){
			if(str1.charAt(i) == str2.charAt(i)){
				num++;
			}
		}
		return ((double)num) / 64.0;
	}
	
	//汉明距离计算  
	 public static int hanmingDistance(String str1,String str2)  
	 {  
	    if((str1.length()!=64)||(str2.length()!=64))  
	        return -1;  
	    int difference = 0;  
	    for(int i=0;i<64;i++)  
	    {  
	        if(str1.charAt(i) != str2.charAt(i))  
	            difference++;  
	    }  
	    return difference;  
	 }
	
	public static void main(String[] args) {
		String hash1 = getFeatureValue("http://cloudimg.hotoday.in/v1/icon?id=6647988317858033605&size=700*420&fmt=.jpeg");
		String hash2 = getFeatureValue("http://cloudimg.hotoday.in/v1/icon?id=2774947283363644346&size=680*302&fmt=.jpeg");
		String hash3 = getFeatureValue("http://img.my.csdn.net/uploads/201303/27/1364398783_4355.jpg");
		String hash4 = getFeatureValue("http://img.my.csdn.net/uploads/201303/27/1364398786_6679.jpg");
		
		System.out.println("image1 and image1 :" + calculateSimilarity(hash1, hash1));
		System.out.println(hanmingDistance(hash1, hash2));
		System.out.println("image1 and image2 :" + calculateSimilarity(hash1, hash2));
		System.out.println("image1 and image3 :" + calculateSimilarity(hash1, hash3));
		System.out.println("image1 and image4 :" + calculateSimilarity(hash1, hash4));
		System.out.println("--------");
		System.out.println("image2 and image2 :" + calculateSimilarity(hash2, hash2));
		System.out.println("image2 and image3 :" + calculateSimilarity(hash2, hash3));
		System.out.println("image2 and image4 :" + calculateSimilarity(hash2, hash4));
		System.out.println("--------");
		System.out.println("image3 and image3 :" + calculateSimilarity(hash3, hash3));
		System.out.println("image3 and image4 :" + calculateSimilarity(hash3, hash4));
		System.out.println("--------");
		System.out.println("image4 and image4 :" + calculateSimilarity(hash4, hash4));
		
	}
}