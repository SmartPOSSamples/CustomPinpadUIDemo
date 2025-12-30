package com.wizarpos.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import com.cloudpos.jniinterface.HSMInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pengli on 16-11-8.
 */

public class SystemUtils {

    public static final String TAG = SystemUtils.class.getSimpleName();
    public static final String LOGO_CUSTOM_TONGLIAN_SH = "TONGLIANSH";
    public static final String LOGO_CUSTOM_MINSHNEG = "MINSHENG";
    public static final String LOGO_CUSTOM_AISINO = "AISINO";
    public static final String LOGO_CUSTOM_BIZZPOS = "BIZZPOS";
    public static final String LOGO_CUSTOM_ELGIN = "ELGIN";
    public static final String LOGO_CUSTOM_SPAY = "SPAY";

    public static final String FILENAME_PROC_VERSION = "/proc/version";
    public static final String IN_SDCARD = "/storage/insdcard";
    public static final String MODEL_WIZARPOS  = "WIZARPOS 1";
    public static final String MODEL_WIZARPOS1  = "WIZARPOS_1";
    public static final String MODEL_WIZARPAD1  = "WIZARPAD_1";
    public static final String MODEL_WIZARHANDQ1  = "WIZARHAND_Q1";
    public static final String MODEL_WIZARHANDH0  = "WIZARHAND_M0";
    public static final String MODEL_WIZARHANDQD4  = "QD4";
    public static final String CUSTOM_SN = "wp.customer.serialno";
    private static Object setRecoveryFlagByClass(){
        Object eepromNativeResult = null ;
        try {
//                                                                              com.wizarpos.android.core.util.EepromNative
            Class<?> eepromNative = Class.forName("com.wizarpos.android.core.util.EepromNative");
            Logger.debug( "1 " + eepromNative.toString());
            eepromNativeResult = eepromNative.getMethod("setRecoveryFlag", new Class[]{byte.class}).invoke(eepromNative, (byte)0x02);   //return byte
            Logger.debug("3 " +eepromNativeResult.toString());
        }catch (Exception e) {
            Logger.error( "i can't find this method for eepromnative");
            e.printStackTrace();
            eepromNativeResult = null;
        }
        return eepromNativeResult;
    }

    public static String getDefalutTusn(){
        if(SystemUtils.isLogoAisino()){
            return "000014" + "04" + SystemUtils.getCustomSN() ;
        }
        return "000016" + "04" + Build.SERIAL;
    }

