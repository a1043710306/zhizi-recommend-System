package inveno.spider.parser.utils;

import inveno.spider.parser.exception.ExtractException;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;


public class Utils {
	/**
     * 
     */
    private static Pattern scalarValue = Pattern.compile("(&#(\\d{5})[;]?)");
	private static final char[] unencodedChars = new char[] { '-', '_', '.',
			'~', '!', '*', '\'', '(', ')', ';', ':', '@', '&', '=', '+', '$',
			',', '/', '?', '#', '[', ']', '%' };

	private static Configuration configuration = null;

	private static final String ZERO="0";
	private static final String TRUE="true";
	private static final String FALSE="false";
	private static final String ERROR_EMPTY_DATE = "error:empty date";
	
	
    private static final String DATE_FORMAT_SHORT = "yyyyMMdd";
    private static final String DATE_FORMAT_LONG = "EEE MMM dd HH:mm:ss z yyyy";


	

	/**
	 * calculate absolute link from relative links found in html
	 * 
	 * @param baseUrl
	 *            base url
	 * @param path
	 * @return
	 */
	public static String calculateLink(String baseUrl, String path,
			String encoding) {
		try {
			URL pathUrl;
			URL baseURL = new URL(baseUrl);
			if (path != null && path.startsWith("?")) {
				// create the base URL minus query.
				URL base = new URL(baseURL.getProtocol(), baseURL.getHost(),
						baseURL.getPort(), baseURL.getPath());
				pathUrl = new URL(base.toString() + path);
			} else {
				// fix bug:if the parameter of path is null,must convert it to
				// ""
				pathUrl = new URL(baseURL, null == path ? "" : path);
			}
			String str = encodeUrl(pathUrl.toString(), encoding);
			return StringUtils.replaceChars(str, '\\', '/');
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		// if(path==null) return null;
		// if(path.startsWith("http:") || path.startsWith("https:"))
		// return path; // absolute path
		//
		// try {
		// URL base = new URL(baseUrl);
		// int port = base.getPort();
		// String remotePath = base.getPath();
		//
		// if(StringUtils.isBlank(remotePath)) { // is root path
		// if(!path.startsWith("/")) path = "/" + path; // prepend with "/"
		// }
		// if(path.startsWith("/")) {
		// if(port<0) // default port
		// return base.getProtocol() + "://" + base.getHost() + path;
		// else // custom port
		// return base.getProtocol() + "://" + base.getHost() + ":" +
		// base.getPort() + path;
		//
		// } else {
		// return StringUtils.substringBeforeLast(baseUrl, "/") + "/" + path;
		// }
		//
		// } catch (MalformedURLException e) {
		// throw new RuntimeException(e);
		// }
	}

	/**
	 * Replace urls with date in it. Replace <%yyyy%> <%yy%> with year, <%mm%>
	 * <%m%> with month, <%dd%> <%d%> with day in month
	 * 
	 * @param str
	 * @return
	 */
	public static String replaceDateTemplate(String str) {
		Date today = new Date();
		return replaceDateTemplate(str, today);
	}

	public static String replaceDateTemplate(String str, Date replacedDate) {
		if (str.contains("<%yyyy%>")) {
			str = StringUtils.replace(str, "<%yyyy%>", new SimpleDateFormat(
					"yyyy").format(replacedDate));
		}
		if (str.contains("<%yy%>")) {
			str = StringUtils.replace(str, "<%yy%>",
					new SimpleDateFormat("yy").format(replacedDate));
		}
		if (str.contains("<%mm%>")) {
			str = StringUtils.replace(str, "<%mm%>",
					new SimpleDateFormat("MM").format(replacedDate));
		}
		if (str.contains("<%m%>")) {
			str = StringUtils.replace(str, "<%m%>",
					new SimpleDateFormat("M").format(replacedDate));
		}
		if (str.contains("<%dd%>")) {
			str = StringUtils.replace(str, "<%dd%>",
					new SimpleDateFormat("dd").format(replacedDate));
		}
		if (str.contains("<%d%>")) {
			str = StringUtils.replace(str, "<%d%>",
					new SimpleDateFormat("d").format(replacedDate));
		}
		return str;
	}

	public static String replaceDateTemplate(String str, int listingAddDays) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, listingAddDays);
		Date replacedDate = calendar.getTime();
		return replaceDateTemplate(str, replacedDate);
	}

	/**
	 * * calculate nextpage link from pages_set links found in html
	 * 
	 * @param currentUrl
	 *            current url
	 * @param pagesUrl
	 *            pages_set url
	 * @return
	 */
	public static String calculateNextpageLink(String currentUrl,
			String pagesUrl) throws ExtractException {
		// difference is digit
		int idx = StringUtils.indexOfDifference(currentUrl, pagesUrl);
		if (idx == -1)
			throw new ExtractException(
					"the baseUrl is equals to pagesUrl, pagesUrl:" + pagesUrl);
		// the difference digit
		while (idx - 1 >= 0 && Character.isDigit(currentUrl.charAt(idx - 1))) {
			idx--;
		}
		Pattern pattern = Pattern.compile("\\d+");
		// Trailing empty strings are included
		String[] strBase = pattern.split(currentUrl, -1);
		String[] strPages = pattern.split(pagesUrl, -1);
		// showforum-2.aspx
		// showforum-2-2.aspx
		// the first page or not
		if (strBase.length < strPages.length) {
			// seq = String.valueOf(2);
			return pagesUrl;
		} else if (strBase.length == strPages.length) {
			String strLast = currentUrl.substring(idx);
			Matcher m = pattern.matcher(strLast);
			if (m.find()) {
				String strNum = m.group();
				long i = Long.parseLong(strNum);
				String seq = String.valueOf(++i);
				// String strDif = pagesUrl.substring(idx);
				// Matcher matcher = pattern.matcher(strDif);
				String strReplace = m.replaceFirst(seq);
				return currentUrl.substring(0, idx) + strReplace;
			} else
				throw new ExtractException(
						"the difference between baseUrl and pagesUrl doesnot contain digit, difference in baseUrl : "
								+ strLast);
		} else
			throw new ExtractException(
					"the pages_set found is error, pagesUrl:" + pagesUrl);

	}

	/**
	 * Method to trim multiple spaces in HTML
	 * 
	 * @param text
	 * @return
	 */
	public static String trimHtmlMultipleSpaces(String text) {
		text = text.replace('\r', ' ');
		text = text.replace('\n', ' ');

		if (StringUtils.isBlank(text) && text.length() > 0) // return a single
															// space
			return " ";

		boolean lastIsSpace = false;
		StringBuilder sb = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == ' ') {
				if (lastIsSpace)
					continue;
				lastIsSpace = true;
			} else {
				lastIsSpace = false;
			}
			sb.append(c);
		}
		return sb.toString();
	}

	public static String encodeUrlForSqlLoader(String url) {
		if (url == null)
			return null;
		return url.replaceAll(";", "%3B");
	}

	public static String encodeUrlForChineseCharAndSpace(String url,
			String encoding) {
		if (encoding == null)
			return url;
		StringBuilder newUrlSb = new StringBuilder();
		for (char ch : url.toCharArray()) {
			if (Character.OTHER_LETTER == Character.getType(ch)) {
				try {
					newUrlSb.append(URLEncoder.encode(String.valueOf(ch),
							encoding));
				} catch (UnsupportedEncodingException e) {
					newUrlSb.append(ch);
				}
			} else if (Character.SPACE_SEPARATOR == Character.getType(ch)) {
				newUrlSb.append("%");
				newUrlSb.append(Integer.toString(ch, 16));
			} else {
				newUrlSb.append(ch);
			}
		}
		return newUrlSb.toString();
	}

	public static String encodeUrl(String url, String encoding) {
		if (encoding == null)
			return url;
		StringBuilder newUrlSb = new StringBuilder();
		for (char ch : url.toCharArray()) {
			if (isEncodedChar(ch)) {
				try {
					newUrlSb.append(URLEncoder.encode(String.valueOf(ch),
							encoding));
				} catch (UnsupportedEncodingException e) {
					newUrlSb.append(ch);
				}
			} else {
				newUrlSb.append(ch);
			}

		}
		return newUrlSb.toString();
	}

	private static boolean isEncodedChar(char ch) {
		if (ch > 127)
			return true;
		if ('A' <= ch && ch <= 'Z')
			return false;
		if ('a' <= ch && ch <= 'z')
			return false;
		if ('0' <= ch && ch <= '9')
			return false;
		for (char unencodedChar : unencodedChars) {
			if (unencodedChar == ch)
				return false;
		}
		return true;
	}

	public static String replaceSpace(String str) {
		String replace160 = StringUtils.replaceChars(str, '?', ' '); // replace
																		// 160
																		// with
																		// 32
		return StringUtils.replaceChars(replace160, '　', ' '); // replace
																// fullwidth
																// space.
	}

	public static String getRegExpMatch(String tagText,
			String regularExpression, boolean match) throws ExtractException {
		if (regularExpression == null)
			return tagText;

		Pattern pattern = Pattern.compile(regularExpression, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(tagText);
		if (matcher.find()) {
			if (matcher.groupCount() > 0)
				return matcher.group(1);
			else
				throw new ExtractException(
						"The number of capturing groups is 0.");
		}
		if (match)
			throw new ExtractException(
					"Wrong regularExpression: can not find sring that matches the pattern. "
							+ tagText);
		else
			return tagText;
	}

	public static String getRegExpReplace(String tagText,
			String regularExpression, String replaceWith, String matchStrategy)
			throws ExtractException {
		boolean match = true;
		if (FALSE.equalsIgnoreCase(matchStrategy))
			match = false;
		if (replaceWith == null)
			return getRegExpMatch(tagText, regularExpression, match);
		if (regularExpression == null)
			return tagText;

		String replaceResult = tagText;
		boolean flag = false;
		if (regularExpression != null && replaceWith != null) {

			Pattern pattern = Pattern
					.compile(regularExpression, Pattern.DOTALL);
			Matcher matcher = pattern.matcher(tagText);
			if (matcher.find()) {
				Pattern p = Pattern.compile("\\\\(\\d)");
				Matcher m = p.matcher(replaceWith);
				while (m.find()) {
					flag = true;
					int replaceNum = Integer.parseInt(m.group(1));
					if (replaceNum <= matcher.groupCount()) {
						String reference = quoteReplacement(matcher
								.group(replaceNum));
						replaceResult = replaceWith.replaceAll("\\\\"
								+ replaceNum, reference);
						replaceWith = replaceResult;
					} else
						throw new ExtractException(
								"Back references number is greater than group count :"
										+ replaceWith);
				}
				// replaceWith doesn't contain back references
				if (!flag) {
					String replaceStr = matcher.group();
					replaceResult = tagText.replaceAll(replaceStr, replaceWith);
				}
			} else if (match)
				throw new ExtractException(
						"Wrong regularExpression: can not find the string that matches the pattern. "
								+ tagText);
		}
		return replaceResult;
	}

	public static boolean isBefore(Date date, int trackbackDays) {
		return date.before(DateUtils.addDays(new Date(), -trackbackDays));
	}

	/**
	 * 给定的日期是不是在今天之�?
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isAfterToday(Date date) {
		if (null == date)
			return false;

		Date today = new Date();
		if ((date.getTime() - today.getTime()) > 1000 * 60 * 60 * 24)
			return true;

		return false;
	}

	/**
	 * 给定的日期是不是在今天之�?
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isAfterNToday(Date date, int n) {
		// if(n<1) return false;
		if (null == date)
			return false;

		Date today = new Date();
		if ((date.getTime() - today.getTime()) > n * 1000 * 60 * 60 * 24L)
			return true;

		return false;
	}


	public static String formatLink(String text) {
		text = text.replace('\r', ' ');
		text = text.replace('\n', ' ');
		return StringUtils.trim(text);
	}

	public static String replaceWrongBr(String str) {
		return StringUtils.replace(str, "</br>", "<br />"); // replace incorret
															// br: </br> with
															// <br />
	}

	/**
	 * 由于String.replaceAll方法时如果替换字符中有\�?可能会出现indexOutOfBoundsException 固先对其他进行替�?
	 * 
	 * @param s
	 * @return
	 */
	private static String quoteReplacement(String s) {
		if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1))
			return s;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\\') {
				sb.append('\\');
				sb.append('\\');
			} else if (c == '$') {
				sb.append('\\');
				sb.append('$');
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 转换character-mapping.properties中指定的字符
	 * 
	 * @param s
	 * @return
	 */
	public static String convertCharactersByConfig(String s) {
		try {
			PropertiesConfiguration conf = new PropertiesConfiguration();
			conf.setDelimiterParsingDisabled(false);
			conf.setEncoding("UTF-8");
			conf.load(Utils.class
					.getResourceAsStream("/character-mapping.properties"));

			configuration = conf;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// 如果加载config文件失败，不做转�?
		if (null == configuration)
			return s;

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {// 逐字转换
			char c = s.charAt(i);
			String conv2String = configuration.getString(c + "");

			if (StringUtils.isNotBlank(conv2String)) {
				sb.append(conv2String);
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 把日期格式化成指定的格式
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String formatDate(Date date, String pattern) {
		if (null == date)
			return ERROR_EMPTY_DATE;

		return new SimpleDateFormat(pattern).format(date);
	}

	public static final String convertIntToString(int n) {
		return n == 0 ? FALSE : TRUE;
	}

	public static final String convertIntStrToBoolString(String n) {
		return n.equals(ZERO)? FALSE : TRUE;
	}

	/**
	 * 转换成yyyyMMdd格式的字符串
	 * 
	 * @param date
	 * @return
	 */
	public synchronized static final String convertDateToString(Date date) {
		String dateStr = null;
		DateFormat sdf = new SimpleDateFormat(DATE_FORMAT_LONG, Locale.US);
		DateFormat ndf = new SimpleDateFormat(DATE_FORMAT_SHORT);
		if (date != null) {
			try {
				dateStr = ndf.format(sdf.parse(date.toString()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return dateStr;
	}

	/**
	 * Get string of md5 code from str,the same to mysql's md5 function
	 * 
	 * @param str
	 * @return
	 */
	public static final String getMD5Str(String str) {
		return org.apache.commons.codec.digest.DigestUtils.md5Hex(str);
	}

	/**
	 * 获取服务器IP地址
	 * 
	 * @return
	 */
	public static String getRealIp() {

		StringBuilder localip = new StringBuilder();// 本地IP，如果没有配置外网IP则返回它
		String netip = null;// 外网IP
		Enumeration<NetworkInterface> netInterfaces = null;

		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress ip = null;
			boolean finded = false;// 是否找到外网IP

			while (netInterfaces.hasMoreElements() && !finded) {

				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> address = ni.getInetAddresses();

				while (address.hasMoreElements()) {
					ip = address.nextElement();
					if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
							&& ip.getHostAddress().indexOf(":") == -1) {// 外网IP
						netip = ip.getHostAddress();
						finded = true;
						break;
					} else if (ip.isSiteLocalAddress()
							&& !ip.isLoopbackAddress()
							&& ip.getHostAddress().indexOf(":") == -1) {// 内网IP
						localip.append(ip.getHostAddress() + ",");
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		if (netip != null && !"".equals(netip)) {
			return netip;
		} else {
			return localip.toString().substring(0,
					localip.toString().lastIndexOf(","));
		}
	}

    public static String dateDiffNum(Date startTime, Date endTime)
    {
        Calendar srcCal = Calendar.getInstance();
        srcCal.setTime(startTime);
        Calendar dstCal = Calendar.getInstance();
        dstCal.setTime(endTime);

        // 比较年月日
        int year = dstCal.get(Calendar.YEAR) - srcCal.get(Calendar.YEAR);
        int month = dstCal.get(Calendar.MONTH) - srcCal.get(Calendar.MONTH);
        int day = dstCal.get(Calendar.DAY_OF_MONTH)
                - srcCal.get(Calendar.DAY_OF_MONTH);

        if (endTime.getTime() < startTime.getTime())
        {
            return "error";
        }
        if (month < 0)
        {
            year -= 1;
            month += 12;
        }

        if (day < 0)
        {
            month -= 1;
            dstCal.add(Calendar.MONTH, -1);
            day += perMonthDays(dstCal);
        }

        String ages = (year > 0 ? year + "年" : "")
                + (month > 0 ? month + "个月" : "") + day + "天前";
        return ages;
    }

	/**
	 * 判断一个时间所在月有多少天
	 * 
	 * @param Calendar
	 *            具体时间的日历对�?
	 * @throws ParseException
	 */
	public static int perMonthDays(Calendar cal) {
		int maxDays = 0;
		int month = cal.get(Calendar.MONTH);

		switch (month) {
		case Calendar.JANUARY:
		case Calendar.MARCH:
		case Calendar.MAY:
		case Calendar.JULY:
		case Calendar.AUGUST:
		case Calendar.OCTOBER:
		case Calendar.DECEMBER:
			maxDays = 31;
			break;
		case Calendar.APRIL:
		case Calendar.JUNE:
		case Calendar.SEPTEMBER:
		case Calendar.NOVEMBER:
			maxDays = 30;
			break;
		case Calendar.FEBRUARY:
			if (isLeap(cal.get(Calendar.YEAR))) {
				maxDays = 29;
			} else {
				maxDays = 28;
			}
			break;
		}
		return maxDays;
	}

	/**
	 * 判断某年是否是闰�?
	 * 
	 * @param year
	 *            年份
	 * @throws ParseException
	 */
	public static boolean isLeap(int year) {
		boolean leap = false;
		if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
			leap = true;
		}
		return leap;
	}

	/**
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException {
		// 当前时间
		Date now = new Date();
		// 设置一个时�?012-2-18 8:14 （Java 日期中年份是0表示1900年，月份0表示一月）
		Date birthdy = new Date(2012 - 1900, 2 - 1, 18, 8, 14);
		// 计算日期差�?
		String ages = dateDiffNum(birthdy, now);
		System.out.println(ages);
	}


	public static String quotedStr(String str) {
		return '"'+str+'"';
	}

	public static String join(Collection collection, char c) {
		return StringUtils.join(collection, c);
	}

	public static boolean isNull(String str) {
		return (str == null || str.equals("")) == true ? true : false;
	}
	public static String getMiddleUrl(String regex, String url) {
		Pattern pattern = Pattern.compile(regex);
		Matcher m = pattern.matcher(url);
		if (m.find()) {
			return m.group(1);
		}
		return "";
	}
}
