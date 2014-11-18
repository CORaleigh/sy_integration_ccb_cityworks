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
			
			String FAType = request.getFAType();
			String instructions = request.getInstructions();
			System.out.println(FAType + " " + instructions);
			
			
			//  Cityworks expects json, so we need to convert CCB xml to json
			//  Build the json object from the unmarshalled xml message from CCB

			Map config = new HashMap();
			JsonBuilderFactory factory = Json.createBuilderFactory(config);
			String meterHeader = "meterHeader";
			JsonObject value = factory.createObjectBuilder()
					.add(meterHeader,  factory.createObjectBuilder()
				     .add("fieldActivityId", request.getFieldActivityId())
				     .add("fieldActivityType", factory.createObjectBuilder()
				     .add("code", request.getFAType()))
				     .add("customerName", request.getCustomerName())
				     .add("customerPhone", request.getCustomerPhoneNumber())
				     .add("lifeSupport", "false")
				     .add("currentMeter", factory.createObjectBuilder()
				    	 .add("meterId", request.getMeterBadgeNumber())
					     .add("removeMeter", "false") 	// is this step 1?
					     .add("performDeviceTest", "null")  // is this step 2?
					     .add("compoundMeter", "false") // How to tell we have a compound meter? Registers is of type Registers, need to deal with that
					     .add("readDateTime", "true") // bug on Mike's side
					     .add("size", factory.createObjectBuilder().add("code", "A")) // needs to be the code, not descr request.getMeterSize()
					     .add("register1", factory.createObjectBuilder() //Registers is of type Registers, need to deal with that
				    		 .add("reading", "170")
				    		 .add("dials", "4")
				    		 .add("miu", "1491097636")
				    		 .add("readType", factory.createObjectBuilder().add("code", "60"))
				    		 .add("mrSource", factory.createObjectBuilder().add("code", "MTR-LEAK4"))
				    		 .add("size", factory.createObjectBuilder().add("code", "A")) 
				    		 .add("lowReadThreshold", "256")
				    		 .add("highReadThreshold","357"))
				    	 //.add("register2", "null")
				    		 .addNull("register2")
				    	 //.add("deviceTest", "null")) // must be a true null
				    	 	.addNull("deviceTest"))
				     //.add("installMeter", "null")
				     .addNull("installMeter")
				     .add("streetAddress", request.getServiceAddress())
				     .add("faInstructions", "faInstructions")
				     .add("faComments", "faComments")
				     .add("spLocationDetails", "spLocationDetails")
				     .add("spType", request.getServicePointType())
				     .add("premiseType", request.getPremiseType())
				     .add("township", request.getTownship())
				     .add("cityLimit", request.getCityLimit())
				     .add("useClass", request.getUseClass())
				     .add("postal", request.getZipCode())
				     .add("faRemark", factory.createObjectBuilder().add("code","X-NOTES-CM"))
				     .add("spSourceStatus", factory.createObjectBuilder().add("code","C"))
				     .add("disconnectLocation", factory.createObjectBuilder().add("code","METR"))
				     //.add("adjustmentType", "null")
				     .addNull("adjustmentType")
				     //.add("letterValue", "null")
				     .addNull("letterValue")
				     //.add("toDoValue", "null")
				     .addNull("toDoValue")
				     .add("workOrderId", "1234"))
				     .add("workOrder", factory.createObjectBuilder().add("workOrderId", "woID")
				    		 .add("description", "Meter - Leaking - Check and Repair")
				    		 .add("supervisor", "null")
				    		 .add("requestedBy", "null")
				    		 .add("initiatedBy", "null")
				    		 .add("initiateDate", "2014-11-17")
				    		 .add("location", "null")
				    		 .add("projectStartDate", "2014-11-17")
				    		 .add("projectFinishDate", "2014-12-01")
				    		 .add("priority", "50")
				    		 .add("numDaysBefore", "1")
				    		 .add("woCategory", "null")
				    		 .add("status", "Initiated")
				    		 .add("woTemplateId", "2")
				    		 .add("woAddress", "604 SARVER CT")
				    		 .add("woXCoordinate", "35.764184343")
				    		 .add("woYCoordinate", "-78.65269062"))
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
