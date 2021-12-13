package client.william.ffats.Maps;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Node implements Parcelable {
    private Long ID;
    private Double latitude;
    private Double longitude;
    private ArrayList<Way> wayId;

    public Node() {
        this.wayId = new ArrayList<>();
    }


    protected Node(Parcel in) {
        if (in.readByte() == 0) {
            ID = null;
        } else {
            ID = in.readLong();
        }
        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readDouble();
        }
        wayId = in.createTypedArrayList(Way.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (ID == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(ID);
        }
        if (latitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(latitude);
        }
        if (longitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(longitude);
        }
        dest.writeTypedList(wayId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Node> CREATOR = new Creator<Node>() {
        @Override
        public Node createFromParcel(Parcel in) {
            return new Node(in);
        }

        @Override
        public Node[] newArray(int size) {
            return new Node[size];
        }
    };

    public Long getID() {
        return ID;
    }


    public void setID(Long ID) {
        this.ID = ID;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public ArrayList<Way> getWay() {
        return wayId;
    }

    public void setWay(ArrayList<Way> wayId) {
        this.wayId = wayId;
    }

}
