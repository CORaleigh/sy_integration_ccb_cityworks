package com.example.switchyard.cw_fa_from_ccb;

import gov.raleigh.employeeservice.service.impl.RequestMessage;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.switchyard.component.bean.Reference;
import org.switchyard.component.bean.Service;

import com.fasterxml.jackson.jr.ob.JSON;



@Service(HandleCCBFADataInterface.class)
public class HandleCCBFADataInterfaceBean_Impl implements
		HandleCCBFADataInterface {
	
	@Inject
	@Reference
	private InvokeCWCreateWOServiceInterface invokeCWCreateWOServiceInterface;
	
	@Override
	public String getMessage(String messagebody) {
		String woID = "";
		try {
			JAXBContext jaxc = JAXBContext.newInstance("gov.raleigh.employeeservice.service.impl");
			Unmarshaller unmarshaller = jaxc.createUnmarshaller();
			StringReader reader = new StringReader(messagebody);
			RequestMessage request = (RequestMessage) unmarshaller.unmarshal(reader);
						
			
			
			Map config = new HashMap();
			JsonBuilderFactory factory = Json.createBuilderFactory(config);
			JsonObject value = factory.createObjectBuilder()
					.add("meterHeader",  factory.createObjectBuilder()
				     .add("fieldActivityId", request.getFieldActivityId()) 
				     .add("fieldActivityType", factory.createObjectBuilder()
				     .add("code", request.getFAType()))
				     .add("customerName", request.getCustomerName())
				     .add("customerPhone", (JsonValue) ((request.getCustomerPhoneNumber() == null) ? JsonValue.NULL : request.getCustomerPhoneNumber()))
				     .add("lifeSupport", "false")  // need to add this to CCB
				     .add("currentMeter", factory.createObjectBuilder()
				    	 .add("meterId", request.getMeterBadgeNumber())  // need to add meter id, not badge number
					     .addNull("removeMeter") 	// always null
					     .addNull("performDeviceTest")  // always null
					     .add("compoundMeter", "false") // Need to add logic that if more than one register, compound = true
					     .addNull("readDateTime") // bug on Mike's side, set it to null
					     .add("size", factory.createObjectBuilder()
					    	 .add("code", request.getRegisters().getRegisterSize())) // needs to be the code, not descr request.getMeterSize() - can this be register1.registerSize?
					    	 .add("register1", factory.createObjectBuilder() //Registers is of type Registers, need to deal with that
				    		 .add("reading", request.getRegisters().getLastMeterRead().toBigInteger()) // current reading? 
				    		 .add("dials", "4") // need to add
				    		 .add("miu", request.getRegisters().getMIU().toString()) 
				    		 .add("readType", factory.createObjectBuilder().add("code", "60")) 
				    		 .add("mrSource", factory.createObjectBuilder().add("code", "MTR-LEAK4"))
				    		 .add("size", factory.createObjectBuilder().add("code", "A")) 
				    		 .add("lowReadThreshold", request.getRegisters().getLowMeterReadingWarning().toBigInteger())
				    		 .add("highReadThreshold",request.getRegisters().getHighMeterReadingWarning().toBigInteger()))
				    		 .addNull("register2") // if compound = true, then we will have data for this, so check for null first
				    	 	 .addNull("deviceTest"))  // always null
				     .addNull("installMeter") // always null
				     .add("streetAddress", request.getServiceAddress())
				     .add("faInstructions", request.getInstructions()) 
				     .add("faComments", "faComments") // always null
				     .addNull("spLocationDetails") // always null
				     .add("spType", request.getServicePointType())
				     .add("premiseType", request.getPremiseType())
				     .add("township", request.getTownship())
				     .add("cityLimit", request.getCityLimit())
				     .add("useClass", request.getUseClass())
				     .add("postal", request.getZipCode())
				     .add("faRemark", factory.createObjectBuilder().addNull("code")) // set this to null, only get this from CW
				     .add("spSourceStatus", factory.createObjectBuilder().add("code","C")) // need to send the code, not the descr
				     .add("disconnectLocation", factory.createObjectBuilder().add("code","METR"))
				     .addNull("adjustmentType")
				     .addNull("letterValue")
				     .addNull("toDoValue") 
				     .add("workOrderId", "1234")) // should always be null, unless this is an update.
				     .add("workOrder", factory.createObjectBuilder().addNull("workOrderId")  // should always be null
				    		 .add("description", request.getDescription()) 
				    		 .add("supervisor", "null")
				    		 .add("requestedBy", "null")
				    		 .add("initiatedBy", "null")
				    		 .add("initiateDate", "2014-11-17")
				    		 .add("location", "null")
				    		 .add("projectStartDate", "2014-11-17")
				    		 .add("projectFinishDate", "2014-12-01")
				    		 .add("priority", "50") // pass this null 
				    		 .add("numDaysBefore", "1")
				    		 .add("woCategory", "null")
				    		 .add("status", request.getStatus()) // need a crosswalk here?  "Initiated" vs. "Pending" this might bomb out b/c 
				    		 .add("woTemplateId", "2") // will we need to look this up?
				    		 .add("woAddress", request.getServiceAddress())
				    		 .add("woXCoordinate", request.getXCoordinate())
				    		 .add("woYCoordinate", request.getYCoordinate()))
				     .build();
			
			// ugly print the json
			System.out.println("json sample " + value +"\n");
			
			JsonObject response = invokeCWCreateWOServiceInterface.createWO(value);
			woID = response.getString("message");
			
			System.out.println("response " + response +"\n" + "woID = " + woID);
			

		} catch (JAXBException e) {
			e.printStackTrace();
		} 

		//System.out.println("inbound Message Body = "+messagebody);
		return "<ResponseMessage><WorkOrderId>" + woID + "</WorkOrderId></ResponseMessage>";
		//return "I am your father's brother's sister's former roomate";

	}

}
