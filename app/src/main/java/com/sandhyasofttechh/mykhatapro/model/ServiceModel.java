package com.sandhyasofttechh.mykhatapro.model;

import java.io.Serializable;

public class ServiceModel implements Serializable {

    public String serviceId;
    public String serviceName;
    public String price;
    public String unit;
    public String sacCode;
    public String gst;
    public String imageUrl;
    public String dateAdded;
    public String timeAdded;
    public long timestamp;

    public ServiceModel() {} // required empty constructor
}
