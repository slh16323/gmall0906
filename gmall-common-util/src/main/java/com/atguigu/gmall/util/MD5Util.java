package com.atguigu.gmall.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    
    /**
     * 生成随机验证码的工具方法
     * @param length：验证码的位数
     * @return
     */
    public static String randomCode(int length) {
        
        StringBuilder builder = new StringBuilder();
        
        for(int i = 0; i <length; i++) {
            
            double random = Math.random();
            
            int code = (int) (random * 10);
            
            builder.append(code);
        }
        
        return builder.toString();
    }
    
    /**
     * 检查字符串是否有效
     * @param source 被检查的字符串
     * @return
     *  true：有效
     *  false：无效
     */
    public static boolean strCheck(String source) {
        
        return source != null && source.length() > 0;
    }
    
    /**
     * 进行MD5加密的工具方法
     * @param source 明文
     * @return 密文
     * @exception 在传入的字符串无效时，抛出异常
     */
    public static String md5(String source) throws RuntimeException {
        
        //1.对传入的source字符串进行检查
        if(!strCheck(source)) {
            
            //2.如果检测到源字符串不可用，则抛出异常通知方法调用者
            throw new RuntimeException("字符串不可用");
        }
        
        //3.声明变量指定算法名称
        String algorithm = "md5";
        
        try {
            //4.获取MessageDigest实例对象
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            
            //5.获取源字符串的字节数组
            byte[] sourceBytes = source.getBytes();
            
            //6.执行加密
            byte[] targetBytes = messageDigest.digest(sourceBytes);
            
            //7.创建StringBuilder对象
            StringBuilder builder = new StringBuilder();
            
            //8.创建字符数组
            char[] characters = new char[] {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
            
            //9.遍历targetBytes
            for(int i = 0; i < targetBytes.length; i++) {
                
                //10.获取当前字节数
                byte currentByte = targetBytes[i];
                
                //11.通过和15做与运算获取当前字节的低四位
                int lowValue = currentByte & 15;
                
                //12.通过讲当前字节右移，然后和15做与运算获取高四位值
                int highValue = (currentByte >> 4) & 15;
                
                //13.从characters数组中获取对应的字符
                char lowChar = characters[lowValue];
                char highChar = characters[highValue];
                
                //14.拼字符串
                builder.append(highChar).append(lowChar);
                
            }
            
            //15.返回处理结果
            return builder.toString();
            
        } catch (NoSuchAlgorithmException e) {}
        
        return null;
    }

}
