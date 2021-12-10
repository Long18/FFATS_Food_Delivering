package shipper.william.ffats.Maps;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class MapValue implements Parcelable {
    private Double minLat;
    private Double maxLat;
    private Double minLon;
    private Double maxLon;

    private ArrayList<Node> nodes;
    private ArrayList<Way> ways;

    private ArrayList<Node> vertices;
    private int MAX_length;
    private Double[][] graph;

    public MapValue(){
        this.nodes = new ArrayList<>();
        this.ways = new ArrayList<>();
        this.vertices = new ArrayList<>();
    }


    protected MapValue(Parcel in) {
        if (in.readByte() == 0) {
            minLat = null;
        } else {
            minLat = in.readDouble();
        }
        if (in.readByte() == 0) {
            maxLat = null;
        } else {
            maxLat = in.readDouble();
        }
        if (in.readByte() == 0) {
            minLon = null;
        } else {
            minLon = in.readDouble();
        }
        if (in.readByte() == 0) {
            maxLon = null;
        } else {
            maxLon = in.readDouble();
        }
        MAX_length = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (minLat == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(minLat);
        }
        if (maxLat == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(maxLat);
        }
        if (minLon == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(minLon);
        }
        if (maxLon == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(maxLon);
        }
        dest.writeInt(MAX_length);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MapValue> CREATOR = new Creator<MapValue>() {
        @Override
        public MapValue createFromParcel(Parcel in) {
            return new MapValue(in);
        }

        @Override
        public MapValue[] newArray(int size) {
            return new MapValue[size];
        }
    };

    public double getMinLat() {
        return minLat;
    }

    public void setMinLat(Double minLat) {
        this.minLat = minLat;
    }

    public Double getMaxLat() {
        return maxLat;
    }

    public void setMaxLat(Double maxLat) {
        this.maxLat = maxLat;
    }

    public Double getMinLon() {
        return minLon;
    }

    public void setMinLon(Double minLon) {
        this.minLon = minLon;
    }

    public Double getMaxLon() {
        return maxLon;
    }

    public void setMaxLon(Double maxLon) {
        this.maxLon = maxLon;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public ArrayList<Way> getWays() {
        return ways;
    }

    public void setWays(ArrayList<Way> ways) {
        this.ways = ways;
    }

    public ArrayList<Node> getVertices() {
        return vertices;
    }

    public void setVertices(ArrayList<Node> vertices) {
        this.vertices = vertices;
    }

    public int getMAX_length() {
        return MAX_length;
    }

    public void setMAX_length(int MAX_length) {
        this.MAX_length = MAX_length;
    }

    public Double[][] getGraph() {
        return graph;
    }

    public void setGraph(Double[][] graph) {
        this.graph = graph;
    }

}
