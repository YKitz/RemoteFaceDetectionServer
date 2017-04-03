package myMain;


import com.example.yannic.remotefacedetection.agent.FaceDetectionAgent;
import com.example.yannic.remotefacedetection.agent.TestAgent;

import jadex.base.PlatformConfiguration;
import jadex.base.RootComponentConfiguration;
import jadex.base.Starter;

public class Main {

	public static void main(String[] args) {
		PlatformConfiguration config  = PlatformConfiguration.getDefault();
        RootComponentConfiguration rootConfig = config.getRootConfig();
 		rootConfig.setKernels(RootComponentConfiguration.KERNEL.micro);
		rootConfig.setAwareness(true);
		rootConfig.setNetworkName("OpenCVTestNetwork");
		rootConfig.setNetworkPass("testpw");
//		
     //   config.setPlatformName("OpenCVRemote");

		 
	       config.addComponent(FaceDetectionAgent.class);
	       config.addComponent(TestAgent.class);
	        Starter.createPlatform(config).get();

	}

}
