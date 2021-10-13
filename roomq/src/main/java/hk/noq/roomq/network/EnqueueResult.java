package hk.noq.roomq.network;

import com.google.gson.annotations.SerializedName;

public class EnqueueResult {
    public String id;
    public Integer vd;

    @SerializedName("waiting_room_url")
    public String waitingRoomURL;
}