    public static void rebootSystemForRepair(Context mContext){
        Object objResult =  setRecoveryFlagByClass();
        if(objResult != null){
            try{
                PowerManager pManager=(PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                pManager.reboot("recovery");
            }catch(Exception e){
                e.printStackTrace();
                Logger.error( "need system signed");
            }finally{
            }
        }
    }

    public static String getCustomSN(){
        String customSN = getProperty(CUSTOM_SN, "");
        Log.d(TAG, "customSN is " + customSN);
        return customSN;
    }


    public static boolean isModelWizarposV2(){
        int currentapiVersion = Build.VERSION.SDK_INT;
        String model = getSystemModel().trim();
        if ((model.equals("WIZARPOS 1") || model.equals("WIZARPOS_1")) && currentapiVersion >= 21) {
            return true;
        }
        return false;
    }
    public static boolean isModelWizarpos(){
        String model = getSystemModel().trim();
        int currentapiVersion = Build.VERSION.SDK_INT;
        if ((model.equals("WIZARPOS 1") || model.equals("WIZARPOS_1")) && currentapiVersion <= 19)  {
            return true;
        }
        return false;
    }

    public static boolean isRma(){
//        ro.wp.rma = 1
        String value = getProperty("ro.wp.rma","").trim();
        if("1".equalsIgnoreCase(value)){
            return true;
        }
        return false;
    }

    public static boolean isModelM0(){
        String model = getSystemModel();
        return model.equals("WIZARHAND_M0");
    }

    public static boolean isModelWizarpad(){
        String model = getSystemModel();
        return model.equals("WIZARPAD_1");
    }
    public static boolean isModelQ1(){
        String model = getSystemModel();
        return model.equals("WIZARHAND_Q1");
    }

    public static boolean isModelQ1v2(){
        String model = getSystemModelByNew();
        return model.equals("Q1V2");
    }
    public static boolean isModelQ2(){
        String model = getSystemModelByNew();//
        return model.equals("Q2");
    }

    public static boolean isModelQ2a7(){
        String model = getSystemModelByNew();//
        return model.equalsIgnoreCase("Q2A7");
    }

    public static boolean isModelQ3a7(){
        String model = getSystemModelByNew();
        return model.equalsIgnoreCase("Q3A7");
    }
    public static boolean isModelQD4(){
//        ro.wp.product.submodel
        String model = getProperty("ro.wp.product.submodel","").trim().toUpperCase();
        return model.equals("QD4");
    }
    public static boolean isModelQD5(){
//        ro.wp.product.submodel
        String model = getProperty("ro.wp.product.submodel","").trim().toUpperCase();
        return model.equals("QD5");
    }
    public static boolean isSupportInnerPINPad(){
        return (isModelQ1() || isTypeQ2() || isModelQ2() || isModelQ3a7() || isModelQ2a7());
    }

    public static boolean isTypeQ2(){
        String model = getSystemModel();
        return model.equals("WIZARHAND_Q2") || model.equals("WIZARPOS_Q2");
    }

    public static boolean isTypeQ3(){
        String model = getSystemModel();
        return model.equals("WIZARHAND_Q3") || model.equals("WIZARPOS_Q3");
    }

    public static boolean isLogoWposs(){
        String logo = getSystemLogo();
        return "wposs".equalsIgnoreCase(logo);
    }

    public static String getSystemModel(){
        String model = getProperty("ro.product.model","").trim();
        model = model.toUpperCase();
        return model;
    }
    public static String getSystemModelByNew(){
        String model = getProperty("ro.wp.product.model","").trim();
        model = model.toUpperCase();
        return model;
    }

    public static boolean isFactoryTest(){
        String value = getProperty("ro.wp.facttest", "");
        if("1".equals(value)){
            return true;
        }
        return false;
    }

    /** 基带版本 */
    public static String getBasebandVersion() {
        return getProperty("gsm.version.baseband", "");
    }

    /** user\eng\engroot */
    public static String getFirmwareType() {
        return getProperty("ro.firmware.type", "");
    }

    /** splash版本 */
    public static String getSplashVersion() {
        return getProperty("ro.wp.logo", "");
    }


    /** 关于pos的版本号字段. */
    public static String getSystemVersionName() {
        return getProperty("ro.build.display.id", "");
    }

    public static String getProperty(String key, String defaultValue){
        Object value = null ;
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            value = systemProperties.getMethod("get", new Class[]{String.class, String.class}).invoke(systemProperties, new Object[]{key, defaultValue});
        }catch (Exception e) {
            e.printStackTrace();
        }
        return value.toString();
    }
    public static int getProperty(String key, int defaultValue){

        String value = getProperty(key, "" + defaultValue);
//        int value = SystemProperties.getInt(key, defaultValue);
        return Integer.parseInt(value);
    }


    public static String getSystemLogo(){
        String logo = getProperty("ro.wp.logo","").trim();
        return logo;
    }
    public static boolean isLogoMinsheng(){
        String logo = getSystemLogo();
        return logo.equalsIgnoreCase(LOGO_CUSTOM_MINSHNEG);
    }

//    famoco
    public static boolean isLogoFamoco(){
        String logo = getSystemLogo();
        return logo.equalsIgnoreCase("famoco");
    }
    //    dejavoo
    public static boolean isLogoDejavoo(){
        String logo = getSystemLogo();
        return logo.equalsIgnoreCase("dejavoo");
    }
    //    aisino
    public static boolean isLogoAisino(){
        String logo = getSystemLogo();
        return logo.equalsIgnoreCase(LOGO_CUSTOM_AISINO);
    }
    public static boolean isLogoSPay(){
        String logo = getSystemLogo();
        return logo.equalsIgnoreCase(LOGO_CUSTOM_SPAY);
    }
    public static boolean isSupportUserRole(){
//        persist.wp.supportuserrole
        String key = "persist.wp.supportuserrole";
        int value = SystemUtils.getProperty(key, 0);
        Logger.debug("%s = %s", key, value);
        if(value == 1){
            return true;
        }
        return false;
    }
//    elgin
    public static boolean isLogoElgin(){
        String logo = getSystemLogo();
        return logo.equalsIgnoreCase(LOGO_CUSTOM_ELGIN);
    }
    public static boolean isLogoBizzpos(){
        String logo = getSystemLogo();
        return logo.equalsIgnoreCase(LOGO_CUSTOM_BIZZPOS);
    }

