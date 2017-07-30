package com.example.yannic.remotefacedetection.agent;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentFeature;
import jadex.micro.annotation.AgentService;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import jadex.bridge.service.RequiredServiceInfo;

@Agent
@RequiredServices(@RequiredService(name = "faceDetectionService", type = FaceDetectionService.class, binding = @Binding(scope = RequiredServiceInfo.SCOPE_GLOBAL)))
public class TestAgent {

	FaceDetectionService fds;
	// @AgentService
	// private FaceDetectionService fdi;

	// @AgentFeature
	// private IRequiredServicesFeature reqFeat;
	//
	//
	//
	// {
	// ISumService sum = reqFeat.getRequiredService("sum").get();s
	// }

	public void getTest(FaceDetectionService fds) {
		System.out.println(fds.test());
	}

	//
	@AgentBody
	public void executeBody(IInternalAccess agent) {
		fds = (FaceDetectionService) agent.getComponentFeature(IRequiredServicesFeature.class)
				.getRequiredService("faceDetectionService").get();
		System.out.println("Test Agent Body");
		getTest(fds);
	}

}
