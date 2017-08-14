package com.nju.urbangreen.zhenjiangurbangreen.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nju.urbangreen.zhenjiangurbangreen.basisClass.GreenObjectSug;
import com.nju.urbangreen.zhenjiangurbangreen.basisClass.GreenObject;
import com.nju.urbangreen.zhenjiangurbangreen.maintainRecord.Maintain;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.EOFException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Liwei on 2016/12/25.
 */
public class WebServiceUtils {
    //public static final String SOAP_ACTION = "http://tempuri.org/CheckUpdate";
    //public static final String OPERATION_NAME = "CheckUpdate";
    //public static final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    public static final String WSDL_TARGET_NAMESPACE = "http://services.ui.webbos.sjf.org/";
    public static final String SOAP_ADDRESS = "http://114.212.112.41/GreenLand_test/EXT_GreenLand/Mobile/Services/GLService.asmx";
    public static final int Timeout = 10000;

    public static final String Check_Update = "CheckUpdate";
    public static final String Login = "Login";
    public static final String Get_Maintain_Record = "GetMaintainRecord";
    public static final String Get_UGO_Info_Except_ST = "GetUGOInfoExceptST";//ST表示行道树
    public static final String Get_Near_Street_Tree = "GetNearStreetTree";
    public static final String GET_UGO_Suggest = "GetUGOSuggest";
    public static final String SEARCH_UGO_INFO = "SearchUGOInfo_2";

    public static final String KEY_REFLACT_OPERATION_NAME = "wmn";
    public static final String KEY_REFLACT_OPERATION_PARAM = "wmp";
    public static final String OPERATION_NAME = "RequestServices";
    public static final String OPERATION_NAME_WITHOUT_USERINFO = "RequestServicesWithoutUserInfo";
    public static final String KEY_OPERATION_PARAM = "RequestInfo";
    /**
     * 登录名
     */
    public static final String KEY_USERNAME = "ln";
    /**
     * 密码
     */
    public static final String KEY_PASSWORD = "lp";
    /**
     * 是否成功
     */
    public static final String KEY_SUCCEED = "ok";
    /**
     * 结果说明
     */
    public static final int RESULT_SUCCEED = 100;
    /**
     * 错误信息
     */
    public static final String KEY_ERRMESSAGE = "msg";
    /**
     * 返回结果
     */
    public static final String KEY_RESULT = "ret";

    //public static boolean flag = false;
    //static Map<String, Object> results = new HashMap<String, Object>();

    private static Gson gson = new GsonBuilder().serializeNulls().create();
    private static ConnectivityManager connectivityManager = (ConnectivityManager) MyApplication.getContext()
            .getSystemService(Context.CONNECTIVITY_SERVICE);

