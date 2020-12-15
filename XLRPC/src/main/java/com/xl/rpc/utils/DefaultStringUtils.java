package com.xl.rpc.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串操作工具类
 * 
 */
public class DefaultStringUtils extends org.apache.commons.lang.StringUtils {

	private static final Logger log = LoggerFactory.getLogger(DefaultStringUtils.class);

	public static byte[] toBytes(String s) {
		try {
			return s != null ? s.getBytes("UTF-8") : null;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String toStr(byte[] b) {
		try {
			return b != null ? new String(b, "UTF-8") : null;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取字符串的长度，中文占一个字符,英文数字占半个字符
	 * 
	 * @param value
	 *            指定的字符串
	 * @return 字符串的长度
	 */
	public static double strLength(String value) {
		double valueLength = 0;
		String chinese = "[\u4e00-\u9fa5]";
		// 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
		for (int i = 0; i < value.length(); i++) {
			// 获取一个字符
			String temp = value.substring(i, i + 1);
			// 判断是否为中文字符
			if (temp.matches(chinese)) {
				// 中文字符长度为1
				valueLength += 1;
			} else {
				// 其他字符长度为0.5
				valueLength += 0.5;
			}
		}
		// 进位取整
		return Math.ceil(valueLength);
	}

	/**
	 * 字符串连接
	 * 
	 * @param strs
	 * @return
	 */
	public static String contactString(String... strs) {
		StringBuilder sb = new StringBuilder();
		for (String str : strs) {
			sb.append(str);
		}
		return sb.toString();
	}

	/**
	 * 字符串连接
	 * 
	 * @param strs
	 * @return
	 */
	public static String concatenateString2(String sep, String... strs) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strs.length; i++) {
			if (strs[i] == null) {
				continue;
			}
			sb.append(strs[i]);
			if (i != strs.length - 1) {
				sb.append(sep);
			}
		}
		return sb.toString();
	}

	public static String asciiReverse(String data) {
		if (data == null)
			return null;

		int len = data.length();

		if (len == 0)
			return data;

		StringBuilder str_buf = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char in = data.charAt(i);

			if (in > 0 && in < 128) {
				in = (char) (127 - in);
			}
			str_buf.append(in);
		}

		return str_buf.toString();
	}

	/**
	 * 按指定长度裁减字符串用于显示（英文字符1,中文字符2） note:
	 * 函数对中文做了特殊处理，因此如果输入中含有中文，应该调用此函数，否则，直接通过String的length操作判断
	 * 
	 * @param str
	 * @param splitLen
	 * @return
	 */
	public static String splitDisplayString(String str, int splitLen) {
		if (str == null)
			return null;

		char[] result = new char[splitLen];
		int dislpayLen = 0;
		int j = 0;
		int len = str.length();
		for (int i = 0; i < len; ++i) {
			char ch = str.charAt(i);
			if (ch > '\u00FF')
				dislpayLen = dislpayLen + 2;
			else
				dislpayLen++;
			if (dislpayLen <= splitLen)
				result[j++] = ch;
			else
				break;
		}
		return new String(result, 0, j);
	}

	/**
	 * 第一个参数，传入的是要截的中英文字符串，第二个参数，要截取的长度。 /**
	 * 截取字符串(汉字按两个字符算),如果截取的长度刚好是汉字的中间,那舍弃这个汉字
	 * 这个规则和subStringWithChinese方法的规则不一样,所以没有重复
	 * 
	 * @param str
	 * @param subBytes
	 * @return str
	 */
	public static String subStringWithChinese(String str, int subBytes) {
		if (DefaultStringUtils.isNotEmpty(str)) {
			// int byteLen = str.length();
			int bytes = 0; // 用来存储字符串的总字节数
			for (int i = 0; i < str.length(); i++) {
				if (bytes == subBytes) {
					return str.substring(0, i);
				}
				char c = str.charAt(i);
				if (c < 256) {
					bytes += 1; // 英文字符的字节数看作1
				} else {
					bytes += 2; // 中文字符的字节数看作2
					if (bytes - subBytes == 1) {
						return str.substring(0, i);
					}
				}
			}
		}
		return str;
	}

	/**
	 * 汉字当1位
	 * 
	 * @param str
	 * @param beginIndex
	 * @param endIndex
	 * @return
	 */
	public static String subStringWithChinese(String str, int beginIndex, int endIndex) {
		if (endIndex <= beginIndex)
			return null;

		int bytesLen = str.length();
		String subStr = null;
		int bytes = 0; // 用来存储字符串的总字节数
		for (int i = 0; i < str.length(); i++) {
			if (bytes == beginIndex) {
				subStr = str.substring(i, bytesLen);
				bytes = i;
				break;
			}
			char c = str.charAt(i);
			if (c < 256) {
				bytes += 1; // 英文字符的字节数看作1
			} else {
				bytes += 2; // 中文字符的字节数看作2
				if (bytes - beginIndex == 1) {
					subStr = str.substring(i, bytesLen);
					bytes = i;
					break;
				}
			}
		}

		String retStr = subStringWithChinese(subStr, endIndex - bytes);
		return retStr;
	}

	/**
	 * 计算字符串字节数（英文字符1,中文字符2） note:
	 * 函数对中文做了特殊处理，因此如果输入中含有中文，应该调用此函数，否则，直接通过String的length操作判断
	 * 
	 * @see #getStrLength(String)
	 * @deprecated
	 */
	public static int displayLength(String str) {
		return getStrLength(str);
	}

	/**
	 * 验证一个可能含有中文的字符串长度是否在指定的长度范围内 note:
	 * 函数对中文做了特殊处理，因此如果输入中含有中文，应该调用此函数，否则，直接通过String的length操作判断
	 * 
	 * @param str
	 * @param maxLength
	 * @return 如果大于最大值，则返回false，否则返回true
	 */
	public static boolean validateMaxLength(String str, int maxLength) {
		int length = str.length();

		// 只有字符串在最大长度和最大长度的1/2之间才去获取实际长度
		if (length <= maxLength && length > maxLength / 2) {
			return !(DefaultStringUtils.displayLength(str) > maxLength);
		}
		return (length <= maxLength);
	}

	/**
	 * 功能：获取字符串长度，一个汉字等于两个字符
	 * 
	 * @param str
	 *            字符串
	 * @return 字符串长度
	 */
	public static int getStrLength(String str) {
		if (str == null || str.length() == 0)
			return 0;
		int count = 0;
		for (char c : str.toCharArray()) {
			count += c < 256 ? 1 : 2;
		}
		return count;
		// int bytes = 0; // 用来存储字符串的总字节数
		// for (int i = 0; i < str.length(); i++) {
		// char c = str.charAt(i);
		// if (c < 256) {
		// bytes += 1; // 英文字符的字节数看作1
		// } else {
		// bytes += 2; // 中文字符的字节数看作2
		// }
		// }
		//
		// return bytes;
	}

	/**
	 * 验证一个可能含有中文的字符串长度是否在指定的长度范围内 note:
	 * 函数对中文做了特殊处理，因此如果输入中含有中文，应该调用此函数，否则，直接通过String的length操作判断
	 * 
	 * @param str
	 * @param minLength
	 * @return 如果小于最小值，则返回false，否则返回true
	 */
	public static boolean validateMinLength(String str, int minLength) {
		int length = str.length();

		if (length >= minLength)
			return true;
		// 只有字符串在最大长度和最大长度的1/2之间才去获取实际长度
		if (length < minLength && length >= minLength / 2) {
			return !(DefaultStringUtils.displayLength(str) < minLength);
		}
		return (length > minLength);
	}

	/**
	 * 截取字符串(汉字按两个字符算),如果截取的长度刚好是汉字的中间,那舍弃这个汉字
	 * 这个规则和subStringWithChinese方法的规则不一样,所以没有重复
	 * 
	 * @param s
	 *            要截取的字符串,必须是gbk的编码
	 * @param length
	 *            要截取的长度
	 * @return 截取后的字符串
	 */
	public static String subString(String s, int length) {
		try {
			byte[] bytes = s.getBytes("gbk");
			if (length >= bytes.length) {
				return s;
			}
			// 如果截取的字符串的汉字字节个数是奇数个,就舍弃最后一个
			byte[] afterBytes = new byte[length];
			System.arraycopy(bytes, 0, afterBytes, 0, length);
			int count = 0;
			for (byte b : afterBytes) {
				if (b < 0) {
					count++;
				}
			}
			String returnStr = new String(afterBytes, 0, afterBytes.length, "gbk");
			if (count % 2 != 0) {
				returnStr = new String(afterBytes, 0, afterBytes.length - 1, "gbk");
			}
			return returnStr;
		} catch (Exception e) {
			log.error("字符截取失败", e);
			return s;
		}
	}

	public static String subStringWithSuffix(String s, int length, String suffix) {
		try {
			if (DefaultStringUtils.isEmpty(s)) {
				return "";
			}
			byte[] bytes = s.getBytes("gbk");
			if (length >= bytes.length) {
				return s;
			}
			return subString(s, length) + suffix;
		} catch (Exception e) {
			log.error("字符截取失败", e);
			return s;
		}
	}
	
	public static String subStringBefore(int start, String s, String pos) {
		int index = s.indexOf(pos);
		if ( index < 0 )
			return s;
		return s.substring(start, index);
	}

	/*
	 * 截取转译之后的字符串，避免截取到转译出乱码，截取了无效链接，截取了@
	 */
	public static String subStringEscapeMessycode(String str, int length) {
		try {
			if (getStrLength(str) > length) {
				String content = subStringWithChinese(str, length);
				if (content.matches(".*http://[0-9a-zA-Z#\\./\\?&=-]*$")) {// 处理无效链接
					content = content.substring(0, content.lastIndexOf("http://"));
				} else if (content.matches(".*&[0-9a-zA-Z#]{0,6}$")) {// 处理转译乱码
					content = content.substring(0, content.lastIndexOf("&"));
				} else if (content.matches(".*@[\\u4e00-\\u9fa5\\w-]+$")) {// 处理无效@
					content = content.substring(0, content.lastIndexOf("@"));
				}
				return content;
			} else {
				return str;
			}
		} catch (Exception e) {
			log.info("subStringEscapeMessycode failed,the string is:" + str + ";length is" + length);
			return str;
		}
	}

	/**
	 * 去掉前后空格
	 * 
	 * @param content
	 * @return
	 */
	public static String trim(String content) {
		if (null != content) {
			return content.trim();
		}
		return content;
	}

	/**
	 * 按字节长度截取字符串
	 * 
	 * @param str
	 *            将要截取的字符串参数
	 * @param toCount
	 *            截取的字节长度
	 * @param more
	 *            字符串末尾补上的字符串
	 * @return 返回截取后的字符串
	 */
	public static String substring(String src, int length, String more, boolean withEmoji) {
		if (DefaultStringUtils.isEmpty(src)) {
			return "";
		}
		double strLength = strLength(src);
		if (strLength <= length) {
			return src;
		} 
		else {

			int emojiLen = 0;
			int dislpayLen = 0;

			int i=0;
			for (; i < src.length(); ++i) {
				char ch = src.charAt(i);
				
				//check emoji char
				if ( emojiLen > 7 ){
					dislpayLen += emojiLen;
					emojiLen = 0;
				}
				if ( emojiLen > 0 ){
					if ( ch == '}' ){
						dislpayLen ++;
						emojiLen = 0;
						continue;
					}
					
					emojiLen ++;
					continue;
				}
				
				if ( withEmoji && ch == '{' ){
					emojiLen = 1;
					continue;
				}
				//end emojichar
				
				if ( ++dislpayLen >  length )
					break;
				
			}
			String data = src.substring(0, i);
			if ( i < src.length() )
				data  += more;
			return data;
		}
	}

	/**
	 * 将分割字符串转为 Set<Long> 结构,
	 * 
	 * @param str
	 * @return 始终有返回值
	 */
	public static Set<Long> commaStringToSet(String str, String sep) {
		String[] arr = str.split(sep);
		Set<Long> set = new HashSet<Long>();
		for (String s : arr) {
			set.add(Long.parseLong(s));
		}
		return set;
	}

	/**
	 * 将分割字符串转为 Set<Long> 结构,
	 * 
	 * @param str
	 * @return 始终有返回值
	 */
	public static Set<String> commaStringToStringSet(String str, String sep) {
		String[] arr = str.split(sep);
		Set<String> set = new HashSet<String>();
		for (String s : arr) {
			set.add(s);
		}
		return set;
	}
	
	/**
	 * 
	 * @param set
	 * @param sep
	 * @return
	 */
	public static String SetToCommaString(Set<Long> set, String sep) {
		StringBuilder bld = new StringBuilder();
		for (Long ss : set) {
			bld.append(String.valueOf(ss));
			bld.append(sep);
		}
		return bld.toString();
	}

	public static String getStringFromCollection(Collection<?> colls, String sep) {
		StringBuilder sb = new StringBuilder();

		for (Iterator<?> it = colls.iterator(); it.hasNext();) {
			Object o = it.next();
			sb.append(o);
			sb.append(sep);
		}
		return sb.toString();
	}

	public static String toString(Object obj) {
		if (obj == null)
			return "null";
		else
			return obj.toString();
	}

	public static long parseLong(String s) {
		long i = 0L;
		try {
			i = Long.parseLong(s);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return i;
	}

	public static int parseInt(String s) {
		int i = 0;
		try {
			i = Integer.parseInt(s);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return i;
	}

	 public static int parseInt(String s, int defaultValue) {
	    int i = defaultValue;
	    try {
	      i = Integer.parseInt(s);
	    } catch (Exception ex) {
	      ex.printStackTrace();
	    }
	    return i;
	  }
	
	public static boolean checkEmail(String email) {
		boolean flag = false;
		try {
			String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(email);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 验证手机号码
	 * 
	 * @param mobiles
	 * @return
	 */
	public static boolean checkMobileNumber(String mobileNumber) {
		boolean flag = false;
		try {
			Pattern regex = Pattern
					.compile("^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
			Matcher matcher = regex.matcher(mobileNumber);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	public static void main(String a[]) {
		System.out.println(DefaultStringUtils.strLength("qweqwe第三方123qwe"));
		
		String s = "{23}{333}{22}我发我发我发我g";
		
		String data = DefaultStringUtils.substring(s, 10, ".....", true);
		
		System.out.println(data);
	}
}