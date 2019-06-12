package com.fallntic.jotaayumouride;

import java.io.Serializable;

class Commission implements Serializable {

    private String dahiraId;
    private String commissionId;
    private String commissionName;
    private String commissionResponsible;

    public Commission(){}

    public Commission(String dahiraId, String commissionId, String commissionName, String commissionResponsible) {
        this.dahiraId = dahiraId;
        this.commissionId = commissionId;
        this.commissionResponsible = commissionResponsible;
    }

    public String getDahiraId() {
        return dahiraId;
    }

    public String getCommissionId() {
        return commissionId;
    }

    public String getCommissionName() {
        return commissionName;
    }

    public String getCommissionResponsible() {
        return commissionResponsible;
    }
}
