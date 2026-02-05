package com.happy.tracku.gson.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ValidateLoginResponsestatus {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("idEmployee")
    @Expose
    private Integer idEmployee;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("pin")
    @Expose
    private String pin;
    @SerializedName("contactNumber")
    @Expose
    private String contactNumber;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("loginID")
    @Expose
    private Integer loginID;
    @SerializedName("employeeCode")
    @Expose
    private Integer employeeCode;
    @SerializedName("isAdmin")
    @Expose
    private Integer isAdmin;
    @SerializedName("statusMessage")
    @Expose
    private String statusMessage;
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("isLocationCheckRequired")
    @Expose
    private Integer isLocationCheckRequired;
    @SerializedName("location")
    @Expose
    private String location;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(Integer idEmployee) {
        this.idEmployee = idEmployee;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getLoginID() {
        return loginID;
    }

    public void setLoginID(Integer loginID) {
        this.loginID = loginID;
    }

    public Integer getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(Integer employeeCode) {
        this.employeeCode = employeeCode;
    }

    public Integer getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Integer isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getIsLocationCheckRequired() {
        return isLocationCheckRequired;
    }

    public void setIsLocationCheckRequired(Integer isLocationCheckRequired) {
        this.isLocationCheckRequired = isLocationCheckRequired;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
