package com.example.switchyard.cw_fa_from_ccb;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


@Path("/")
public interface HandleCCBFADataInterfaceResource {
	
	@POST
	Response getMessage(String messagebody);
}
