package com.example.shelltool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.Adler32;

public class shellTool {
    public static void main(String []args){
        System.out.println("hello shellTool");
        System.out.println("current dir: " + System.getProperty("user.dir"));
        try {
            File payloadSrcFile = new File("signature_1.0-debug.apk");
            System.out.println("apk size: " + payloadSrcFile.length());
            File unShellDexFile = new File("unshellapp.dex");
            byte []payloadArray = encrpt(readFileBytes(payloadSrcFile));
            byte []unShellDexArray = readFileBytes(unShellDexFile);
            int payloadLen = payloadArray.length;
            int unShellDexLen = unShellDexArray.length;
            int totalLen = payloadLen + unShellDexLen + 4;
            byte []newdex = new byte[totalLen];
            System.arraycopy(unShellDexArray, 0,  newdex,0, unShellDexLen);
            System.arraycopy(payloadArray, 0 , newdex, unShellDexLen, payloadLen);
            System.out.println("len: " + Arrays.toString(intToByteArray(payloadLen)));
            System.arraycopy(encrpt(intToByteArray(payloadLen)), 0, newdex, totalLen-4, 4);
            fixFileSizeHeader(newdex);
            fixSHA1Header(newdex);
            fixCheckSumHeader(newdex);

            String str = "classes.dex";
            File file = new File(str);
            if (!file.exists()){
                file.createNewFile();
            }
            FileOutputStream localFileOutputStream = new FileOutputStream(str);
            localFileOutputStream.write(newdex);
            localFileOutputStream.flush();
            localFileOutputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public static byte[] intToByte(int number) {
        byte[] b = new byte[4];
        for (int i = 3; i >= 0; i--) {
            b[i] = (byte) (number % 256);
            // 右移1字节
            number >>= 8;
        }
        return b;
    }

    private static byte[] encrpt(byte[] srcdata){
        for(int i=0; i < srcdata.length; i++){
            srcdata[i] = (byte)(srcdata[i] ^ 0xFF);
        }
        return srcdata;
    }

    private static byte[] readFileBytes(File file){
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return fileContent;
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private static void fixCheckSumHeader(byte[] dexBytes) {
        Adler32 adler = new Adler32();
        adler.update(dexBytes, 12, dexBytes.length - 12);
        long value = adler.getValue();
        int va = (int) value;
        byte[] newcs = intToByteArray(va);
        byte[] recs = new byte[4];
        for (int i = 0; i < 4; i++) {
            recs[i] = newcs[newcs.length - 1 - i];
            System.out.println(Integer.toHexString(newcs[i]));
        }
        System.arraycopy(recs, 0, dexBytes, 8, 4);
        System.out.println(Long.toHexString(value));
        System.out.println();
    }

    private static void fixSHA1Header(byte[] dexBytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(dexBytes, 32, dexBytes.length - 32);
        byte[] newdt = md.digest();
        System.arraycopy(newdt, 0, dexBytes, 12, 20);
        String hexstr = "";
        for (int i = 0; i < newdt.length; i++) {
            hexstr += Integer.toString((newdt[i] & 0xff) + 0x100, 16).substring(1);
        }
        System.out.println(hexstr);
    }
    private static void fixFileSizeHeader(byte[] dexBytes) {

        byte[] newfs = intToByteArray(dexBytes.length);
        System.out.println(Integer.toHexString(dexBytes.length));
        byte[] refs = new byte[4];
        for (int i = 0; i < 4; i++) {
            refs[i] = newfs[newfs.length - 1 - i];
            System.out.println(Integer.toHexString(newfs[i]));
        }
        System.arraycopy(refs, 0, dexBytes, 32, 4);
    }
}
