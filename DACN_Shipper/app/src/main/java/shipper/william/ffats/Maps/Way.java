package shipper.william.ffats.Maps;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Way implements Parcelable {
    private String ID;
    private ArrayList<Node> nodes;
    private String name;
    private boolean oneWay;

    public Way() {
        nodes = new ArrayList<>();
    }

    public Way(Way way) {
        this.ID = way.getID();
        this.nodes = way.getNodes();
        this.name = way.getName();
        this.oneWay = way.isOneWay();
    }

    public Way(String ID, ArrayList<Node> nodes, String name, boolean oneWay) {
        this.ID = ID;
        this.nodes = nodes;
        this.name = name;
        this.oneWay = oneWay;
    }

    protected Way(Parcel in) {
        ID = in.readString();
        name = in.readString();
        oneWay = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID);
        dest.writeString(name);
        dest.writeByte((byte) (oneWay ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Way> CREATOR = new Creator<Way>() {
        @Override
        public Way createFromParcel(Parcel in) {
            return new Way(in);
        }

        @Override
        public Way[] newArray(int size) {
            return new Way[size];
        }
    };

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public void addNodes(Node node){
        this.nodes.add(node);
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOneWay() {
        return oneWay;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }
}
