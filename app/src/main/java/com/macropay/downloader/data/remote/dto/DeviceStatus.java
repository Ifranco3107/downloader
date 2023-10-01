package com.macropay.downloader.data.remote.dto;

import com.google.gson.Gson;

public class DeviceStatus {
    private String imei;
    private String lock_id;
    private String nivel_bloqueo;
    private String ult_fec_act;
    private String ult_fec_syncmovil;
    private String id_error;
    private String desc_error;
    private String locked;
    private String trans_id;
    private String user_id;

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getLock_id() {
        return lock_id;
    }

    public void setLock_id(String lock_id) {
        this.lock_id = lock_id;
    }

    public String getUlt_fec_act() {
        return ult_fec_act;
    }

    public void setUlt_fec_act(String ult_fec_act) {
        this.ult_fec_act = ult_fec_act;
    }

    public String getId_error() {
        return id_error;
    }

    public void setId_error(String id_error) {
        this.id_error = id_error;
    }

    public String getDesc_error() {
        return desc_error;
    }

    public void setDesc_error(String desc_error) {
        this.desc_error = desc_error;
    }

    public String getUlt_fec_syncmovil() {
        return ult_fec_syncmovil;
    }

    public void setUlt_fec_syncmovil(String ult_fec_syncmovil) {
        this.ult_fec_syncmovil = ult_fec_syncmovil;
    }

    public String getLocked() {
        return locked;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }

    public String getNivel_bloqueo() {
        return nivel_bloqueo;
    }

    public void setNivel_bloqueo(String nivel_bloqueo) {
        this.nivel_bloqueo = nivel_bloqueo;
    }

    public String getTrans_id() {
        return trans_id;
    }

    public void setTrans_id(String trans_id) {
        this.trans_id = trans_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }
}
