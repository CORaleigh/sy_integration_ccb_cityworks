package com.example.switchyard.cw_fa_from_ccb;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;

public interface InvokeCWUpdateWOServiceInterface {
	@POST
    @Consumes({"application/json"})
	JsonObject updateWO(JsonObject value);
}
