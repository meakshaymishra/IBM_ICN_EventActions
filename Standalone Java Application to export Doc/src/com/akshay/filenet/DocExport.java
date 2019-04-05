package com.akshay.filenet;

import com.filenet.api.collection.FolderSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Document;
//import com.filenet.api.core.EngineObject;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.engine.EventActionHandler;
import com.filenet.api.events.ObjectChangeEvent;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ErrorRecord;
import com.filenet.api.exception.ExceptionCode;
import com.filenet.api.util.Id;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
//import java.nio.file.Paths;
//import java.util.Arrays;
//import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

//import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * 
 * 
 * 
 */

public class DocExport implements EventActionHandler {
	private static Logger logger = Logger.getLogger(DocExport.class);

	private static final class ConfigInfo {

		static String OUTPUT_ROOT_FOLDER;
		static String FILENET_PATH_TO_DISCARD;
		static String IICMSV2;
		static String INVESTIGATE_V4;
		static String IIV2;
		static String INVEST_AND_INTELLI_CMS;
		static String POC_INVESTIGATION;
	}

	private static final String PROP_FILE = "config.properties";

	public static boolean readPropertiesFile() {
		try {
			InputStream is = WADADocExport.class.getResourceAsStream(PROP_FILE);
			Properties prop = new Properties();
			prop.load(is);
			ConfigInfo.OUTPUT_ROOT_FOLDER = prop.getProperty("TargetPath");
			ConfigInfo.FILENET_PATH_TO_DISCARD = prop.getProperty("SourceFileNetPathToDiscard");
			ConfigInfo.IICMSV2 = prop.getProperty("IICMSV2");
			ConfigInfo.INVESTIGATE_V4 = prop.getProperty("INVESTIGATE_V4");
			ConfigInfo.IIV2 = prop.getProperty("IIV2");
			ConfigInfo.INVEST_AND_INTELLI_CMS = prop.getProperty("INVEST_AND_INTELLI_CMS");
			ConfigInfo.POC_INVESTIGATION = prop.getProperty("POC_INVESTIGATION");

			is.close();

			return true;

		} catch (Exception e) {
			return false;
		}
	}

	public WADADocExport() {
	}

