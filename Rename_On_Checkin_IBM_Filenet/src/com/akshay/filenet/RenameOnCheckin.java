package com.akshay.filenet;

import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.engine.EventActionHandler;
import com.filenet.api.events.ObjectChangeEvent;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ErrorRecord;
import com.filenet.api.exception.ExceptionCode;
import com.filenet.api.util.Id;

import java.net.URL;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class RenameOnCheckin implements EventActionHandler {
	private static Logger logger = Logger.getLogger(RenameOnCheckin.class);

	public RenameOnCheckin() {
	}

	@SuppressWarnings("rawtypes")
	public void onEvent(ObjectChangeEvent event, Id subId) {
		String resource = "/com/akshay/filenet/log4jEventAction.properties";
		URL configFileResource = RenameOnCheckin.class.getResource(resource);
		PropertyConfigurator.configure(configFileResource);

		try {

			ObjectStore os = event.getObjectStore();

			// You may not be able to get properties if you don't do a refresh
			os.refresh();

			Id id = event.get_SourceObjectId();
			Document doc = (Document) Factory.Document.fetchInstance(os, id, null);
			logger.info("Event received on: " + doc.get_Name());
			logger.debug("Document Id: " + doc.get_Id().toString());

			// Set Description as the MWCCDescription provided
			if (doc.getClassName().equalsIgnoreCase("TestDocument")) {
				doc.getProperties().putValue("DocumentTitle", "Document - " + new Date());
			}

			doc.save(RefreshMode.REFRESH);
			logger.debug("Changes saved");

		} catch (Exception e) {

			logger.error("Exception: " + e.getMessage());
			ErrorRecord er[] = { new ErrorRecord(e) };
			throw new EngineRuntimeException(e, ExceptionCode.EVENT_HANDLER_THREW, er);

		}
	}
}
