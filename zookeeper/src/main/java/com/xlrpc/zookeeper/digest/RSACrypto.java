package com.xlrpc.zookeeper.digest;


import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;

//import org.bouncycastle.asn1.ASN1Sequence;

//import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;

/**加密算法*/
public class RSACrypto {

  /** */
  /**
   * 加密算法RSA
   */
  public static final String KEY_ALGORITHM = "RSA";

  /** */
  /**
   * RSA最大加密明文大小
   */
  private static final int MAX_ENCRYPT_BLOCK = 117;

  /** */
  /**
   * RSA最大解密密文大小
   */
  private static final int MAX_DECRYPT_BLOCK = 128;
  /**
   * 签名算法
   */
  private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

  /**
   * 公钥
   */
  private static final String PUBLICKEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDI29uhJc74WmjP291uOgp12aN5NiAiyAsDjOWx0zYwUY7Kok+j7ZaBA5vYd3fWhgf2/iwz0WwtM+S9XWcpTyoUAIj/bQ41G9e4kOwE6ekS6Ub1RSLnls40SSglbBF0JQf3W77WQbiO1v7CHIIECUSTKOXQdWZMWwyvr+U7mThgFQIDAQAB";

  static final Properties PROPERTIES = new Properties(System.getProperties());

  /** */
  /**
   * <p>
   * 公钥加密
   * </p>
   *
   * @param message
   *          源数据
   * @param publicKeyFile
   *          公钥文件路径(公钥内容BASE64编码)
   * @return 加密后数据（BASE64编码）
   * @throws Exception
   */
  public static String encryptByPublicKey(String message, String publicKeyFile) throws Exception {
    File file = new File(publicKeyFile);
    if (file.exists() == false) {
      return "";
    }

    String publicKey = "";
    String sign = "";
    String fcontent = "";

    Long flength = file.length();
    FileInputStream stream = null;

    try {
      stream = new FileInputStream(file);
      byte[] buffer = new byte[flength.intValue()];
      stream.read(buffer);
      fcontent = new String(buffer, "UTF-8");
    } finally {
      if (stream != null) {
        stream.close();
      }
    }

    /*
     * String[] strs = fcontent.split(PROPERTIES.getProperty("line.separator"));
     * if(strs != null && strs.length > 1)
     * {
     * sign = strs[0];
     * for(int i=1; i<strs.length-1; i++)
     * {
     * publicKey += strs[i];
     * publicKey += PROPERTIES.getProperty("line.separator");
     * }
     * publicKey += strs[strs.length-1];
     * if(!publicKey.endsWith("\n")){
     * publicKey += "\n";
     * }
     * }
     */
    int index = fcontent.indexOf("-----BEGIN");
    if (index >= 0) {
      sign = fcontent.substring(0, index);
      publicKey = fcontent.substring(index);
    }

    if (sign == null || "".equals(sign) || publicKey == null || "".equals(publicKey) ) {
      return "";
    }

    if (verify(publicKey, sign, PUBLICKEY) == false) {
      return "";
    }

    publicKey = publicKey.replaceAll("-----BEGIN PUBLIC KEY-----", "").replaceAll(
        "-----END PUBLIC KEY-----", "").replaceAll("-----BEGIN RSA PUBLIC KEY-----", "").replaceAll(
            "-----END RSA PUBLIC KEY-----", "").replace("\n", "").trim();

    byte[] keyBytes =  Base64.getDecoder().decode(publicKey);
    X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    Key publicK = keyFactory.generatePublic(x509KeySpec);
    // 对数据加密
    Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
    cipher.init(Cipher.ENCRYPT_MODE, publicK);
    byte[] data = message.getBytes("UTF-8");
    int inputLen = data.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int offSet = 0;
    byte[] cache;
    int i = 0;
    // 对数据分段加密
    while (inputLen - offSet > 0) {
      if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
        cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
      } else {
        cache = cipher.doFinal(data, offSet, inputLen - offSet);
      }
      out.write(cache, 0, cache.length);
      i++;
      offSet = i * MAX_ENCRYPT_BLOCK;
    }
    byte[] encryptedData = out.toByteArray();
    out.close();
    return Base64.getEncoder().encodeToString(encryptedData);
  }

  /** */
  /**
   * <P>
   * 私钥解密
   * </p>
   *
   * @param message
   *          已加密数据(BASE64编码)
   * @param privateKeyFile
   *          私钥文件路径(私钥内容BASE64编码)
   * @return 解密数据
   * @throws Exception
   */
  public static String decryptByPrivateKey(String message, String privateKeyFile) throws Exception {
    File file = new File(privateKeyFile);
    if (file.exists() == false) {
      return "";
    }

    String privateKey = "";
    String sign = "";
    String fcontent = "";

    Long flength = file.length();
    FileInputStream stream = null;

    try {
      stream = new FileInputStream(file);
      byte[] buffer = new byte[flength.intValue()];
      stream.read(buffer);
      fcontent = new String(buffer, "UTF-8");
    } finally {
      if (stream != null) {
        stream.close();
      }
    }

    /*
     * String[] strs = fcontent.split(PROPERTIES.getProperty("line.separator"));
     * if(strs != null && strs.length > 1)
     * {
     * sign = strs[0];
     * for(int i=1; i<strs.length-1; i++)
     * {
     * privateKey += strs[i];
     * privateKey += PROPERTIES.getProperty("line.separator");
     * }
     * privateKey += strs[strs.length-1];
     * if(!privateKey.endsWith("\n")){
     * privateKey += "\n";
     * }
     * }
     */
    int index = fcontent.indexOf("-----BEGIN");
    if (index >= 0) {
      sign = fcontent.substring(0, index);
      privateKey = fcontent.substring(index);
    }

    if (sign == null || "".equals(sign) || privateKey == null || "".equals(privateKey) ) {
      return "";
    }

    if (verify(privateKey, sign, PUBLICKEY) == false) {
      return "";
    }

    privateKey = privateKey.replaceAll("-----BEGIN RSA PRIVATE KEY-----", "").replaceAll(
        "-----END RSA PRIVATE KEY-----", "").replaceAll("-----BEGIN PRIVATE KEY-----", "")
        .replaceAll("-----END PRIVATE KEY-----", "").replaceAll("\n", "").trim();
    byte[] keyBytes = Base64.getDecoder().decode(privateKey);
    // RSAPrivateKeyStructure asn1PrivKey = new
    // RSAPrivateKeyStructure((ASN1Sequence)
    // ASN1Sequence.fromByteArray(keyBytes));
    // RSAPrivateKeySpec rsaPrivKeySpec = new
    // RSAPrivateKeySpec(asn1PrivKey.getModulus(),
    // asn1PrivKey.getPrivateExponent());

    // byte[] keyBytes = new BASE64Decoder().decodeBuffer(privateKey);
    PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
    Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
    cipher.init(Cipher.DECRYPT_MODE, privateK);
    byte[] encryptedData =    Base64.getDecoder().decode(message);
    int inputLen = encryptedData.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int offSet = 0;
    byte[] cache;
    int i = 0;
    // 对数据分段解密
    while (inputLen - offSet > 0) {
      if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
        cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
      } else {
        cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
      }
      out.write(cache, 0, cache.length);
      i++;
      offSet = i * MAX_DECRYPT_BLOCK;
    }
    byte[] decryptedData = out.toByteArray();
    out.close();
    return new String(decryptedData, "UTF-8");
  }

  /** */
  /**
   * <P>
   * 验证签名
   * </p>
   *
   * @param content
   *          原值
   * @param sign
   *          签名值
   * @param publicKey
   *          公钥
   * @return 验证是否成功
   * @throws Exception
   */
  public static boolean verify(String content, String sign, String publicKey) {
    try {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] keyBytes = decoder.decode(publicKey);
      X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
      PublicKey publicK = keyFactory.generatePublic(x509KeySpec);

      java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

      signature.initVerify(publicK);
      signature.update(content.getBytes("UTF-8"));

      boolean bverify = signature.verify(Base64.getDecoder().decode(sign));
      return bverify;

    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  /** */
  /**
   * <P>
   * 验证签名
   * </p>
   *
   * @param content
   *          原值
   * @param sign
   *          签名值
   * @param publicKey
   *          公钥
   * @return 验证是否成功
   * @throws Exception
   */
  public static boolean verify(String publicKeyFile) {
    try {
      File file = new File(publicKeyFile);
      if (file.exists() == false) {
        return false;
      }

      String publicKey = "";
      String sign = "";
      String fcontent = "";

      Long flength = file.length();
      FileInputStream stream = null;

      try {
        stream = new FileInputStream(file);
        byte[] buffer = new byte[flength.intValue()];
        stream.read(buffer);
        fcontent = new String(buffer, "UTF-8");
      } finally {
        if (stream != null) {
          stream.close();
        }
      }

      int index = fcontent.indexOf("-----BEGIN");
      if (index >= 0) {
        sign = fcontent.substring(0, index);
        publicKey = fcontent.substring(index);
      }

      if (sign == null || "".equals(sign) || publicKey == null || "".equals(publicKey)) {
        return false;
      }


      byte[] keyBytes = Base64.getDecoder().decode(PUBLICKEY);
      X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
      PublicKey publicK = keyFactory.generatePublic(x509KeySpec);

      java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

      signature.initVerify(publicK);
      signature.update(publicKey.getBytes("UTF-8"));

      boolean bverify = signature.verify(Base64.getDecoder().decode(sign));
      return bverify;

    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

}