    public static final String SYSTEM_VERSION = "ro.wp.system.ver";
    public static final String KERNEL_VERSION  = "ro.wp.kernel.ver";
    public static final String BOOTLOADER_VERSION  = "ro.wp.bootloader.ver";

    public static int getSystemVersionCode(){
        String currentSystemVersion = getProperty(SYSTEM_VERSION,"").trim();
        if(currentSystemVersion.equals("")){
//            Build.VERSION.INCREMENTAL
            currentSystemVersion = Build.VERSION.INCREMENTAL;
            Log.e(TAG, "currentSystemVersion = " + currentSystemVersion);
        }
        int versionCode = 0;
        if(currentSystemVersion != null){
            if(isModelQ1()){
                String [] systemVersions =  currentSystemVersion.split("_");
                if(systemVersions.length >= 2){
                    versionCode = Integer.parseInt(systemVersions[1]);
                }else{
                    systemVersions = currentSystemVersion.split("-");
                    if(systemVersions.length >= 2){
                        versionCode = Integer.parseInt(systemVersions[1]);
                    }else{
                        Log.e(TAG, "Current System Version is error! currentSystemVersion =" + currentSystemVersion);
                    }
                }
//                wp1.0.0-2895-ge310e1d

            }else if(isModelWizarpos()){
                String [] systemVersions =  currentSystemVersion.split("r");
                if(systemVersions.length >= 2){
                    versionCode = Integer.parseInt(systemVersions[1]);
                }else{
                    Log.e(TAG, "Current System Version is error! currentSystemVersion =" + currentSystemVersion);
                }
            }else if(isModelWizarposV2()){
                String [] systemVersions =  currentSystemVersion.split("_");
                if(systemVersions.length >= 2){
                    versionCode = Integer.parseInt(systemVersions[1]);
                }else{
                    Log.e(TAG, "Current System Version is error! currentSystemVersion =" + currentSystemVersion);
                }
            }

        }else{
            Log.e(TAG, "Current System Version is Null!");
        }
        Log.d(TAG, "System version code is " + versionCode);
        return versionCode;
    }

    /**
     * formatted kernel version like <br/>
     * 3.0.31-g6fb96c9<br/>
     * builder@dailybuild #r6182<br/>
     * Thu Jun 28 11:02:39 PDT 2012<br/>
     * engroot
     * */
    public static String getNewFormattedKernelVersion() {

        String result = "Unavailable";
        final String PROC_VERSION_REGEX =
                "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
                "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
                "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
                "(#(?:r)?\\d+) " +        /* group 3: "#1" */
                "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
                "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */
        try{
            String rawKernelVersion = readLine(FILENAME_PROC_VERSION);
            Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
            if (!m.matches()) {
                Logger.error("Regex did not match on /proc/version: %s", rawKernelVersion);
            } else if (m.groupCount() < 4) {
                Logger.error("Regex match on /proc/version only returned %s groups", m.groupCount());
            } else {
                result = new StringBuilder(m.group(1)).append("\n").append(m.group(2)).append(" ")
                        .append(m.group(3)).append("\n").append(m.group(4)).append("\n")
                        .append(getFirmwareType()).toString();
            }
        } catch(IOException e) {
            Logger.error("IO Exception when getting kernel version for Device Info screen", e);
        }
        return result;
    }

    /**
     * Reads a line from the specified file.
     * @param filename the file to read from
     * @return the first line, if any.
     * @throws IOException if the file couldn't be read
     */
    public static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    public static boolean compareSDCardPaths(String newPath , String oldPath){
        File newFile = new File(newPath);
        File oldFile = new File(oldPath);
        if(newFile.exists() && oldFile.exists()){
            return newFile.getAbsolutePath().equals(oldFile.getAbsolutePath());
        }
        Log.d(TAG, String.format("路径不一致.%s , %s", newPath, oldPath ));
        return false;
    }
    /***
     * 获得系统更新根目录路径。
     * */
    public static String getSysUpdateRootPath(){
        String updateRootPath = IN_SDCARD;
//		根据终端的不同而采用不同的系统更新根目录路径
        if(isModelWizarpos()){
            updateRootPath = Environment.getExternalStorageDirectory().getPath();//默认为内部sd卡路径
        }
        return updateRootPath;
    }
    public static String getFirmwareUpdateDirPath(){
        return SystemUtils.getSysUpdateRootPath() + "/wizarpos/firmware";
    }

