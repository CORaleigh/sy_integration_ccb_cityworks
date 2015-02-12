package com.example.switchyard.cw_fa_from_ccb;

import gov.raleigh.employeeservice.service.impl.RequestMessage;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.switchyard.component.bean.Reference;
import org.switchyard.component.bean.Service;


@Service(HandleCCBFADataInterface.class)
public class HandleCCBFADataInterfaceBean_Impl implements
		HandleCCBFADataInterface {
	
	@Inject
	@Reference
	private InvokeCWCreateWOServiceInterface invokeCWCreateWOServiceInterface;
	
	@Inject
	@Reference
	private InvokeCWUpdateWOServiceInterface invokeCWUpdateWOServiceInterface;

	@Inject
	@Reference
	private InvokeCWDeleteWOServiceInterface invokeCWDeleteWOServiceInterface;
	
	@Override
	public String getMessage(String messagebody) {
		String woID = "";
		String canceledStatus = "Canceled";
		try {
			JAXBContext jaxc = JAXBContext.newInstance("gov.raleigh.employeeservice.service.impl");
			Unmarshaller unmarshaller = jaxc.createUnmarshaller();
			StringReader reader = new StringReader(messagebody);
			RequestMessage request = (RequestMessage) unmarshaller.unmarshal(reader);
			
			String FAType = request.getFAType();
			String instructions = request.getInstructions();

			
			BigInteger externalID = request.getExternalID();
			
			String statusIn = request.getStatus();
			
			if (statusIn .equals(canceledStatus)){
				 statusIn = "CANCEL";
			}
				else {
					statusIn = request.getStatus();
				}		
			
			if (instructions == null){
				instructions = " ";
			}
			
			String comments = " ";
			
			String spLocationDetail = request.getMeterLocation();
			
			if (spLocationDetail == null)
				spLocationDetail = " ";
			
			String customerPhone = request.getCustomerPhoneNumber();
			if (customerPhone == null)
				customerPhone = " ";
			
			System.out.println("FAType " + FAType);
			System.out.println("Instructions " + instructions);
			System.out.println("Status In " + statusIn);
			System.out.println("Comments " + comments); 
			System.out.println("SP Location Detail " + spLocationDetail);
			System.out.println("Customer Phone " + customerPhone);
			System.out.println("ExternalID "+ externalID);			
		    System.out.println("Field Activity ID " + request.getFieldActivityId());
		    System.out.println("CustomerName " + request.getCustomerName());
		    System.out.println("MeterId " +  request.getMeterID()); 
		    System.out.println("Badge Number " + request.getMeterBadgeNumber());
			System.out.println("Meter Size " + request.getMeterSize());
		    System.out.println("SPId "+ request.getServicePointId());
			System.out.println("Register1");
		    System.out.println("Reading " + request.getRegisters().getLastMeterRead().toBigInteger());
		    System.out.println("Dials " +  request.getRegisters().getDials());
		    System.out.println("Miu " + request.getRegisters().getMIU());
		    System.out.println("ReadType " + request.getRegisters().getReadType());
		    System.out.println("MrSource " + request.getRegisters().getMrSource());
		    System.out.println("Size " + request.getRegisters().getRegisterSize()); 
		    
		    if (externalID  == null) {
		    System.out.println("LowReadThreshold " + request.getRegisters().getLowMeterReadingWarning().toBigInteger());
		    System.out.println("HighReadThreshold " + request.getRegisters().getHighMeterReadingWarning().toBigInteger());
		    };
		    
		    System.out.println("StreetAddress " + request.getServiceAddress());
		    System.out.println("SPType " + request.getServicePointType());
		    System.out.println("PremiseType " + request.getPremiseType());
		    System.out.println("Township " + request.getTownship());
		    System.out.println("CityLimit " + request.getCityLimit());
		    System.out.println("UseClass " + request.getUseClass());
		    System.out.println("Postal " + request.getZipCode());
		    System.out.println("FaRemark " + "X-NOTES-CM");
		    System.out.println("SPSourceStatus " + request.getServicePointSourceStatus().substring(0, 1));
		    System.out.println("DisconnectLocation " + "METR");
		    System.out.println("WorkOrderId " + "1");
		    System.out.println("Description " + request.getDescription());
		    System.out.println("InitiateDate " + request.getScheduledDate());
		    System.out.println("WoAddress " + request.getServiceAddress());
		    System.out.println("WoXCoordinate " + request.getXCoordinate());
		    System.out.println("WoYCoordinate " + request.getYCoordinate());

			
			if (externalID != null ){
				System.out.println("This is an Update");
				Map config = new HashMap();
				JsonBuilderFactory factory = Json.createBuilderFactory(config);
				String meterHeader = "MeterHeader";
				JsonObject value = factory.createObjectBuilder()
						.add(meterHeader,  factory.createObjectBuilder()
					     .add("FieldActivityId", request.getFieldActivityId())
					     .add("FieldActivityType", factory.createObjectBuilder()
					     .add("Code", request.getFAType()))
					     .add("CustomerName", request.getCustomerName())
					     .add("CustomerPhone", customerPhone)
					     .add("LifeSupport", "false")
     				     .add("CurrentMeter", factory.createObjectBuilder()
					    	 .add("MeterId", request.getMeterID())
					    	 .add("BadgeNumber", request.getMeterBadgeNumber())
						     .add("RemoveMeter", "false") 
						     .addNull("IsDeviceTest") 	
						     .add("CompoundMtr", "false") // How to tell we have a compound meter? Registers is of type Registers, need to deal with that
						     .add("ReadDateTime", "true") // bug on Mike's side
						     .add("Size", factory.createObjectBuilder().add("Code", request.getMeterSize()))
						    	 .add("Register1", factory.createObjectBuilder() //Registers is of type Registers, need to deal with that
					    		 .add("Reading", request.getRegisters().getLastMeterRead().toBigInteger())
					    		 .add("Dials", request.getRegisters().getDials())
					    		 .add("Miu", request.getRegisters().getMIU())
					    		 .add("ReadType", factory.createObjectBuilder().add("Code", request.getRegisters().getReadType()))
				 				 .add("MrSource", factory.createObjectBuilder().add("Code", request.getRegisters().getMrSource()))	    		 
					    		 .add("Size", factory.createObjectBuilder().add("Code", request.getRegisters().getRegisterSize()))) 
					    	 //.add("Register2", "null")
					    		 .addNull("Register2")
					    	 //.add("DeviceTest", "null")) // must be a true null
					    	 	.addNull("DeviceTest"))
					     //.add("installMeter", "null")
					     .addNull("InstallMeter")
					     .add("StreetAddress", request.getServiceAddress())
					     .add("FAInstructions", instructions)
					     .add("FAComments", comments)
    		//		     .add("SPLocationDetails", request.getMeterLocation())
    				     .add("SPLocationDetails", spLocationDetail)
					     .add("SPType", request.getServicePointType())
					     .add("PremiseType", request.getPremiseType())
					     .add("Township", request.getTownship())
					     .add("CityLimit", request.getCityLimit())
					     .add("UseClass", request.getUseClass())
					     .add("Postal", request.getZipCode())
					     .add("FARemark", factory.createObjectBuilder().add("Code","X-NOTES-CM"))
			    	     .add("SPSourceStatus", factory.createObjectBuilder().add("Code", request.getServicePointSourceStatus().substring(0, 1)))
					     .add("DisconnectLocation", factory.createObjectBuilder().add("Code","METR"))
					     .addNull("DispatchGroup")
					     .add("SPId", request.getServicePointId())
					     .addNull("StockLocation")
					     .addNull("AdjustmentType")
					     .addNull("LetterType")
					     .addNull("ToDoType")
					     .addNull("AdjustmentValue")
					     .addNull("LetterValue")
					     .addNull("ToDoValue")
					     .add("WorkOrderId", request.getExternalID()))
					     .add("WorkOrder", factory.createObjectBuilder().add("WorkOrderId", request.getExternalID())
					    		 .add("Description", request.getDescription())
					    		 .add("Supervisor", "")
					    		 .add("RequestedBy", "")
					    		 .add("InitiatedBy", "")
					    		 .add("InitiateDate", request.getScheduledDate())
					    		 .add("Location", "")
					    		 .add("ProjectStartDate", "")
					    		 .add("ProjectFinishDate", "")
					    		 .add("Priority", "")
					    		 .add("NumDaysBefore", "1")
					    		 .add("WoCategory", "")
					    		 .add("Status", statusIn)
					    		 .add("WoTemplateId", "257702")
					    		 .add("WoAddress", request.getServiceAddress())
					    		 .add("WoXCoordinate", request.getXCoordinate())
					    		 .add("WoYCoordinate", request.getYCoordinate()))
					     .build();
				
				       System.out.println("External ID " + externalID);
				       System.out.println("Status In " + statusIn +"\n");
				
					   JsonObject response = invokeCWUpdateWOServiceInterface.updateWO(value);	
					   System.out.println("response from update " + response);
					   System.out.println("json update sample " + value +"\n");
			
				    return "<ResponseMessage><WorkOrderId>" + request.getExternalID() + "</WorkOrderId></ResponseMessage>";

			}				    
							
				
			//	if (externalID != null && statusIn.trim().equals(canceledStatus)){
			//		Map config = new HashMap();
			//		JsonBuilderFactory factory = Json.createBuilderFactory(config);
			//		JsonObject value = factory.createObjectBuilder()
			//		.add("workOrder", factory.createObjectBuilder().add("workOrderId", request.getExternalID()))
			//		.build();  		
			//		System.out.println("json cancelled before " + value +"\n");
			//		System.out.println("json cancelled before " );
			//		JsonObject response = invokeCWDeleteWOServiceInterface.deleteWO(request.getExternalID());	
			//		System.out.println("response from canceled " + response);
			//		System.out.println("json cancelled after ");
			
			//	return "<ResponseMessage><WorkOrderId>" + request.getExternalID() + "</WorkOrderId></ResponseMessage>";
			// } 
						

			
			//  Cityworks expects json, so we need to convert CCB xml to json
			//  Build the json object from the unmarshalled xml message from CCB
			
			if (externalID == null) {
			System.out.println("This is an Insert");	
			Map config = new HashMap();
			JsonBuilderFactory factory = Json.createBuilderFactory(config);
			String meterHeader = "MeterHeader";
			JsonObject value = factory.createObjectBuilder()
					.add(meterHeader,  factory.createObjectBuilder()
				     .add("FieldActivityId", request.getFieldActivityId())
				     .add("FieldActivityType", factory.createObjectBuilder().add("Code", request.getFAType()))
				     .add("CustomerName", request.getCustomerName())
				     .add("CustomerPhone", customerPhone)
				     .add("LifeSupport", "false")
				     .add("CurrentMeter", factory.createObjectBuilder()
				    	 .add("MeterId", request.getMeterID())
				    	 .add("BadgeNumber", request.getMeterBadgeNumber())
					     .add("RemoveMeter", "false")
					     .addNull("IsDeviceTest") 
					     .add("CompoundMtr", "false") // How to tell we have a compound meter? Registers is of type Registers, need to deal with that
					     .add("ReadDateTime", "true") // bug on Mike's side
					     .add("Size", factory.createObjectBuilder().add("Code", request.getMeterSize()))
					    	 .add("Register1", factory.createObjectBuilder() //Registers is of type Registers, need to deal with that
				    		 .add("Reading", request.getRegisters().getLastMeterRead().toBigInteger())
				    		 .add("Dials", request.getRegisters().getDials())
				    		 .add("Miu", request.getRegisters().getMIU())
				    		 .add("ReadType", factory.createObjectBuilder().add("Code", request.getRegisters().getReadType()))
				    		 .add("MrSource", factory.createObjectBuilder().add("Code", request.getRegisters().getMrSource()))
				    		 .add("Size", factory.createObjectBuilder().add("Code", request.getRegisters().getRegisterSize())) 
				    	     .add("LowReadThreshold", request.getRegisters().getLowMeterReadingWarning().toBigInteger())
				    		 .add("HighReadThreshold", request.getRegisters().getHighMeterReadingWarning().toBigInteger()))
				    	 //.add("Register2", "null")
				    		 .addNull("Register2")
				    	 //.add("DeviceTest", "null")) // must be a true null
				    	 	.addNull("DeviceTest"))
				     //.add("InstallMeter", "null")
				     .addNull("InstallMeter")
				     .add("StreetAddress", request.getServiceAddress())
				     .add("FAInstructions", instructions)
				     .add("FAComments", comments)
                     .add("SPLocationDetails", spLocationDetail)
				     .add("SPType", request.getServicePointType())
				     .add("PremiseType", request.getPremiseType())
				     .add("Township", request.getTownship())
				     .add("CityLimit", request.getCityLimit())
				     .add("UseClass", request.getUseClass())
				     .add("Postal", request.getZipCode())
				     .add("FARemark", factory.createObjectBuilder().add("Code","X-NOTES-CM"))
				     .add("SPSourceStatus", factory.createObjectBuilder().add("Code", request.getServicePointSourceStatus().substring(0, 1)))
				     .add("DisconnectLocation", factory.createObjectBuilder().add("Code","METR"))
				     .addNull("DispatchGroup")
				     .add("SPId", request.getServicePointId())
				     .addNull("StockLocation")
				     .addNull("AdjustmentType")
				     .addNull("LetterType")
	                 .addNull("ToDoType")
				     .addNull("AdjustmentValue")
				     .addNull("LetterValue")
				     .addNull("ToDoValue")
				     .add("WorkOrderId", "1"))
				     .add("WorkOrder", factory.createObjectBuilder().add("WorkOrderId", "1")
				    		 .add("Description", request.getDescription())
				    		 .add("Supervisor", "")
				    		 .add("RequestedBy", "")
				    		 .add("InitiatedBy", "")
				    		 .add("InitiateDate", request.getScheduledDate())
				    		 .add("Location", "")
				    		 .add("ProjectStartDate", "")
				    		 .add("ProjectFinishDate", "")
				    		 .add("Priority", "")
				    		 .add("NumDaysBefore", "1")
				    		 .add("WoCategory", "")
				       		 .add("SubmitTo", "")
				    		 .add("Status", "Initiated")
				    		 .add("WoTemplateId", "257702")
				    		 .add("WoAddress", request.getServiceAddress())
				    		 .add("WoXCoordinate", request.getXCoordinate())
				    		 .add("WoYCoordinate", request.getYCoordinate()))
				     .build();
			
			// ugly print the json
			System.out.println("json sample " + value +"\n");
			
			JsonObject response = invokeCWCreateWOServiceInterface.createWO(value);
			
			woID = response.getString("message");
			
			System.out.println("response " + response +"\n" + "woID = " + woID);
			}	


		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		//System.out.println("inbound Message Body = "+messagebody);
		return "<ResponseMessage><WorkOrderId>" + woID + "</WorkOrderId></ResponseMessage>";
		//return "I am your father's brother's sister's former roomate";

	}

}
