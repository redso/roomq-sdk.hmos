package com.example.roomq_module.network;

import com.google.gson.annotations.SerializedName;

public class EnqueueResponse {
    public String id;
    public Integer vd;

    @SerializedName("waiting_room_url")
    public String waitingRoomURL;
}

