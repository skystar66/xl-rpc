package com.xl.rpc.utils;


import org.apache.commons.lang.StringEscapeUtils;

public class HTMLUtils {

  /**
   * HTML 转义，防script注入
   * @param s
   * @return
   */
  public static String htmlTagTransfer(String s) {
    if (DefaultStringUtils.isEmpty(s)) {
      return s;
    }

    s = s.replace("&", "&amp;");
    s = s.replace("<", "&lt;");
    s = s.replace(">", "&gt;");
    s = s.replace("\"", "&quot;");
    // return StringEscapeUtils.escapeHtml(s);
    return s;
  }

  public static void main(String[] args) {
    String s = StringEscapeUtils.escapeHtml("<script>alert('hello中');</script>");
    System.out.println(s);
    System.out.println(StringEscapeUtils.unescapeHtml(s));
  }
}
