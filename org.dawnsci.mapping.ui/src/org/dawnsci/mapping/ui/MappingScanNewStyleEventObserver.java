package org.dawnsci.mapping.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.mapping.ui.datamodel.LiveDataBean;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPropertyFilter.FilterAction;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//FIXME This needs to be renamed once things settle down
public class MappingScanNewStyleEventObserver implements IScanListener {

	private static final Logger logger = LoggerFactory.getLogger(MappingScanNewStyleEventObserver.class);

	// TODO These constants would ideally be defined somewhere in the Dawn mapping UI code
	private static final String DAWNSCI_MAPPING_FILE_OPEN = "org/dawnsci/events/file/OPEN";
	private static final String DAWNSCI_MAPPING_FILE_CLOSE = "org/dawnsci/events/file/CLOSE";

	private IEventService eventService;
	private ISubscriber<IScanListener> subscriber;
	private EventAdmin eventAdmin;

	public void bindIEventService(IEventService eventService) {
		logger.debug("bindIEventService called with {}", eventService.toString());
		this.eventService = eventService;
	}

	public void unbindIEventService(IEventService eventService) {
		logger.debug("unbindIEventService called with {}", eventService.toString());
		if (eventService == this.eventService) {
			this.eventService = null;
		}
	}

	public void bindEventAdmin(EventAdmin eventAdmin) {
		logger.debug("bindEventAdmin called with {}", eventAdmin.toString());
		this.eventAdmin = eventAdmin;
	}

	public void unbindEventAdmin(EventAdmin eventAdmin) {
		logger.debug("unbindEventAdmin called with {}", eventAdmin.toString());
		if (eventAdmin == this.eventAdmin) {
			this.eventAdmin = null;
		}
	}

	public void start() {
		
		final String suri = CommandConstants.getScanningBrokerUri();
		if (suri==null) return; // Nothing to start, standard DAWN.

		logger.info("Starting the Mapping Scan Event Observer");

		// Check the service is available this should always be true!
		if (eventService == null) {
			logger.error("Tried to start Mapping Scan Event Observer but required services are not available");
			return;
		}

		try {
			final URI uri = new URI(suri);
			subscriber = eventService.createSubscriber(uri, EventConstants.STATUS_TOPIC);
			
			// We don't care about the scan request, removing it means that
			// all the points models and detector models to not have to resolve in
			// order to get the event.
			subscriber.addProperty("scanRequest", FilterAction.DELETE); 
			subscriber.addProperty("position", FilterAction.DELETE); 		            
			subscriber.addListener(this);
			
			logger.info("Created subscriber");
			
		} catch (URISyntaxException | EventException e) {
			logger.error("Could not subscribe to the event service", e);
		}
	}

	@Override
	public void scanEventPerformed(ScanEvent evt) {
		// Don't do anything
	}

	@Override
	public void scanStateChanged(ScanEvent event) {
		ScanBean beanNoScanReq = event.getBean();
		final String filePath = beanNoScanReq.getFilePath();
		// Scan started
		if (beanNoScanReq.scanStart() == true) {
			logger.info("Pushing data to live visualisation from SWMR file: {}", filePath);

			// Create the LiveDataBean
			LiveDataBean liveDataBean = new LiveDataBean();
			
			// Recent change to GDA means that its configuration may be read without
			// making a dependency on it.
			String dataServerHost = getDataServerHost();
			int dataServerPort    = getDataServerPort();

			// Configure the liveDataBean with a host and port to reach a dataserver
			liveDataBean.setHost(dataServerHost);
			// Default the port to -1 so it can be checked next. An Integer is unboxed here to int
			liveDataBean.setPort(dataServerPort);
			// Check the liveDataBean is valid
			if (liveDataBean.getHost() == null || liveDataBean.getPort() == -1) {
				logger.error("Live visualisation failed. The properties: {} or {} have not been set",
						"GDA/gda.dataserver.host", "GDA/gda.dataserver.port");
				// We can't do anything live at this point so return
				return;
			}

			// Create map holding the info needed to display the map
			Map<String, Object> eventMap = new HashMap<String, Object>();
			eventMap.put("path", filePath);
			eventMap.put("live_bean", liveDataBean);

			// Send the event
			eventAdmin.postEvent(new Event(DAWNSCI_MAPPING_FILE_OPEN, eventMap));
		}
		// Scan ended swap out remote SWMR file access for direct file access
		if (beanNoScanReq.scanEnd() == true) {
			logger.info("Switching from remote SWMR file to direct access: {}", filePath);
			Map<String, Object> eventMap = new HashMap<String, Object>();
			eventMap.put("path", filePath);
			// Close the old remote file
			eventAdmin.postEvent(new Event(DAWNSCI_MAPPING_FILE_CLOSE, eventMap));
			// Reopen the file
			eventAdmin.postEvent(new Event(DAWNSCI_MAPPING_FILE_OPEN, eventMap));
		}
	}


	// TODO put this in global place?
	private String getDataServerHost() {
		String name = System.getProperty("org.eclipse.dawnsci.data.server.host");
		if (name==null) name = System.getProperty("GDA/gda.dataserver.host");
		if (name==null) name = System.getProperty("gda.dataserver.host");
		return null;
	}

	// TODO put this in global place?
	private int getDataServerPort() {
		int port = Integer.getInteger("org.eclipse.dawnsci.data.server.port", -1);
		if (port<=0) port = Integer.getInteger("GDA/gda.dataserver.port", -1);
		if (port<=0) port = Integer.getInteger("gda.dataserver.port", -1);
		return port;
	}
}
