/*******************************************************************************
\ * Copyright (C) Landed Experts Technologies Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Kazem Naderi - 2019
 ******************************************************************************/
package com.landedexperts.letlock.chat.dto;

public class ReturnCodeMessageResponse {

    protected String returnCode = "SUCCESS";

    protected String returnMessage = "";

    public ReturnCodeMessageResponse(final String returnCode, final String returnMessage) {
        this.returnCode = returnCode != null ? returnCode : "";
        this.returnMessage = returnMessage != null ? returnMessage : "";
    }

    public ReturnCodeMessageResponse() {

    }

    public String getReturnCode() {
        return this.returnCode;
    }

    public String getReturnMessage() {
        return this.returnMessage;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }
}
