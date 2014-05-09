
package test.custom.adapter;

public class Device {
		   
	    private String m_szDeviceName;
	    private String m_szDeviceAddress;
	    private int m_nDeviceType;
	    private int m_nDeviceStatus;
	    private int m_nDeviceID;

	    public Device( String deviceName, String deviceAddress, int deviceType, int deviceStatus, int deviceID ) {
	        this.m_szDeviceName = deviceName;
	        this.m_szDeviceAddress = deviceAddress;
	        this.m_nDeviceType = deviceType;
	        this.m_nDeviceStatus = deviceStatus;
	        this.m_nDeviceID = deviceID;
	      }


	    public String getDeviceName() { return m_szDeviceName; }
	    public void setDeviceName(String deviceName) { this.m_szDeviceName = deviceName;}
	    
	    public String getDeviceAddress() {return m_szDeviceAddress;}
	    public void setDeviceAddress(String deviceAddress) {this.m_szDeviceAddress = deviceAddress;}
	    
	    public int getDeviceType() { return m_nDeviceType; }
	    public void setDeviceType(int deviceType) { this.m_nDeviceType = deviceType;}
	    
	    public int getDeviceStatus() { return m_nDeviceStatus; }
	    public void setDeviceStatus(int deviceStatus) { this.m_nDeviceStatus = deviceStatus;}
	    
	    public int getDeviceID() { return m_nDeviceID; }
	    public void setDeviceID(int deviceID) { this.m_nDeviceID = deviceID;}
}