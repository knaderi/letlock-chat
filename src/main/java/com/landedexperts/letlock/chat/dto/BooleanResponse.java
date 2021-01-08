/*******************************************************************************
 * Copyright (C) Landed Experts Technologies Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Kazem Naderi - 2019
 ******************************************************************************/
package com.landedexperts.letlock.chat.dto;

public class BooleanResponse {
	

    private ResultObject result = new ResultObject();
	private String returnCode = "SUCCESS";

	private String returnMessage = "";

    public String getReturnMessage() {
		return returnMessage;
	}


	public void setReturnMessage(String returnMessage) {
		this.returnMessage = returnMessage;
	}


	public BooleanResponse(boolean resultValue, String returnCode, String returnMessage) {
        setValue(resultValue);
        this.returnCode = returnCode;
        this.returnMessage = returnMessage;
    }


	public String getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}
	
    public ResultObject getResult() {
        return result;
    }
    
    public void setValue(boolean value) {
        result.setValue(value);
    }
	
    public class ResultObject{
        private boolean value = false;
        public boolean getValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }

        public ResultObject() {
        }

    }
}
