package com.example.yannic.remotefacedetection.agent;


import java.util.List;

import jadex.bridge.service.annotation.Security;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.Tuple2Future;


//@Security(Security.UNRESTRICTED)
public interface FaceDetectionService {

	
	String test();
	
	
	Tuple2Future<List<Integer>, byte[]> getFrame(int id, byte[] data);
	
	
}
