package com.licitacion.utils;

import android.util.Base64;

import java.lang.reflect.Field;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encript {

    public Encript() {
    }

    public static String encrypt(String value, String llave) {
        try {
            String ex = "CaxrqTOzLqBoxQn9";
            SecretKeySpec skeySpec = new SecretKeySpec(llave.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            removeCryptographyRestrictions();
            cipher.init(1, skeySpec, new IvParameterSpec(ex.getBytes()));
            byte[] encrypted = cipher.doFinal(value.getBytes());
            String var1 = Base64.encodeToString(encrypted, 0);
            var1 = var1.replace(" ", "");
            var1 = var1.replace("\n", "");
            //System.out.println("ENCRIPTANDo...." + var1);
            return var1;
        } catch (Exception var7) {
            var7.printStackTrace();
            return null;
        }
    }

    private static String method(String str) {
        if (str != null && str.length() > 0 && str.charAt(str.length()-1) == '=') {
            str = str.substring(0, str.length()-1);
            str = str.substring(0, str.length()-2);
        } else
            str = "---> "+str.charAt(str.length()-2);
        return str;
    }

    public static String decrypt(String encrypted, String llave) {
        try {
            String ex = "CaxrqTOzLqBoxQn9";
            SecretKeySpec skeySpec = new SecretKeySpec(llave.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            removeCryptographyRestrictions();
            cipher.init(2, skeySpec, new IvParameterSpec(ex.getBytes()));
            byte[] original = cipher.doFinal(Base64.decode(encrypted.getBytes(), 0));
            //System.out.println("desencript... "+encrypted+"---> "+ new String(original));
            return new String(original);
        } catch (Exception var6) {
            var6.printStackTrace();
            return "";
        }
    }

    private static void removeCryptographyRestrictions() {
        if(isRestrictedCryptography()) {
            try {
                Field e;
                try {
                    Class e1 = Class.forName("javax.crypto.JceSecurity");
                    e = e1.getDeclaredField("isRestricted");
                } catch (ClassNotFoundException var4) {
                    try {
                        Class e2 = Class.forName("javax.crypto.SunJCE_b");
                        e = e2.getDeclaredField("g");
                    } catch (ClassNotFoundException var3) {
                        throw var4;
                    }
                }

                e.setAccessible(true);
                e.set((Object)null, Boolean.valueOf(false));
            } catch (Throwable var5) {
                System.out.println("Error");
            }

        }
    }

    private static boolean isRestrictedCryptography() {
        return "Java(TM) SE Runtime Environment".equals(System.getProperty("java.runtime.name"));
    }
}
