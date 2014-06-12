package com.citytechinc.cq.clientlibs.core.listeners.library.impl;

import com.citytechinc.cq.clientlibs.api.domain.library.ClientLibrary;
import com.citytechinc.cq.clientlibs.api.events.library.ClientLibraryEvent;
import com.citytechinc.cq.clientlibs.api.events.library.factory.ClientLibraryEventFactory;
import com.citytechinc.cq.clientlibs.api.services.clientlibs.ClientLibraryManager;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClientLibraryEventListener implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(ClientLibraryEventListener.class);

    private final ClientLibraryEventFactory clientLibraryEventFactory;
    private final ClientLibraryManager clientLibraryManager;
    private final Session session;

    public ClientLibraryEventListener(ClientLibraryEventFactory clientLibraryEventFactory, ClientLibraryManager clientLibraryManager, Session session) {
        this.clientLibraryEventFactory = clientLibraryEventFactory;
        this.clientLibraryManager = clientLibraryManager;
        this.session = session;
    }

    @Override
    public void onEvent(EventIterator events) {

        List<ClientLibraryEvent> clientLibraryEventList = Lists.newArrayList();

        Set<ClientLibrary> clientLibrarySetSnapshot = clientLibraryManager.getLibraries();
        Map<String, ClientLibrary> clientLibraryByPathMap = Maps.newHashMap();

        for (ClientLibrary currentClientLibrary : clientLibrarySetSnapshot) {
            clientLibraryByPathMap.put(currentClientLibrary.getClientLibraryPath(), currentClientLibrary);
        }

        while (events.hasNext()) {
            Event currentEvent = events.nextEvent();

            try {

                Optional<ClientLibraryEvent> clientLibraryEventOptional =
                        clientLibraryEventFactory.make(currentEvent, clientLibraryByPathMap, session);

                if (clientLibraryEventOptional.isPresent()) {
                    clientLibraryEventList.add(clientLibraryEventOptional.get());
                }

            } catch (RepositoryException e) {
                LOG.error("Repository Exception encountered while processing potential Client Library events", e);
            }


        }

        if (clientLibraryEventList.size() > 0) {
            clientLibraryManager.requestRefresh();
        }

    }

}
