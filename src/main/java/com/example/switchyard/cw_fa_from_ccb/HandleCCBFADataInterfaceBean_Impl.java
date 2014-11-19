package com.example.switchyard.cw_fa_from_ccb;

import gov.raleigh.employeeservice.service.impl.RequestMessage;

import java.io.StringReader;
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

/*Sample CCB outbound message

<RequestMessage>
<FAType>SYS_BROK</FAType>
<Description>MRR - Box Broken</Description>
<Step1>10 - Replace/Repair Box</Step1>
<Step2>20 - Remove Meter (Optional)</Step2>
<Step3>30 - Install Meter (Optional)</Step3>
<ServiceAddress>712 WALLRIDGE DR</ServiceAddress>
<CustomerName>ELLINGTON,LAURA  </CustomerName>
<MeterSize>5/8" SP Meter Size</MeterSize>
<MeterModel>CCF Water Meter</MeterModel>
<FieldActivityId>7242000179</FieldActivityId>
<Status>Pending</Status>
<DispatchGroup>SMALL</DispatchGroup>
<ServicePointId>7242000004</ServicePointId>
<ServicePointType>WTR-WW-1</ServicePointType>
<ScheduledDate>2014-09-17</ScheduledDate>
<Instructions>Test for Broken Box</Instructions>
<Cycle>SC10</Cycle>
<Route>WF-12100</Route>
<Sequence>501443</Sequence>
<CustomerPhoneNumber>(919) 492-3628 </CustomerPhoneNumber>
<ZipCode>27587</ZipCode>
<ServicePointSourceStatus>Connected</ServicePointSourceStatus>
<ServicePointCreationDate>1998-12-02</ServicePointCreationDate>
<MeterInstallDate>2006-08-01</MeterInstallDate>
<PremiseType>Residential - Single Family</PremiseType>
<Township>WAKE FOREST</Township>
<CityLimit>INSIDE</CityLimit>
<UseClass>Residential - Single Family</UseClass>
<XCoordinate>36.000833312</XCoordinate>
<YCoordinate>-78.51216635</YCoordinate>
<FAClass>METER</FAClass>
<Priority>Priority 50</Priority>
<MeterBadgeNumber>83137130</MeterBadgeNumber>
<Registers>
  <registerId>8933300688</registerId>
  <readSequence>1</readSequence>
  <lowMeterReadingWarning>117.000000</lowMeterReadingWarning>
  <highMeterReadingWarning>147.000000</highMeterReadingWarning>
  <lastMeterRead>117.000000</lastMeterRead>
  <MIU>1471134942</MIU>
  <RegisterSize>A</RegisterSize>
</Registers>
</RequestMessage>

  
Sample Cityworks inbound Json

{
"meterHeader" : {
                "fieldActivityId" : "3190200267",
                "fieldActivityType" : { "code" : "MTR_LEAK" },
                "customerName" : "JACKSON,EDDIE L",
                "customerPhone" : null,  
                "lifeSupport" : "true",  
                "currentMeter" : {
                                "meterId" : "84890704",    
                                "removeMeter" : "false",
                                "performDeviceTest" : "false",
                                "compoundMeter" : "false",
                                "readDateTime" : null,
                                "size" : {"code" : "A"},     
                                "register1" : {
                                                "reading" : "170",   
                                                "dials" : "4",         
                                                "miu" : "1490154346",
                                                "readType" : {"code" : "60"},  
                                                "mrSource" : {"code" : "MTR-LEAK4"},
                                                "size" : {"code" : "A"},
                                                "lowReadThreshold" : "170",     
                                                "highReadThreshold" : "190"     
                                },
                                "register2" : null,
                                "deviceTest" : null
                },
                "installMeter" : null,
                "streetAddress" : "604 SARVER CT",
                "faInstructions" : "Testing CCB to Citworks WO Creation",
                "faComments" : "This is only a test FA",
                "spLocationDetails" : "Location Details",
                "spType" : "WTR-WW-1",
                "premiseType" : "Residential - Single Family",
                "township" : "RALEIGH",
                "cityLimit" : "INSIDE",
                "useClass" : "Residential - Single Family",
                "postal" : "27603",
                "faRemark" : { "code" :  "X-NOTES-CM"},
                "spSourceStatus" : {"code" : "C"},  
                "disconnectLocation" : { "code"  : "METR"},
                "adjustmentType" : null,
                "letterType" : null,
                "toDoType" : null,
                "adjustmentValue" : null,
                "letterValue" : null,
                "toDoValue" : null,
                "workOrderId": null
}
,				
	
"workOrder" :

{

                                "workOrderId" : "",
                                "description" : "Meter - Leaking - Check and Repair",
                               "supervisor" : "",
                               "requestedBy" : "",
                               "initiatedBy" : "",
                               "initiateDate" : "2014-11-01",
                               "location" : "",
                               "projectStartDate" : "2014-11-03",
                               "projectFinishDate" : "2014-12-01",
                               "priority" : "50",
                               "numDaysBefore" : "1",
                               "woCategory" : "",
                               "submitTo" : "",
                               "status" : "Initiated",
                               "woTemplateId" : "2",   
                                "woAddress" : "604 SARVER CT",
                                "woXCoordinate" : "35.764184343",
                                "woYCoordinate" : "-78.65269062"
                }
}
*/

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
						
			//  Cityworks expects json, so we need to convert CCB xml to json
			//  Build the json object from the unmarshalled xml message from CCB		
			
			/*meterHeader
			fieldActivityId
			fieldActivityType
			code
			customerName
			customerPhone
			lifeSupport
			currentMeter
			meterId
			removeMeter
			performDeviceTest
			compoundMeter
			readDateTime
			size
			code
			register1
			reading
			dials
			miu
			readType
			mrSource
			size
			lowReadThreshold
			highReadThreshold
			register2
			deviceTest
			installMeter
			streetAddress
			faInstructions
			faComments
			spLocationDetails
			spType
			premiseType
			township
			cityLimit
			useClass
			postal
			faRemark
			spSourceStatus
			disconnectLocation
			adjustmentType
			letterValue
			toDoValue
			workOrderId
			workOrder
			description
			supervisor
			requestedBy
			initiatedBy
			initiateDate
			location
			projectStartDate
			projectFinishDate
			priority
			numDaysBefore
			woCategory
			status
			woTemplateId
			woAddress
			woXCoordinate
			woYCoordinate*/
			
			// for deletewo - aka cancelling fa - pass in a woID - doesn't have to even be json, just pass in the ID
			// question for Jay - do we remove wo altogether, or just put in a cancelled status
			// if we have a legit status in CW, all we need to do is update status to Cancel.
			// don't need to use deletewo at all.
			
			// need to add lifesupport, if it's null, it's considered false
			// need to add meterid
			// current meter is only required for certain FA's
			// need to add meter size code
			// need to add readType - what is this? 
			// need to add meterRead source
			// we have register ID in our xml, but Mike doesn't seem to need it, does he?
			// deviceTest will always be null
			// is registersize == A the same as meter size
			// add field length and type for each as well
			// spLocationDetails - do we need to send this from CCB? or do we just get this back from CW
			// SPType is not being used in JSON, Mike's not doing anything with it yet.
			// Should we send Premise Type Code instead of descr? for same thing for UseClass 
			// Need to add code for spSourceStatus or we can just get the first character
			// need to add dials.
			// we are not passing disconnect location, should we?
			// workOrderId is always null on createwo
			// Jay - woTemplateId ,will we need to look this up?
   		    // .add("status", request.getStatus()) - need a crosswalk here?  "Initiated" vs. "Pending" this might bomb out b/c 	

			Map config = new HashMap();
			JsonBuilderFactory factory = Json.createBuilderFactory(config);
			JsonObject value = factory.createObjectBuilder()
					.add("meterHeader",  factory.createObjectBuilder()
				     .add("fieldActivityId", request.getFieldActivityId()) 
				     .add("fieldActivityType", factory.createObjectBuilder()
				     .add("code", request.getFAType()))
				     .add("customerName", request.getCustomerName())
				     .add("customerPhone", request.getCustomerPhoneNumber())
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
				    		 .add("reading", "170") // current reading? 
				    		 .add("dials", "4") // need to add
				    		 .add("miu", request.getRegisters().getMIU().toString()) 
				    		 .add("readType", factory.createObjectBuilder().add("code", "60")) 
				    		 .add("mrSource", factory.createObjectBuilder().add("code", "MTR-LEAK4"))
				    		 .add("size", factory.createObjectBuilder().add("code", "A")) 
				    		 .add("lowReadThreshold", "256")
				    		 .add("highReadThreshold","357"))
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
				     .add("faRemark", factory.createObjectBuilder().add("code","X-NOTES-CM")) // set this to null, only get this from CW
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
			
//			// pretty print the json
//			ObjectMapper mapper = new ObjectMapper();
//			
//			try {
//				System.out.println("sampleJsonOutput: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value));
//			} catch (JsonGenerationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JsonMappingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		//System.out.println("inbound Message Body = "+messagebody);
		return "<ResponseMessage><WorkOrderId>" + woID + "</WorkOrderId></ResponseMessage>";
		//return "I am your father's brother's sister's former roomate";

	}

}
