package org.jeffklein.turfwars.codes.backend.scheduled;

import org.jeffklein.turfwars.codes.client.TempCodeApiJsonResponse;
import org.jeffklein.turfwars.codes.client.TurfWarsApiClient;
import org.jeffklein.turfwars.codes.dataaccess.model.TempCode;
import org.jeffklein.turfwars.codes.dataaccess.model.TempCodeApiResponse;
import org.jeffklein.turfwars.codes.dataaccess.service.TempCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Class that will poll the Temp Code API server for new codes.
 */
@Component
public class TempCodeApiPoller {

    @Autowired
    private TurfWarsApiClient apiClient;

    @Autowired
    private TempCodeService tempCodeService;

    @Scheduled(fixedRate = 20000)
    public void pollServerForTempCodes() {
        TempCodeApiJsonResponse apiResponse = apiClient.getJsonResponse();
        TempCodeApiResponse toPersist = this.copyTempCodesFromJsonResponse(apiResponse.getTempCodes(), new TempCodeApiResponse());
        toPersist.setTimestamp(apiResponse.getTimestamp());
        toPersist.setNextUpdate(apiResponse.getNextUpdate());
        tempCodeService.saveTempCodeApiResponse(toPersist);
    }

    private TempCodeApiResponse copyTempCodesFromJsonResponse(Set<org.jeffklein.turfwars.codes.client.TempCode> jsonCodes,
                                                        TempCodeApiResponse apiResponse) {
        Set<TempCode> dbCodes = new HashSet<TempCode>();
        for (org.jeffklein.turfwars.codes.client.TempCode jsonCode : jsonCodes) {
            TempCode dbCode = new TempCode();
            dbCode.setCode(jsonCode.getCode());
            dbCode.setExpires(jsonCode.getExpires());
            dbCode.setTempCodeApiResponse(apiResponse);
            dbCodes.add(dbCode);
        }
        apiResponse.setTempCodes(dbCodes);
        return apiResponse;
    }
}
