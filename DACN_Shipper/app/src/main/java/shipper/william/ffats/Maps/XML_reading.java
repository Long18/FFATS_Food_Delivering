package shipper.william.ffats.Maps;

import android.content.res.AssetManager;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class XML_reading {
    public static boolean isUsingThread = false;
    //region reading xml map

    /**
     *  reading XML from assets
     *
     * @param assetManager  assets folder
     * @param fileName      file name in assets folder
     * @param mapValue      output class
     */
    public static void readXml(AssetManager assetManager, String fileName, MapValue mapValue) {
        InputStream xmlStream;
        try {

            // declare value
            xmlStream = assetManager.open(fileName);
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlParser.setInput(xmlStream, null);

            processParsing(xmlParser, mapValue);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    /**
     * reading xml line and add data to outputClass
     * @param xmlParser     xmlParser
     * @param mapValue      output class
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static void processParsing(XmlPullParser xmlParser, MapValue mapValue) throws XmlPullParserException, IOException {
        int evenType = 0;
        Node currentNode = null;
        Way currentWay = null;


        evenType = xmlParser.getEventType();
        boolean isHighway = false;
        int count = 0;

        ParserLoop:
        while (evenType != XmlPullParser.END_DOCUMENT) {
            String eltName = null;
            count++;
//            Log.e("TAG", "processParsing: "+ count +" " + evenType);

//            if (!isHighway) {
//                evenType = xmlParser.next();
//                continue;
//            }

            switch (evenType) {
                case XmlPullParser.START_TAG:
                    eltName = xmlParser.getName();

                    if (eltName.equals("bounds")) {
                        mapValue.setMinLat(Double.parseDouble(xmlParser.getAttributeValue(null, "minlat")));
                        mapValue.setMinLon(Double.parseDouble(xmlParser.getAttributeValue(null, "minlon")));
                        mapValue.setMaxLat(Double.parseDouble(xmlParser.getAttributeValue(null, "maxlat")));
                        mapValue.setMaxLon(Double.parseDouble(xmlParser.getAttributeValue(null, "maxlon")));
                    }

                    if (eltName.equals("node")) {

                        currentNode = new Node();
                        currentNode.setID(Long.parseLong(xmlParser.getAttributeValue(null, "id")));
                        currentNode.setLatitude(Double.parseDouble(xmlParser.getAttributeValue(null, "lat")));
                        currentNode.setLongitude(Double.parseDouble(xmlParser.getAttributeValue(null, "lon")));
                        mapValue.getNodes().add(currentNode);
                    }
                    if (eltName.equals("way")) {

                        currentWay = new Way();
                        currentWay.setID(xmlParser.getAttributeValue(null, "id"));

                    }
                    if (eltName.equals("nd")) {
                        Node temp = new Node();
                        temp.setID(Long.parseLong(xmlParser.getAttributeValue(null, "ref")));
                        currentWay.addNodes(temp);
                    }

                    if (eltName.equals("tag")) {
                        // way type
                        if (xmlParser.getAttributeValue(null, "k").equals("highway")) {
                            if (highwayCheck(xmlParser.getAttributeValue(null, "v"), false)) {
                                isHighway = true;
                            }

                        }

                        if (xmlParser.getAttributeValue(null, "k").equals("service")) {
                            if (xmlParser.getAttributeValue(null, "v").equals("alley")) {
                                isHighway = true;
                            }

                        }

                        if (xmlParser.getAttributeValue(null, "k").equals("oneway")) {
                            if (xmlParser.getAttributeValue(null, "v").equals("yes")) {
                                currentWay.setOneWay(true);
                            }

                        }
                    }
                    if (eltName.equals("relation")) {
                        break ParserLoop;
                    }

                    break;


                case XmlPullParser.END_TAG:
                    eltName = xmlParser.getName();
                    if (eltName.equals("way") && isHighway && currentWay!= null) {
                        addWay(new Way(currentWay),mapValue);
                        isHighway = false;
                    }
                    break;
            }

            evenType = xmlParser.next();
        }

    }

    /**
     * find and add node into currentWay and add currentWay to ways List
     *
     * @param currentWay    current way
     * @param mapValue      output class
     */
    public static void addWay(Way currentWay, MapValue mapValue) {

        if(isUsingThread){
            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                    ///////////////////////
                    for (int i = 0; i < currentWay.getNodes().stream().count(); i++) {
                        for (Node node :
                                mapValue.getNodes()) {
                            if (node.getID().equals(currentWay.getNodes().get(i).getID())) {

                                node.getWay().add(currentWay);
                                currentWay.getNodes().set(i, node);

                                break;
                            }
                        }
                    }
                    /////////////////////////
                    mapValue.getWays().add(currentWay);
                }
            });
            th.start();
        }else{
            //                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            ///////////////////////
            for (int i = 0; i < currentWay.getNodes().stream().count(); i++) {
                for (Node node :
                        mapValue.getNodes()) {
                    if (node.getID().equals(currentWay.getNodes().get(i).getID())) {

                        node.getWay().add(currentWay);
                        currentWay.getNodes().set(i, node);

                        break;
                    }
                }
            }
            /////////////////////////
            mapValue.getWays().add(currentWay);
        }
    }


    /**
     * check that input value match with case
     *
     * @param HighwayValue  input value
     * @param getAll        "true" is get all the way, "false" is just get the way in case
     * @return
     */
    public static boolean highwayCheck(String HighwayValue, boolean getAll) {
        if (!getAll) {
            switch (HighwayValue) {
                case "motorway":
                case "trunk":
                case "primary":
                case "secondary":
                case "tertiary":
                case "unclassified":
                case "residential":
                case "motorway_link":
                case "trunk_link":
                case "primary_link":
                case "secondary_link":
                case "tertiary_link":
                case "living_street":
                case "pedestrian":

                    return true;
                default:
                    return false;
            }
        } else {

            return true;
        }
    }



    //endregion
}
