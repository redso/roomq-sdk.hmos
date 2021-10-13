package com.example.roomq_module;

import com.example.roomq_module.network.EnqueueResponse;
import com.example.roomq_module.network.RoomStatus;
import com.google.gson.Gson;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.app.Context;

import java.util.HashMap;
import java.util.Map;

public class RoomQ {
    public static final String TOKEN = "RoomQ.TOKEN";

    private RoomStatus roomStatus = null;

    private String clientID = null;

    public RoomQ(String clientID) {
        this.clientID = clientID;
    }

    private RoomStatus getStatus() {
        if (roomStatus != null) {
            return roomStatus;
        }
        try {
            String responseString = Utils.sendHTTPRequest("GET", "https://roomq.noqstatus.com/api/rooms/" + clientID, null);
            if (responseString != null) {
                return new Gson().fromJson(responseString, RoomStatus.class);
            }
        } catch (HTTPRequestException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getEndpoint() {
        RoomStatus roomStatus = getStatus();
        LogManager.getInstance().log(roomStatus.toString());
        if (roomStatus == null || roomStatus.state == "pause" || roomStatus.state == "stopped") {
            return null;
        }
        return "https://" + roomStatus.backend + "/queue/" + clientID;
    }

    public void enqueue(Context context, EnqueueCallback callback) {
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
                String responseString = Utils.sendHTTPRequest("POST", endpoint, data);
                EnqueueResponse enqueueResponse = new Gson().fromJson(responseString, EnqueueResponse.class);
                Token.set(context, enqueueResponse.id);

                if (enqueueResponse.vd % 2 == 1 || true) {
                    Intent waitingRoomIntent = new Intent();
                    Operation operation = new Intent.OperationBuilder()
                            .withDeviceId("")
//                            .withBundleName("hk.noq.roomq")
                            .withAbilityName("hk.noq.roomq.WaitingRoomAbility")
                            .build();
                    waitingRoomIntent.setOperation(operation);
                    String waitingRoomUrl = enqueueResponse.waitingRoomURL + "?noq_t=" + enqueueResponse.id + "&c=" + clientID + "&noq_r=https://app.noq.com.hk?p=hmos";
                    waitingRoomIntent.setParam("URL",waitingRoomUrl);
                    callback.onEnterRoom(waitingRoomIntent, enqueueResponse.id);
                } else {
                    callback.onEnterMain(CompletionType.ENTER_DIRECTLY, enqueueResponse.id);
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onEnterMain(CompletionType.ENTER_WITH_ERROR, "");
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
                EnqueueResponse enqueueResponse = new Gson().fromJson(responseString, EnqueueResponse.class);
                Token.set(context, enqueueResponse.id);
                if (enqueueResponse.vd % 2 == 1) {
                    callback.onResult(ExtendSessionResponse.EXPIRED);
                } else {
                    callback.onResult(ExtendSessionResponse.EXTENDED);
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onResult(ExtendSessionResponse.SERVER_ERROR);
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
            } catch (Exception e) {
                e.printStackTrace();
                callback.onResult(DeleteSessionResponse.SERVER_ERROR);
            }
        }).start();
    }


    public enum CompletionType {
        ENTER_DIRECTLY,
        ENTER_WITH_ERROR
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

    public interface EnqueueCallback {
        void onEnterRoom(Intent intent, String url);
        void onEnterMain(CompletionType completionType, String token);
    }

    public interface ExtendSessionCallback {
        void onResult(ExtendSessionResponse response);
    }

    public interface DeleteSessionCallback {
        void onResult(DeleteSessionResponse response);
    }
}
