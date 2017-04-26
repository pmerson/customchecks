package com.sun.j2ee.blueprints.consumerwebsite.actions;

import javax.servlet.http.*;

import com.sun.j2ee.blueprints.consumerwebsite.AdventureComponentManager;
import com.sun.j2ee.blueprints.consumerwebsite.AdventureKeys;
import com.sun.j2ee.blueprints.consumerwebsite.CustomerBean;
import com.sun.j2ee.blueprints.consumerwebsite.exceptions.CustomerException;
import com.sun.j2ee.blueprints.customer.CustomerFacade;
import com.sun.j2ee.blueprints.signon.web.SignOnFilter;
import com.sun.j2ee.blueprints.waf.controller.web.html.HTMLActionException;

public final class CatalogHTMLAction {          // VIOLATION

    public Event perform(HttpServletRequest request) throws HTMLActionException {
        return null;
    }

}

class DummyHTMLAction extends HTMLActionSupport {     // NOT A VIOLATION

}