	@SuppressWarnings("rawtypes")
	public void onEvent(ObjectChangeEvent event, Id subId) {
		String resource = "/com/akshay/filenet/log4jWADAExport.properties";

		URL configFileResource = WADADocExport.class.getResource(resource);
		PropertyConfigurator.configure(configFileResource);

		try {
			if (readPropertiesFile()) {
				logger.debug("SOURCE_FOLDER :: " + ConfigInfo.OUTPUT_ROOT_FOLDER);
			} else {
				logger.error("Failed to read from " + PROP_FILE
						+ " file. Please make sure the file exist and correct config info provided.");
			}
			ObjectStore os = event.getObjectStore();
			String PathName = "";
			String FolderName = "";
			os.refresh();
			

			Id id = event.get_SourceObjectId();
			Document doc = (Document) Factory.Document.fetchInstance(os, id, null);
			try {
				FolderSet folderset = doc.get_FoldersFiledIn();
				Iterator iter = folderset.iterator();
				Folder folder = null;
				while (iter.hasNext()) {
					folder = (Folder) iter.next();
				}
				/*
				 * FolderName = folder.get_FolderName().toString();
				 * logger.info("Folder NAme :: "+FolderName);
				 */
				PathName = folder.get_PathName().toString();
				logger.info("Folder Path Name :: " + PathName);
			} catch (Exception exp) {
				logger.info("Folder Fetch Exception :: " + exp.getMessage());
			}

			logger.info("Event received on: " + doc.get_Name());
			logger.info("Document Id: " + doc.get_Id().toString());
			// logger.info("Folder Path : "+PathName);
			if (doc.getProperties().getEngineObjectValue("CmAcmAssociatedCase") != null) {
				String caseType = "";
				String docClass = doc.get_ClassDescription().get_DisplayName();
				logger.debug("Document Class is :: " + docClass);
				String[] parts = PathName.split("[\\\\/]", -1);
				String CaseType = parts[5];
				logger.debug("Case Type is :: " + CaseType);
				String CaseNumber = "";

				logger.info("Length of parts array : " + parts.length);
				String[] Case_Path = {};
				String CasePath = "";
				// System.arraycopy(parts, 0, Case_Path, 0, 11);
				// Case_Path = Arrays.copyOf(parts, 11);
				try {
					for (int i = 0; i < 12; i++) {
						CasePath = CasePath + parts[i] + "\\";
						logger.info("Case Path is " + CasePath);
					}
				} catch (Exception e) {
					logger.info("For loop not working");
				}
				Folder folder = Factory.Folder.fetchInstance(os, CasePath, null);
				if (parts[3].contains("IICMS V2")) {
					CaseNumber = folder.getProperties().getStringValue(ConfigInfo.IICMSV2);
				} else if (parts[3].contains("IIV2")) {
					CaseNumber = folder.getProperties().getStringValue(ConfigInfo.IIV2);
				} else if (parts[3].contains("Investigate V4")) {
					CaseNumber = folder.getProperties().getStringValue(ConfigInfo.INVESTIGATE_V4);
				} else if (parts[3].contains("Investigation and Intelligence Case Management Solution")) {
					CaseNumber = folder.getProperties().getStringValue(ConfigInfo.INVEST_AND_INTELLI_CMS);
				} else if (parts[3].contains("POC Investigation Case Solution")) {
					CaseNumber = folder.getProperties().getStringValue(ConfigInfo.POC_INVESTIGATION);
				}

				logger.info("Case Number :: " + CaseNumber);

				/*
				 * try { //String CasePath = StringUtils.join(Case_Path, "\\"); String CasePath
				 * = String.join("\\", Case_Path); logger.info("Case Path :: " + CasePath);
				 * Folder folder = Factory.Folder.fetchInstance(os, CasePath, null); CaseNumber
				 * = folder.getProperties().getStringValue("IIV4_CaseNumber");
				 * 
				 * logger.info("Case Number :: " + CaseNumber); }catch (Exception ex) {
				 * logger.info("Error in Fetching Instance of folder "+ex.getMessage()); }
				 */
				String SubFolderPath = "";
				logger.info("Case Length :: " + parts.length);

				try {
					// String SubFolderPath = "";
					if (parts.length > 12) {
						logger.info("Case Sub" + parts[12]);
						// String[] Sub_Folder = Arrays.copyOfRange(parts, 12, parts.length);
						// SubFolderPath = StringUtils.join(Sub_Folder, "\\");
						logger.info("Inside the if Condition...");
						for (int i = 12; i <= (parts.length - 1); i++) {
							if ((parts.length - 1 - i) == 0) {
								SubFolderPath = SubFolderPath + parts[i];
							} else {
								SubFolderPath = SubFolderPath + parts[i] + "\\";
							}

						}
						logger.info("SubFolderPath Path :: " + SubFolderPath);
					}
				} catch (Exception ex) {
					logger.info("Error in Subfolder : " + ex.getMessage());
				}

				String finalOutputFolder = ConfigInfo.OUTPUT_ROOT_FOLDER;

				String fName = doc.getProperties().getStringValue("DocumentTitle");
				logger.info("Document Title :: " + fName);
				InputStream in = doc.accessContentStream(0);

				String destinationFolder = finalOutputFolder + "\\" + CaseType + "\\" + CaseNumber + "\\"
						+ SubFolderPath + "\\" + docClass;
				logger.info("Document Destination to be Created :: " + destinationFolder);
				/*
				 * File destDirectory; Path path; destDirectory = new File(destinationFolder);
				 * path = destDirectory.toPath(); Files.createDirectories(path.getParent());
				 */
				new File(destinationFolder).mkdirs();

				FileOutputStream fos = new FileOutputStream(destinationFolder + "\\" + fName);
				logger.info("File Output Stream Started");
				DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(fos));
				// outStream.write(in);

				byte[] buffer = new byte[4096];

				while (in.read(buffer) != -1) {
					outStream.write(buffer);
					logger.info("Writing Buffer");
				}
				outStream.close();
				logger.info("Closed OutStream");
				doc.save(RefreshMode.REFRESH);
				logger.info("Changes saved");
			}

		} catch (Exception e) {

			logger.error("Exception: " + e.getMessage());
			ErrorRecord er[] = { new ErrorRecord(e) };
			throw new EngineRuntimeException(e, ExceptionCode.EVENT_HANDLER_THREW, er);

		}
	}
}