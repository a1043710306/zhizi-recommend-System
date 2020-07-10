package inveno.spider.reports.util;

import com.madgag.gif.fmsware.GifDecoder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * Created by dell on 2016/5/17.
 */
public class ImageHelper
{
	public static BufferedImage decodeImage(java.io.InputStream is) throws Exception
	{
		java.awt.image.BufferedImage image = null;
		java.io.ByteArrayOutputStream os  = new ByteArrayOutputStream();
		org.apache.commons.io.IOUtils.copy(is, os);
		os.flush();
		os.close();
		byte[] data = os.toByteArray();
		try
		{
			image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(data));
		}
		catch (Exception e)
		{
			GifDecoder decoder = new GifDecoder();
			decoder.read(new java.io.ByteArrayInputStream(data));
			image = decoder.getImage();
		}
		return image;
	}
}