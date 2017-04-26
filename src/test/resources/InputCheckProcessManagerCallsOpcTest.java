package com.sun.j2ee.blueprints.processmanager.ejb;

import com.sun.j2ee.blueprints.opc.*;                // VIOLATION
import com.sun.j2ee.blueprints.opc.invoice.Invoice;  // VIOLATION
import com.sun.j2ee.blueprints.opc.mailer.*;         // VIOLATION
import static com.sun.j2ee.blueprints.opc.JNDINames; // VIOLATION 

import com.sun.j2ee.blueprints.opc.utils.*;                      // NON VIOLATION
import com.sun.j2ee.blueprints.opc.utils.InfraException;         // NON VIOLATION
import static com.sun.j2ee.blueprints.opc.utils.InfraException;  // NON VIOLATION

public class Dummmy {

    private ManagerLocalHome mlh;

    private com.sun.j2ee.blueprints.opc.invoice.Invoice invoice;    // VIOLATION

    public void createManager(String orderId, String status,
            String actyOrderStatus,
            String airlineOrderStatus,
            String lodgOrderStatus)
            throws CreateException {
        ManagerLocal manager = mlh.create(orderId, status, actyOrderStatus,
                airlineOrderStatus, lodgOrderStatus,
                false);
    }
}
