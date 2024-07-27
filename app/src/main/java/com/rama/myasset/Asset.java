package com.rama.myasset;

import java.util.Date;

public class Asset {
    public Asset(){

    }

    String id, asset_name, description, status, user_input, user_edit, image1, image2, image3, image4;
    Date time_input, time_edit;

    public String getId() {
        return id;
    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getImage3() {
        return image3;
    }

    public void setImage3(String image3) {
        this.image3 = image3;
    }

    public String getImage4() {
        return image4;
    }

    public void setImage4(String image4) {
        this.image4 = image4;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAsset_name() {
        return asset_name;
    }

    public void setAsset_name(String asset_name) {
        this.asset_name = asset_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUser_input() {
        return user_input;
    }

    public void setUser_input(String user_input) {
        this.user_input = user_input;
    }

    public Date getTime_input() {
        return time_input;
    }

    public void setTime_input(Date time_input) {
        this.time_input = time_input;
    }

    public String getUser_edit() {
        return user_edit;
    }

    public void setUser_edit(String user_edit) {
        this.user_edit = user_edit;
    }

    public Date getTime_edit() {
        return time_edit;
    }

    public void setTime_edit(Date time_edit) {
        this.time_edit = time_edit;
    }
}
