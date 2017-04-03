package com.example.yannic.remotefacedetection.agent;


import java.util.List;

import jadex.bridge.service.annotation.Security;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;


//@Security(Security.UNRESTRICTED)
public interface FaceDetectionService {

	
	String test();
	
	
	Future<List<Integer>> getFrame(int height, int width, byte[] data);
	
	
}