    private static boolean is_offline() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo == null || !networkInfo.isAvailable());
    }

    private static Map<String, Object> callMethod(String operationName, Map<String, Object> params) {
        return callMethod(operationName, params, true);
    }

    private static Map<String, Object> callMethod(String methodName, Map<String, Object> params, Boolean identify) {

        Map<String, Object> results = new HashMap<String, Object>();
        Map<String, Object> requestProperty = new HashMap<String, Object>();

        SoapObject request;
        if (identify) {
            String username = SPUtils.getString("username", "xk");
            String password = SPUtils.getString("password", "@");

            if (methodName.equals(Login)) {
                username = params.get(KEY_USERNAME).toString();
                password = params.get(KEY_PASSWORD).toString();
                Log.i("Login username: ", username);
                Log.i("Login password: ", password);
            }
            requestProperty.put(KEY_REFLACT_OPERATION_NAME, methodName);
            requestProperty.put(KEY_USERNAME, username);
            requestProperty.put(KEY_PASSWORD, password);
            request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);
        } else {
            request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME_WITHOUT_USERINFO);
            requestProperty.put(KEY_REFLACT_OPERATION_NAME, "NoUser_" + methodName);
        }


        requestProperty.put(KEY_REFLACT_OPERATION_PARAM, params);
        request.addProperty(KEY_OPERATION_PARAM, ZipUtils.compress(gson.toJson(requestProperty)));

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
        envelope.dotNet = true;
        envelope.encodingStyle = SoapSerializationEnvelope.ENC;
        envelope.setOutputSoapObject(request);

        HttpTransportSE transport = new HttpTransportSE(SOAP_ADDRESS, Timeout);

        try {
            if (identify) {
                transport.call(WSDL_TARGET_NAMESPACE + OPERATION_NAME, envelope);
            } else {
                transport.call(WSDL_TARGET_NAMESPACE + OPERATION_NAME_WITHOUT_USERINFO, envelope);
            }

            SoapPrimitive resultPrimitive = (SoapPrimitive) envelope.getResponse();
            String tmp = resultPrimitive.toString();
            String jsonString = ZipUtils.uncompress(tmp);
            results = gson.fromJson(jsonString, results.getClass());

        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            results.clear();
            results.put(KEY_SUCCEED, 99);
            results.put(KEY_ERRMESSAGE, "网络连接超时，请检查网络设置~");
            return results;
        } catch (SocketException e) {
            e.printStackTrace();
            results.clear();
            results.put(KEY_SUCCEED, 99);
            results.put(KEY_ERRMESSAGE, "网络异常，请重试~");
            return results;
        } catch (EOFException e) {
            e.printStackTrace();
            results.clear();
            results.put(KEY_SUCCEED, 99);
            results.put(KEY_ERRMESSAGE, "网络异常，请重试~");
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            results.clear();
            results.put(KEY_SUCCEED, 99);
            results.put(KEY_ERRMESSAGE, e.getMessage());
            return results;
        }

        return results;
    }

    public static Map<String, Object> checkUpdate(String[] errorMessage) {
        if (is_offline()) {
            errorMessage[0] = "网络连接断开，请稍后再试";
            return null;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("VersionCode", getVersion());
        Map<String, Object> results = callMethod(Check_Update, params);
        if (Integer.parseInt(results.get(KEY_SUCCEED).toString()) == RESULT_SUCCEED) {
            String jsonResults = results.get(KEY_RESULT).toString();
            return gson.fromJson(jsonResults, new TypeToken<Map<String, Object>>() {
            }.getType());
        } else {
            if (errorMessage != null && results.get(KEY_ERRMESSAGE) != null) {
                errorMessage[0] = results.get(KEY_ERRMESSAGE).toString();
                Log.i("错误消息", "checkUpdate: " + errorMessage[0]);
            }
            return null;
        }

    }

    public static List<Maintain> getMaintainRecord(Map<String, Object> query, String[] errorMessage) {
        if (is_offline()) {
            errorMessage[0] = "网络连接断开，请稍后再试";
            return null;
        }
        Map<String, Object> res = callMethod(Get_Maintain_Record, query);
        if (Integer.parseInt(res.get(KEY_SUCCEED).toString()) == RESULT_SUCCEED) {
            return gson.fromJson(res.get(KEY_RESULT).toString(),
                    new TypeToken<List<Maintain>>() {
                    }.getType());
        } else {
            if (errorMessage != null && res.get(KEY_ERRMESSAGE) != null) {
                errorMessage[0] = res.get(KEY_ERRMESSAGE).toString();
            }
            return null;
        }
    }

    public static List<GreenObject> getUGOInfoExceptST(String[] errorMessage) {
        if (CacheUtil.hasUGOs()) {
            return CacheUtil.getUGOs();
        } else {
            if (is_offline()) {
                errorMessage[0] = "网络连接断开，请稍后再试";
                return null;
            }
            Map<String, Object> results = callMethod(Get_UGO_Info_Except_ST, null);
            if (Integer.parseInt(results.get(KEY_SUCCEED).toString()) == RESULT_SUCCEED) {
                String jsonResults = results.get(KEY_RESULT).toString();
                List<GreenObject> objs = new ArrayList<>();
                objs = gson.fromJson(jsonResults, new TypeToken<List<GreenObject>>() {
                }.getType());
                CacheUtil.putUGOs(objs);
                return objs;
            } else {
                if (errorMessage != null && results.get(KEY_ERRMESSAGE) != null) {
                    errorMessage[0] = results.get(KEY_ERRMESSAGE).toString();
                }
                return null;
            }
        }

    }


    public static List<GreenObject> getNearStreetTree(double x, double y, double radius, String[] errorMessage) {
        if (is_offline()) {
            errorMessage[0] = "网络连接断开，请稍后再试";
            return null;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("radius", radius);
        params.put("pos_json_str", GeoJsonUtil.Point2WKTString(x, y));
        Map<String, Object> results = callMethod(Get_Near_Street_Tree, params);
        if (Integer.parseInt(results.get(KEY_SUCCEED).toString()) == RESULT_SUCCEED) {
            String jsonResults = results.get(KEY_RESULT).toString();
            return gson.fromJson(jsonResults, new TypeToken<List<GreenObject>>() {
            }.getType());
        } else {
            if (errorMessage != null && results.get(KEY_ERRMESSAGE) != null) {
                errorMessage[0] = results.get(KEY_ERRMESSAGE).toString();
                Log.i("错误信息", "get near street tree: " + errorMessage[0]);
            }
            return null;
        }
    }

    public static List<GreenObject> searchUGOInfo_2(String[] errorMessage, String ugoParam, String flag) {
        if (is_offline()) {
            errorMessage[0] = "网络连接断开，请稍后再试";
            return null;
        }
        Map<String, Object> params = new HashMap<>();
        params.put(flag,ugoParam);

        Map<String, Object> results = callMethod(SEARCH_UGO_INFO, params);
        if (Integer.parseInt(results.get(KEY_SUCCEED).toString()) == RESULT_SUCCEED) {
            String jsonResults = results.get(KEY_RESULT).toString();
            return gson.fromJson(jsonResults, new TypeToken<List<GreenObject>>() {
            }.getType());
        } else {
            if (errorMessage != null && results.get(KEY_ERRMESSAGE) != null) {
                errorMessage[0] = results.get(KEY_ERRMESSAGE).toString();
                Log.i("错误信息", "Get UGO Sug: " + errorMessage[0]);
            }
            return null;
        }
    }

    public static List<GreenObjectSug> getUGOSug(String[] errorMessage) {
        if (is_offline()) {
            errorMessage[0] = "网络连接断开，请稍后再试";
            return null;
        }
        Map<String, Object> results = callMethod(GET_UGO_Suggest, null);
        if (Integer.parseInt(results.get(KEY_SUCCEED).toString()) == RESULT_SUCCEED) {
            String jsonResults = results.get(KEY_RESULT).toString();
            return gson.fromJson(jsonResults, new TypeToken<List<GreenObjectSug>>() {
            }.getType());
        } else {
            if (errorMessage != null && results.get(KEY_ERRMESSAGE) != null) {
                errorMessage[0] = results.get(KEY_ERRMESSAGE).toString();
                Log.i("错误信息", "Get UGO Sug: " + errorMessage[0]);
            }
            return null;
        }
    }

    public static Map<String, Object> login(String username, String password, String[] errorMessage) {
        if (is_offline()) {
            errorMessage[0] = "网络连接断开，请稍后再试";
            return null;
        }
        Map<String, Object> params = new HashMap<>();
        params.put(KEY_USERNAME, username);
        params.put(KEY_PASSWORD, password);
        Map<String, Object> results = callMethod(Login, params);
        if (Integer.parseInt(results.get(KEY_SUCCEED).toString()) == RESULT_SUCCEED) {
            String jsonResults = results.get(KEY_RESULT).toString();
            return gson.fromJson(jsonResults, new TypeToken<Map<String, Object>>() {
            }.getType());

        } else {
            if (errorMessage != null && results.get(KEY_ERRMESSAGE) != null) {
                errorMessage[0] = results.get(KEY_ERRMESSAGE).toString();
                Log.i("错误信息", "login: " + errorMessage[0]);
            }
            return null;
        }
    }

    public static int getVersion() {
        try {
            PackageManager manager = MyApplication.getContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(MyApplication.getContext().getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}