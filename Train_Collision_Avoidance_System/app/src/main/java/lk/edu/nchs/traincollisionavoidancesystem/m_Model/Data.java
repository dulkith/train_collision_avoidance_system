package lk.edu.nchs.traincollisionavoidancesystem.m_Model;

import lk.edu.nchs.traincollisionavoidancesystem.m_Helper.DataControl;

/**
 * Created by Dulkith on 6/8/18.
 */

public class Data {

//    private int doorStatus;
//    private double doorLocationLatitude;
//    private double doorLocationLongitude;
//    private String doorLocationAccuracy;
//    private double autoDoorDistance;
//
//    private int bulb1Status;
//    private int bulb2Status;
//    private int bulb1Sensative;
//    private int bulb2Sensative;
//
//    private int systemMode;
//    private int userPassCode;
//    private int online;
//    private int saveLocation;
//
//    public Data() {
//    }
//
//    public Data(int doorStatus, double doorLocationLatitude, double doorLocationLongitude, String doorLocationAccuracy, double autoDoorDistance, int bulb1Status, int bulb2Status, int bulb1Sensative, int bulb2Sensative, int systemMode, int userPassCode, int online, int saveLocation) {
//        this.doorStatus = doorStatus;
//        this.doorLocationLatitude = doorLocationLatitude;
//        this.doorLocationLongitude = doorLocationLongitude;
//        this.doorLocationAccuracy = doorLocationAccuracy;
//        this.autoDoorDistance = autoDoorDistance;
//        this.bulb1Status = bulb1Status;
//        this.bulb2Status = bulb2Status;
//        this.bulb1Sensative = bulb1Sensative;
//        this.bulb2Sensative = bulb2Sensative;
//        this.systemMode = systemMode;
//        this.userPassCode = userPassCode;
//        this.online = online;
//        this.saveLocation = saveLocation;
//    }

    public static int getDoorStatus() {
        return DataControl.readInt("DOOR_STATUS");
    }

    public static void setDoorStatus(int doorStatus) {
        DataControl.writeInt("DOOR_STATUS",doorStatus);
    }

    public static double getDoorLocationLatitude() {
        return DataControl.readDouble("DOOR_LOCATION_LATITUDE");
    }

    public static void setDoorLocationLatitude(double doorLocationLatitude) {
        DataControl.writeDouble("DOOR_LOCATION_LATITUDE",doorLocationLatitude);
    }

    public static double getDoorLocationLongitude() {
        return DataControl.readDouble("DOOR_LOCATION_LONGITUDE");
    }

    public static void setDoorLocationLongitude(double doorLocationLongitude) {
        DataControl.writeDouble("DOOR_LOCATION_LONGITUDE",doorLocationLongitude);
    }

    public static String getDoorLocationAccuracy() {
        return String.valueOf(DataControl.readInt("LOCATION_ACCURACY"));
    }

    public static double getAutoDoorDistance() {
        return DataControl.readDouble("AUTO_DOOR_DISTANCE");
    }

    public static void setAutoDoorDistance(double autoDoorDistance) {
        DataControl.writeDouble("AUTO_DOOR_DISTANCE",autoDoorDistance);
    }

    public static int getBulb1Status() {
        return DataControl.readInt("BULB_1_STATUS");
    }

    public static void setBulb1Status(int bulb1Status) {
        DataControl.writeInt("BULB_1_STATUS",bulb1Status);
    }

    public static int getBulb2Status() {
        return DataControl.readInt("BULB_2_STATUS");
    }

    public static void setBulb2Status(int bulb2Status) {
        DataControl.writeInt("BULB_2_STATUS",bulb2Status);
    }

    public static int getBulb1Sensitive() {
        return DataControl.readInt("BULB_1_SENSITIVE");
    }

    public static void setBulb1Sensitive(int bulb1Sensitive) {
        DataControl.writeInt("BULB_1_SENSITIVE",bulb1Sensitive);
    }

    public static int getBulb2Sensitive() {
        return DataControl.readInt("BULB_2_SENSITIVE");
    }

    public static void setBulb2Sensitive(int bulb2Sensitive) {
        DataControl.writeInt("BULB_2_SENSITIVE",bulb2Sensitive);
    }

    public static int getSystemMode() {
        return DataControl.readInt("MODE");
    }

    public static void setSystemMode(int systemMode) {
        DataControl.writeInt("MODE",systemMode);
    }

    public static String getUserPassCode() {
        return DataControl.readString("PASS_CODE");
    }

    public static void setUserPassCode(String userPassCode) {
        DataControl.writeString("PASS_CODE",userPassCode);
    }

    public static int getOnline() {
        return DataControl.readInt("ONLINE");
    }

    public static void setOnline(int online) {
        DataControl.writeInt("ONLINE",online);
    }

    public static int getSaveLocation() {
        return DataControl.readInt("SAVE_LOCATION");
    }

    public static void setSaveLocation(int saveLocation) {
        DataControl.writeInt("SAVE_LOCATION",saveLocation);
    }
}