    /***
     * 获得系统更新根目录路径。
     * */
    public static String getFirmwareDirPath(){
        String updateRootPath = getSysUpdateRootPath() + "/wizarpos/firmware";
        return updateRootPath;
    }

    public static final int KB = 1024;
    public static final int M = KB * 1024;

    public static String getFileSizeString(long currentFileSize){
        if(currentFileSize < KB){
            return currentFileSize + " Byte";
        }else if(currentFileSize < KB){
            double result = (double)currentFileSize/(double)KB;
            String resultStr = String.format("%.2f", result);
            return resultStr + " KB";
        }else{
            double result = (double)currentFileSize/(double)M;
            String resultStr = String.format("%.2f", (double)90500 / (double)1024);
            return resultStr + " KB";
        }
    }



    /**
     * 查看是否是eng版本的系统。
     *
     * */
    public static boolean verifyPosVersionIsCheckCert(){
//		com.wizarpos.android.core.util.POSSecurity.requireCheckCert()
        try {
//			获得反射对象的类,加载指定的类
            Class<?> pOSSecurity = Class.forName("com.wizarpos.android.core.util.POSSecurity");
            Object resultObj = pOSSecurity.getMethod("requireCheckCert").invoke(pOSSecurity);
            Log.d(TAG, "checkPosVersion : resultObj = " + resultObj);
            if(resultObj != null){
                return Boolean.parseBoolean(resultObj.toString());
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG, "checkPosVersion has failed . happen exception ");
        }
        return false;
    }

    public static String getUniqueCode(Context mContext) {
//		：终端唯一标识包括：6 位厂商编号+2位终端类型（同设备类型） + 厂商编号： 例 000002，解析为 000（补充位） 002（厂商编号，起始为 001，依次累加）
//		终端类型： 01： ATM； 02：传统 POS； 03： MPOS； 04：智能 POS； 05： II 型固话 POS
        boolean hasOpened = HSMInterface.isOpened();
        try {
            if(!hasOpened){
                HSMInterface.open();
            }
            boolean isKeyExist = HSMInterface.isKeyExist(0, 0);
            if(!isKeyExist){
                return null;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } finally {
            if(!hasOpened){
                HSMInterface.close();
            }
        }
        String uniqueCode = null ;
        if(isLogoAisino()){
            uniqueCode = "000014" + "04" + getCustomSN();
        }else if(mContext != null){
            String factorySN = null;
            Cursor cursor = null;
            try {
                Uri uri = Uri.parse("content://com.cloudpos.terminal.provider/query");
                ContentResolver resolver = mContext.getContentResolver();
                String selection = "packageName=?" ;
                cursor = resolver.query(uri, new String[]{"sn"}, selection, new String[]{mContext.getPackageName()}, null);
                if (cursor!=null){
                    while (cursor.moveToNext()){
                        factorySN = cursor.getString(0);
                        Log.d(TAG, String.format("find SN : %s", factorySN));
                        break;
                    }
                }
            } catch(Exception e){
                Log.e(TAG, "find sn failed:" + e.getMessage());
            }finally {
                if(cursor != null){
                    cursor.close();
                }
            }
            uniqueCode = "000016" + "04" + (factorySN != null ? factorySN : Build.SERIAL);
        }

        return uniqueCode;
    }

    public static boolean allowReadAccessPcbSn(){
        if(isTypeQ2() || isTypeQ3() || isModelQ2a7() || isModelQ1v2()){
            return true;
        }
        return false;
    }

    //查找所有浏览器
    public static ActivityInfo getSystemBrowserActivityInfo(Context context) {
        PackageManager manager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://www.wizarpos.com/"));
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        if(infos != null && infos.size() >= 1){
            for (ResolveInfo info : infos) {
                Logger.debug("ResolveInfo : %s", info);
                try {
                    PackageInfo packageInfo = manager.getPackageInfo(info.activityInfo.packageName, 0);
                    boolean isSysApp = (packageInfo != null) && (packageInfo.applicationInfo != null) &&
                            ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
                    Logger.debug("isSysApp = %s", isSysApp);
                    if(isSysApp){
                        return info.activityInfo;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }else{
            Logger.error("No enabled browsers found");
        }
        return null;
    }



}
