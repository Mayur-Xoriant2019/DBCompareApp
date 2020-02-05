package com.xoriant.diff.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.Produces;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.diffkit.db.DKDBConnectionInfo;
import org.diffkit.db.DKDBFlavor;
import org.diffkit.diff.conf.DiffKitDAO;
import org.diffkit.model.DKRequestClient;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.xoriant.diff.core.CoreServiceProvider;
import com.xoriant.diff.model.DKColumnRequest;
import com.xoriant.diff.model.DKColumnResponse;
import com.xoriant.diff.model.DKRequest;
import com.xoriant.diff.model.DKResponse;
import com.xoriant.diff.model.ResponseStatus;
import com.xoriant.diff.model.Test;

@Controller
public class DiffServiceController extends CoreServiceProvider{

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String hello(Locale locale, Model model) {
		return "upload";
	}

	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> login(@RequestBody Test test) {

		System.out.println(test);

		return new ResponseEntity<String>("Login Success.",HttpStatus.OK);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<DKResponse> dbInfo(@RequestBody DKRequest request) {
		Map<String,String> responeValue = new HashedMap();
		System.out.println("update db: -----> " +request.toString());
		//call to dkService for db update
		DKRequestClient requestClient = buildDiffKitRequest(request);
		System.out.println("DKRequestClient ---> " +requestClient);
		try {
			responeValue = DiffKitDAO.update(requestClient);
			
		} catch (SQLException e) {
			System.out.println("Catch Block....");
			//e.printStackTrace();
			return new ResponseEntity<DKResponse>(buildDKResponse(null, e),HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<DKResponse>(buildDKResponse(responeValue, null),HttpStatus.OK);
	}
	
	@RequestMapping(value = "/getColumn", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<DKColumnResponse> getColumn(@RequestBody DKColumnRequest colRequest) {
		
		if(!colRequest.notNull()) {
			return new ResponseEntity<DKColumnResponse>(new DKColumnResponse(null,new ResponseStatus("400", "ERROR", "Request field must not be empty.")),HttpStatus.BAD_REQUEST);
		}
		try {
			DKDBConnectionInfo connectionDtl = new DKDBConnectionInfo(colRequest.getDatabase(), DKDBFlavor.valueOf(colRequest.getDatabaseFlavor()), colRequest.getDatabase(), colRequest.getServer(),
					colRequest.getPort(), colRequest.getUsername(), colRequest.getPassword());
			List<String> columns = DiffKitDAO.getColumns(connectionDtl,colRequest.getTableName());
			System.out.println("Column List: ---> " +columns);
			return new ResponseEntity<DKColumnResponse>(buildDKColumnResponse(columns, null),HttpStatus.OK);
		} catch (Exception e) {
			System.out.println("Catch Block....");	
			return new ResponseEntity<DKColumnResponse>(buildDKColumnResponse(null,e),HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		//return new ResponseEntity<DKColumnResponse>(buildDKColumnResponse(dkColumnResponse,null),HttpStatus.OK);
	}
	

	public static void main(String args[]) {
		// System.out.println(DiffServiceController.invokeDiffByPassingFileName(null,
		// null));

	}

	@RequestMapping(value = "/runplan", method = RequestMethod.POST)
	@Produces(javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM)
	public ResponseEntity<InputStreamResource> uploadAnddRunPlan(@RequestParam("file") MultipartFile file) {
		String resposeFileName = null;
		try {
			String inputFile = upload(file);
			if (inputFile != null && !inputFile.isEmpty()) {
				try {
					resposeFileName = execute(inputFile);
				} catch (Throwable e1) {
					System.out
							.println("Exception occurred while calling uploadAnddRunPlan: -------> " + e1.getMessage());
					e1.printStackTrace();
				}
				//File outFile = new File(DOWNLOAD_FOLDER + resposeFileName);
				System.out.println("Response file Location: ---> "+ resposeFileName);
				File outFile = new File(resposeFileName);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.parseMediaType("application/octet-stream"));
				String keyValue = String.format("attachment; filename=\"%s\"", outFile.getName());
				headers.add("Content-Disposition", keyValue);
				headers.setContentLength(outFile.length());
				ResponseEntity<InputStreamResource> response = null;

				try {
					response = new ResponseEntity<>(new InputStreamResource(new FileInputStream(outFile)), headers,
							HttpStatus.OK);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				return response;
			}else {
				System.out.println("File name can not be empty." + resposeFileName);
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/runplanold", method = RequestMethod.POST)
	public ResponseEntity<byte[]> uploadFileAndRunPlan(@RequestParam("file") MultipartFile file) throws Throwable {

		try {
			String fileName = upload(file);
			if (!fileName.isEmpty()) {
				String resposeFileName = execute(fileName);
				System.out.println("File Processed successfully." + resposeFileName);
				File outFile = new File(DOWNLOAD_FOLDER + resposeFileName);
				InputStream ins = new FileInputStream(outFile);

				return new ResponseEntity<>(IOUtils.toByteArray(ins), HttpStatus.OK);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/getfile", method = RequestMethod.GET)
	@Produces(javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM)
	public ResponseEntity<InputStreamResource> getFile() {

		File outFile = new File(DOWNLOAD_FOLDER + "citydiff.sink.diff");
		// InputStream ins = new FileInputStream(outFile);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/octet-stream"));
		String keyValue = String.format("attachment; filename=\"%s\"", outFile.getName());
		headers.add("Content-Disposition", keyValue);
		headers.setContentLength(outFile.length());
		ResponseEntity<InputStreamResource> response = null;

		try {
			response = new ResponseEntity<>(new InputStreamResource(new FileInputStream(outFile)), headers,
					HttpStatus.OK);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return response;
	}

	@RequestMapping(value = "/fileupload", method = RequestMethod.POST)
	public String uploadFile(@RequestParam("file") MultipartFile file) {

		try {
			return upload(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	

	@RequestMapping(value = "/downloadTemplate", method = RequestMethod.GET)
	@Produces(javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM)
	public ResponseEntity<InputStreamResource> download() throws IOException {

		File outFile = new File(DOWNLOAD_FOLDER + "customers.csv");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/octet-stream"));
		String keyValue = String.format("attachment; filename=\"%s\"", outFile.getName());
		headers.add("Content-Disposition", keyValue);
		headers.setContentLength(outFile.length());
		ResponseEntity<InputStreamResource> response = null;

		try {
			response = new ResponseEntity<>(new InputStreamResource(new FileInputStream(outFile)), headers,
					HttpStatus.OK);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return response;
	}


	//Moved to DiffKitServiceClient.java
	/*private DKRequestClient buildDiffKitRequest(DKRequest request) {
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
	}*/

	//Moved to DiffKitServiceClient
	/*public String execute(String fileName) throws Throwable {

		DKApplication dkApp = new DKApplication();
		System.out.println("Executing plan: " + UPLOAD_FOLDER + fileName);
		String responseFileName = dkApp.runDiffKit(UPLOAD_FOLDER + fileName);
		if (responseFileName != null) // "testSqlServer.plan.xml"
			return responseFileName;

		return "Configuration Error";
	}*/
	


}
