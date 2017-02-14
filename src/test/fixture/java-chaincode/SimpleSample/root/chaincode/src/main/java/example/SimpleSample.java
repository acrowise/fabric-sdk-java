/*
Copyright DTCC 2016 All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.protos.peer.FabricProposalResponse.Response;
import org.hyperledger.fabric.sdk.shim.ChaincodeBase;
import org.hyperledger.fabric.sdk.shim.ChaincodeResponseHelper;
import org.hyperledger.fabric.sdk.shim.ChaincodeStub;

/**
 * <h1>Classic "transfer" sample chaincode</h1>
 * (java implementation of <A href="https://github.com/hyperledger/fabric/blob/master/examples/chaincode/go/chaincode_example02/chaincode_example02.go">chaincode_example02.go</A>)
 *
 * @author Sergey Pomytkin spomytkin@gmail.com
 */
public class SimpleSample extends ChaincodeBase {
    private static Log log = LogFactory.getLog(SimpleSample.class);

    @Override
    public Response invoke(ChaincodeStub stub, String function, String[] args) {
        log.info("In run, function:" + function);

        switch (function) {
            case "init":
                return init(stub, function, args);
            case "transfer":
                return transfer(stub, args);
            case "put":
                for (int i = 0; i < args.length; i += 2)
                    stub.putState(args[i], args[i + 1]);
                break;
            case "del":
                for (String arg : args)
                    stub.delState(arg);
                break;
            case "query":
                return query(stub, function, args);
            default:
                return transfer(stub, args);
        }

        return null;
    }

    private Response transfer(ChaincodeStub stub, String[] args) {
        System.out.println("in transfer");
        if (args.length != 3) {
            log.error("Incorrect number of arguments:" + args.length);
            return ChaincodeResponseHelper.error("{\"Error\":\"Incorrect number of arguments. Expecting 3: from, to, amount\"}");
        }
        String fromName = args[0];
        String fromAm = stub.getState(fromName);
        String toName = args[1];
        String toAm = stub.getState(toName);
        String am = args[2];
        int valFrom = 0;
        if (fromAm != null && !fromAm.isEmpty()) {
            try {
                valFrom = Integer.parseInt(fromAm);
            } catch (NumberFormatException e) {
                log.error("{\"Error\":\"Expecting integer value for asset holding of " + fromName + " \"}" + e);
                return ChaincodeResponseHelper.error("{\"Error\":\"Expecting integer value for asset holding of " + fromName + " \"}");
            }
        } else {
            return ChaincodeResponseHelper.error("{\"Error\":\"Failed to get state for " + fromName + "\"}");
        }

        int valTo = 0;
        if (toAm != null && !toAm.isEmpty()) {
            try {
                valTo = Integer.parseInt(toAm);
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
                return ChaincodeResponseHelper.error("{\"Error\":\"Expecting integer value for asset holding of " + toName + " \"}");
            }
        } else {
            return ChaincodeResponseHelper.error("{\"Error\":\"Failed to get state for " + toName + "\"}");
        }

        int valA = 0;
        try {
            valA = Integer.parseInt(am);
        } catch (NumberFormatException e) {
            log.error(e.getMessage());
            return ChaincodeResponseHelper.error("{\"Error\":\"Expecting integer value for amount \"}");
        }
        if (valA > valFrom)
            return ChaincodeResponseHelper.error("{\"Error\":\"Insufficient asset holding value for requested transfer amount \"}");
        valFrom = valFrom - valA;
        valTo = valTo + valA;
        log.info("Transfer " + fromName + ">" + toName + " am='" + am + "' new values='" + valFrom + "','" + valTo + "'");
        stub.putState(fromName, "" + valFrom);
        stub.putState(toName, "" + valTo);
        log.info("Transfer complete");
        return ChaincodeResponseHelper.success("Transfer successful");
    }

    public Response query(ChaincodeStub stub, String function, String[] args) {
        if (args.length != 1) {
            return ChaincodeResponseHelper.error("{\"Error\":\"Incorrect number of arguments. Expecting name of the person to query\"}");
        }
        String am = stub.getState(args[0]);
        if (am != null && !am.isEmpty()) {
            try {
                int valA = Integer.parseInt(am);
		System.out.println(am);
                return ChaincodeResponseHelper.success("{\"Name\":\"" + args[0] + "\",\"Amount\":\"" + am + "\"}");
	
            } catch (NumberFormatException e) {
                return ChaincodeResponseHelper.error("{\"Error\":\"Expecting integer value for asset holding\"}");
            }
        } else {
            return ChaincodeResponseHelper.error("{\"Error\":\"Failed to get state for " + args[0] + "\"}");
        }
    }


    public Response init(ChaincodeStub stub, String function, String[] args) {
        if (args.length != 4) {
            return ChaincodeResponseHelper.error("{\"Error\":\"Incorrect number of arguments. Expecting 4\"}");
        }
        try {
            int valA = Integer.parseInt(args[1]);
            int valB = Integer.parseInt(args[3]);
            stub.putState(args[0], args[1]);
            stub.putState(args[2], args[3]);
        } catch (NumberFormatException e) {
            return ChaincodeResponseHelper.error("{\"Error\":\"Expecting integer value for asset holding\"}");
        }
        return ChaincodeResponseHelper.success("Init Successful");
    }


    @Override
    public String getChaincodeID() {
        return "SimpleSample:0/testchainid";
    }

    public static void main(String[] args) throws Exception {
        new SimpleSample().start(args);
    }

}