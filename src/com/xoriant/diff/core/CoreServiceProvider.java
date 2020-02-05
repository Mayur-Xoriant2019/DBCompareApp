package com.xoriant.diff.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.diffkit.db.DKDBFlavor;
import org.diffkit.diff.conf.DKApplication;
import org.diffkit.model.DKRequestClient;
import org.diffkit.model.LHSDetail;
import org.diffkit.model.RHSDetail;
import org.springframework.web.multipart.MultipartFile;

import com.xoriant.diff.model.DKColumnResponse;
import com.xoriant.diff.model.DKRequest;
import com.xoriant.diff.model.DKResponse;
import com.xoriant.diff.model.DestinationDB;
import com.xoriant.diff.model.ResponseStatus;
import com.xoriant.diff.model.SourceDB;

public class CoreServiceProvider {

	private static final String UPLOAD_FOLDER = "D:"+File.separator+"DIffKitPlans"+File.separator;
	//private static final String UPLOAD_FOLDER = "/opt/db-comparision/DiffKitPlans/upload/";
	protected static String DOWNLOAD_FOLDER = "D:/eclipse/";
	
	public static String execute(String fileName) throws Throwable {
		
		DKApplication dkApp = new DKApplication();
		System.out.println("Executing plan: " +UPLOAD_FOLDER+fileName);
		Path path = Paths.get(UPLOAD_FOLDER+fileName);
		String responseFileName = dkApp.runDiffKit(path.toString());
		if(responseFileName != null)   //"testSqlServer.plan.xml"
			return responseFileName;
		
		return "Configuration Error";
	}
	
	public static DKRequestClient buildDiffKitRequest(DKRequest request) {
		SourceDB sourcedb = request.getSourcedb();
		DestinationDB destinationDB = request.getDestinationdb();
		LHSDetail lhsDetail = null;
		RHSDetail rhsDetail = null;

		DKRequestClient requestClient = new DKRequestClient();
		System.out.println("Is source chagned: " +sourcedb.isChanged() +" AND " + "Is destination changed: " + destinationDB.isChanged());
		if(sourcedb != null && sourcedb.isChanged() ) {
		
			lhsDetail = new LHSDetail().buildDKDBConnectionInfo(sourcedb.getDatabaseName() ,DKDBFlavor.valueOf(sourcedb.getDatabaseName()), sourcedb.getDatabase(),
					sourcedb.getServer(), sourcedb.getPort(), sourcedb.getUsername(), sourcedb.getPassword());
			lhsDetail = lhsDetail.buildTableDetail(sourcedb.getTableInfo().getTableName(),sourcedb.getTableInfo().getKeyColumn(), sourcedb.getTableInfo().getRowId(), sourcedb.getTableInfo().getValue(), 
						sourcedb.getTableInfo().getColumnName());
			
		}
		if(destinationDB != null && destinationDB.isChanged()) {
		
			rhsDetail = new RHSDetail().buildDKDBConnectionInfo(destinationDB.getDatabaseName() ,DKDBFlavor.valueOf(destinationDB.getDatabaseName()), destinationDB.getDatabase(),
					destinationDB.getServer(), destinationDB.getPort(), destinationDB.getUsername(), destinationDB.getPassword());
			rhsDetail = rhsDetail.buildTableDetail(destinationDB.getTableInfo().getTableName(),destinationDB.getTableInfo().getKeyColumn(), destinationDB.getTableInfo().getRowId(), destinationDB.getTableInfo().getValue(), 
					destinationDB.getTableInfo().getColumnName());
			
		}
		requestClient.setLhsDBDetail(lhsDetail);
		requestClient.setRhsDBDetail(rhsDetail);
		return requestClient;
	}
	
	public String upload(MultipartFile file) throws IOException {

		try (InputStream fileInputStream = file.getInputStream();) {
			String fileName = file.getOriginalFilename();
			try {
				createFolderIfNotExists(UPLOAD_FOLDER);
			} catch (SecurityException se) {
				System.out.println("ERROR OCCURRED.--> Can not create destination folder on server");
				return null;
			}
			//String fileLoacation = UPLOAD_FOLDER + fileName;
			Path path = Paths.get(UPLOAD_FOLDER+fileName);
			try {
				saveToFile(fileInputStream, path.toString());
				return fileName;
			} catch (IOException e) {
				System.out.println("ERROR OCCURRED.--> while saving a file to destination folder.");
				return null;
			}
		}
	}
	
	protected DKResponse buildDKResponse(Map<String,String> responseValue, Exception ex) {
		DKResponse response = new DKResponse();
		
		if(ex != null) {
			response.setResponseStatus(new ResponseStatus("500","ERROR",ex.getMessage()));
			response.setSourceValue(null);
			response.setDestinationValue(null);
			return response;
		}
		
		response.setResponseStatus(new ResponseStatus("200","INFO","SUCCESS"));
		response.setSourceValue(responseValue.get("SRC_VALUE"));
		response.setDestinationValue(responseValue.get("DEST_VALUE"));
		
		return response;
	}

	protected DKColumnResponse buildDKColumnResponse(List<String> columns, Exception ex) {
		if(ex != null)
			return new DKColumnResponse(null,new ResponseStatus("500", "ERROR", ex.getMessage()));
		return new DKColumnResponse(columns,new ResponseStatus("200","INFO","SUCCESS"));
	}
	
	/**
	 * Utility method to save InputStream data to target location/file If file
	 * already exist with the filename then delete the old one first
	 * 
	 * @param inStream
	 *            - InputStream to be saved
	 * @param target
	 *            - full path to destination file
	 */
	private void saveToFile(InputStream inStream, String target) throws IOException {
		File file = new File(target);

		if (file.exists())
			file.delete();

		try (OutputStream out = new FileOutputStream(file)) {
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = inStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
		}
	}

	private void createFolderIfNotExists(String dirName) throws SecurityException {
		File theDir = new File(dirName);
		System.out.println("Creating Dir : " + dirName);
		if (!theDir.exists()) {
			theDir.mkdir();
		}

	}
}
