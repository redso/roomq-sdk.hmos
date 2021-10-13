package com.example.roomq_sdk_hmos_demo;

import hk.noq.roomq.*;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;

public class DemoAbility extends Ability {
    static Integer REQUEST_WAITING_ROOM = 0;
    private RoomQ roomq = null;
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        setUIContent(ResourceTable.Layout_ability_demo);
        LogManager.getInstance().log("DemoAbility");
        roomq = new RoomQ("enicorn-demo", true);
        roomq.enqueue(this, new  RoomQ.EnqueueCallback() {
            @Override
            public void onResult(RoomQ.EnqueueResponse enqueueResponse, Intent intent, String token) {
                switch (enqueueResponse) {
                    case WAIT:
                        startAbilityForResult(intent, REQUEST_WAITING_ROOM);
                        break;
                    case ENTER_DIRECTLY:
                        enterMain();
                        break;
                    case ENTER_WITH_ERROR:
                        LogManager.getInstance().log("Enter normal flow with error");
                        enterMain();
                        break;
                    case AUTHENTICATION_ERROR:
                        LogManager.getInstance().log("Authentication Error");
                        break;
                }
            }
        });
    }

    private void enterMain() {
        Intent mainPageIntent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withBundleName("com.example.roomq_sdk_hmos_demo")
                .withAbilityName("com.example.roomq_sdk_hmos_demo.MainAbility")
                .build();
        mainPageIntent.setOperation(operation);
        startAbility(mainPageIntent);
    }

    private void extendSession() {
        if (roomq != null)
            roomq.extendSession(this, 1, new RoomQ.ExtendSessionCallback() {
                @Override
                public void onResult(RoomQ.ExtendSessionResponse extendSessionResponse) {
                    switch (extendSessionResponse) {
                        case EXTENDED:
                            LogManager.getInstance().log("Extended");
                            break;
                        case EXPIRED:
                            LogManager.getInstance().log("Expired");
                            break;
                        case SERVER_ERROR:
                            LogManager.getInstance().log("Server Error");
                            break;
                    }
                }
            });
    }

    private void deleteSession() {
        if (roomq != null)
        roomq.deleteSession(this, new RoomQ.DeleteSessionCallback() {
            @Override
            public void onResult(RoomQ.DeleteSessionResponse deleteSessionResponse) {
                if (deleteSessionResponse == RoomQ.DeleteSessionResponse.SUCCESS) {
                    LogManager.getInstance().log("Deleted");
                } else if (deleteSessionResponse == RoomQ.DeleteSessionResponse.SERVER_ERROR) {
                    LogManager.getInstance().log("Server Error");
                }
            }
        });
    }

    private void getExpiryTime() {
        if (roomq != null) {
            roomq.getExpiryTime(this, new RoomQ.GetExpiryTimeCallback() {
                @Override
                public void onResult(RoomQ.GetExpiryTimeResponse getExpiryTimeResponse, Long deadline, Exception error) {
                    switch (getExpiryTimeResponse) {
                        case SUCCESS:
                            System.out.println("Expiry Time: " + deadline);
                            break;
                        case NO_TOKEN:
                            System.out.println("No Token");
                            break;
                        case EXPIRED:
                            System.out.println("Session Expired");
                            break;
                        case ERROR:
                            System.out.println("Error: " + error.toString());
                            break;
                        case NO_DEADLINE:
                            System.out.println("No Deadline");
                            break;
                    }
                }
            });
        }
    }

    @Override
    protected void onAbilityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_WAITING_ROOM) {
            if (resultCode == RoomQ.RESULT_OK) {
                if (resultData != null) {
                    String token = resultData.getStringParam(RoomQ.TOKEN);
                    enterMain();
                }
            }
        }
        super.onAbilityResult(requestCode, resultCode, resultData);
    }
}
