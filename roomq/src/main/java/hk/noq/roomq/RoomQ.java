package hk.noq.roomq;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import hk.noq.roomq.network.EnqueueResult;
import hk.noq.roomq.network.ExpiryTime;
import hk.noq.roomq.network.RoomStatus;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.app.Context;

import java.util.HashMap;
import java.util.Map;

public class RoomQ {
    public static final String TOKEN = "RoomQ.TOKEN";
    public static final int RESULT_OK = 0;

    private RoomStatus roomStatus = null;

    private String clientID = null;

    public RoomQ(String clientID, Boolean debug) {
        this.clientID = clientID;
        LogManager.getInstance().setDebugMode(debug);
    }

    public RoomQ(String clientID) {
        this(clientID, false);
    }

    private RoomStatus getStatus() {
        if (roomStatus != null) {
            return roomStatus;
        }
        try {
            String responseString = Utils.sendHTTPRequest("GET", "https://roomq.noqstatus.com/api/rooms/" + clientID, null);
            LogManager.getInstance().log("Status Response: " + responseString);
            if (responseString != null) {
                return new Gson().fromJson(responseString, RoomStatus.class);
            }
        } catch (HTTPRequestException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getEndpoint() {
        RoomStatus status = getStatus();
        LogManager.getInstance().log(status.toString());
        if (status.state == null || status.state == "pause" || status.state == "stopped") {
            return null;
        }
        return "https://" + status.backend + "/queue/" + clientID;
    }

    public void enqueue(Context context, EnqueueCallback callback) {
        new Thread(() -> {
            String endpoint = getEndpoint();
            LogManager.getInstance().log("Endpoint: " + endpoint);
            if (endpoint == null) {
                return;
            }
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("id", Token.get(context));
                data.put("action", "beep");
                data.put("client_id", clientID);
                String responseString = Utils.sendHTTPRequest("POST", endpoint, data);
                EnqueueResult enqueueResult = new Gson().fromJson(responseString, EnqueueResult.class);

                Token.set(context, enqueueResult.id);

                if (enqueueResult.vd % 2 == 1 || true) {
                    Intent waitingRoomIntent = new Intent();
                    Operation operation = new Intent.OperationBuilder()
                            .withDeviceId("")
                            .withBundleName(context.getBundleName())
                            .withAbilityName("hk.noq.roomq.WaitingRoomAbility")
                            .build();
                    waitingRoomIntent.setOperation(operation);
                    String waitingRoomUrl = enqueueResult.waitingRoomURL + "?noq_t=" + enqueueResult.id + "&c=" + clientID + "&noq_r=https://app.noq.com.hk?p=hmos";
                    waitingRoomIntent.setParam("URL",waitingRoomUrl);
                    callback.onResult(EnqueueResponse.WAIT, waitingRoomIntent, enqueueResult.id);
                    return;
                } else {
                    callback.onResult(EnqueueResponse.ENTER_DIRECTLY, null, enqueueResult.id);
                    return;
                }
            } catch (HTTPRequestException e) {
                e.printStackTrace();
                String code = String.valueOf(e.getStatusCode());
                if (code.equals("401")) {
                    callback.onResult(EnqueueResponse.AUTHENTICATION_ERROR, null, null);
                    return;
                } else {
                    callback.onResult(EnqueueResponse.ENTER_WITH_ERROR, null, "");
                    return;
                }
            } catch (JsonSyntaxException e) {
                callback.onResult(EnqueueResponse.ENTER_WITH_ERROR, null,"");
                return;
            }
        }).start();
    }

    public void getExpiryTime(Context context, GetExpiryTimeCallback callback) {
        new Thread(() -> {
            if (Token.get(context) == null) {
                callback.onResult(GetExpiryTimeResponse.NO_TOKEN, null, null);
                return;
            }
            String endpoint = getEndpoint();
            if (endpoint == null) {
                return;
            }
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("id", Token.get(context));
                data.put("action", "get_serving");
                data.put("client_id", clientID);
                String responseString = Utils.sendHTTPRequest("POST", endpoint, data);
                ExpiryTime expiryTime = new Gson().fromJson(responseString, ExpiryTime.class);
                if (expiryTime != null && expiryTime.deadline != null) {
                    callback.onResult(GetExpiryTimeResponse.SUCCESS, expiryTime.deadline, null);
                    return;
                }
                callback.onResult(GetExpiryTimeResponse.NO_DEADLINE, null, null);
                return;
            } catch (HTTPRequestException e) {
                e.printStackTrace();
                String code = String.valueOf(e.getStatusCode());
                LogManager.getInstance().log("Code: " + code);
                if (code.equals("404")) {
                    callback.onResult(GetExpiryTimeResponse.EXPIRED, null, null);
                    return;
                } else {
                    callback.onResult(GetExpiryTimeResponse.ERROR, null, e);
                    return;
                }
            } catch (JsonSyntaxException e) {
                callback.onResult(GetExpiryTimeResponse.ERROR, null, e);
                return;
            }
        }).start();
    }

    public void extendSession(Context context, Integer minutes, ExtendSessionCallback callback) {
        new Thread(() -> {
            String endpoint = getEndpoint();
            if (endpoint == null) {
                return;
            }
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("id", Token.get(context));
                data.put("action", "beep");
                data.put("client_id", clientID);
                data.put("extend_serving_duration", minutes * 60);

                String responseString = Utils.sendHTTPRequest("POST", endpoint, data);
                EnqueueResult enqueueResult = new Gson().fromJson(responseString, EnqueueResult.class);
                Token.set(context, enqueueResult.id);
                if (enqueueResult.vd % 2 == 1) {
                    callback.onResult(ExtendSessionResponse.EXPIRED);
                    return;
                } else {
                    callback.onResult(ExtendSessionResponse.EXTENDED);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onResult(ExtendSessionResponse.SERVER_ERROR);
                return;
            }
        }).start();
    }


    public void deleteSession(Context context, DeleteSessionCallback callback) {
        new Thread(() -> {
            String endpoint = getEndpoint();
            if (endpoint == null) {
                return;
            }
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("id", Token.get(context));
                data.put("action", "delete_serving");
                data.put("client_id", clientID);

                String responseString = Utils.sendHTTPRequest("POST", endpoint, data);
                callback.onResult(DeleteSessionResponse.SUCCESS);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                callback.onResult(DeleteSessionResponse.SERVER_ERROR);
                return;
            }
        }).start();
    }

    public void setToken(Context context, String token) {
        Token.set(context, token);
    }

    public enum EnqueueResponse {
        WAIT,
        ENTER_DIRECTLY,
        ENTER_WITH_ERROR,
        AUTHENTICATION_ERROR
    }

    public enum ExtendSessionResponse {
        EXTENDED,
        EXPIRED,
        SERVER_ERROR
    }

    public enum DeleteSessionResponse {
        SUCCESS,
        SERVER_ERROR
    }

    public enum GetExpiryTimeResponse {
        SUCCESS,
        EXPIRED,
        NO_TOKEN,
        NO_DEADLINE,
        ERROR
    }

    public interface EnqueueCallback {
        void onResult(EnqueueResponse response, Intent waitingRoomIntent, String token);
    }

    public interface ExtendSessionCallback {
        void onResult(ExtendSessionResponse response);
    }

    public interface DeleteSessionCallback {
        void onResult(DeleteSessionResponse response);
    }

    public interface GetExpiryTimeCallback {
        void onResult(GetExpiryTimeResponse response, Long deadline, Exception error);
    }
}
