/*
 * Copyright 2016 ASAP.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package gr.ntua.cslab.asap.client;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;

public class WorkflowClient extends RestClient{

    public WorkflowClient() {
        super();

    }

	public void addAbstractWorkflow(AbstractWorkflow1 abstractWorkflow) throws Exception {

        StringWriter writer = new StringWriter();
		JAXBContext jaxbContext = JAXBContext.newInstance( WorkflowDictionary.class );
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
		jaxbMarshaller.marshal( abstractWorkflow.toWorkflowDictionaryRecursive("<br>"), writer );
        String params=writer.toString();
        System.out.println(params);
        System.out.println(abstractWorkflow.name);
		issueRequest("POST", "abstractWorkflows/add/"+abstractWorkflow.name, params);
	}

	public void removeAbstractWorkflow(String name) throws Exception {
		issueRequest("GET", "abstractWorkflows/remove/"+name, null);
	}

	public String materializeWorkflow(String name, String policy) throws Exception {
		return issueRequest("GET", "abstractWorkflows/materialize/"+name+"?policy="+URLEncoder.encode(policy,"UTF-8"), null);
	}
	
	public String materializeWorkflowWithParameters(String name, String policy, String parameters) throws Exception {
		return issueRequest("GET", "abstractWorkflows/materializeWithParams/"+name+"?policy="+URLEncoder.encode(policy,"UTF-8")
				+"&parameters="+URLEncoder.encode(parameters,"UTF-8"), null);
	}
	
	public WorkflowDictionary getAbstractWorkflowDescription(String name) throws Exception {
		String ret = issueRequest("GET", "abstractWorkflows/XML/"+name, null);
		JAXBContext jaxbContext = JAXBContext.newInstance( WorkflowDictionary.class );
		Unmarshaller u = jaxbContext.createUnmarshaller();
		StringBuffer xmlStr = new StringBuffer( ret );
		WorkflowDictionary wd = (WorkflowDictionary) u.unmarshal( new StreamSource( new StringReader( xmlStr.toString() ) ) );
		return wd;
	}
	
	public WorkflowDictionary getMaterializedWorkflowDescription(String name) throws Exception {
		String ret = issueRequest("GET", "workflows/XML/"+name, null);
		JAXBContext jaxbContext = JAXBContext.newInstance( WorkflowDictionary.class );
		Unmarshaller u = jaxbContext.createUnmarshaller();
		StringBuffer xmlStr = new StringBuffer( ret );
		WorkflowDictionary wd = (WorkflowDictionary) u.unmarshal( new StreamSource( new StringReader( xmlStr.toString() ) ) );
		return wd;
	}
	
	public WorkflowDictionary getRunningWorkflowDescription(String name) throws Exception {
		String ret = issueRequest("GET", "runningWorkflows/XML/"+name, null);
		JAXBContext jaxbContext = JAXBContext.newInstance( WorkflowDictionary.class );
		Unmarshaller u = jaxbContext.createUnmarshaller();
		StringBuffer xmlStr = new StringBuffer( ret );
		WorkflowDictionary wd = (WorkflowDictionary) u.unmarshal( new StreamSource( new StringReader( xmlStr.toString() ) ) );
		return wd;
	}
	
	public String getState(String name) throws Exception {
		String ret = issueRequest("GET", "runningWorkflows/"+name+"/state", null);
		return ret;
	}
	

	public Boolean waitForCompletion(String name) throws Exception {
		boolean running=true;
		while(running){
			String state = getState(name);
			if(state.contains("FINISHED SUCCEEDED")){
				running =false;
				return true;
			}
			if(state.contains("FAILED")){
				running =false;
				return false;
			}
			System.out.println(state);
			Thread.sleep(500);
		}
		return true;
	}
	
	public String executeWorkflow(String name) throws Exception {
		return issueRequest("GET", "abstractWorkflows/execute/"+name, null);
	}
	
	public void removeMaterializedWorkflow(String name) throws Exception {
		issueRequest("GET", "workflows/remove/"+URLEncoder.encode(name,"UTF-8"), null);
	}
}
