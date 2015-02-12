package com.example.switchyard.cw_fa_from_ccb;

import java.math.BigInteger;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;

public interface InvokeCWDeleteWOServiceInterface {
	@POST
    @Consumes({"application/json"})
	JsonObject deleteWO(BigInteger externalID);
}
