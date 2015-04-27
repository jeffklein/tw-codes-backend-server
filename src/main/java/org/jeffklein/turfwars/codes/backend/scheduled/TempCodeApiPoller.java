package org.jeffklein.turfwars.codes.backend.scheduled;

import org.jeffklein.turfwars.codes.client.TempCodeDTO;
import org.jeffklein.turfwars.codes.client.TurfWarsApiClient;
import org.jeffklein.turfwars.codes.dataaccess.model.TempCode;
import org.jeffklein.turfwars.codes.dataaccess.service.TempCodeService;
import org.joda.time.DateTime;
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
    //TODO: this works exactly once then blows up with a Unique constraint violation. write a test case in the DAO module
    public void pollServerForTempCodes() {
        Set<TempCodeDTO> apiResponse = apiClient.getTempCodes();
        Set<TempCode> toPersist = this.copyTempCodesFromJsonResponse(apiResponse);
        tempCodeService.saveTempCodeBatch(toPersist);
    }

    // TODO: copying fields one at a time is for the birds. need annotation based DTO copying here...
    private Set<TempCode> copyTempCodesFromJsonResponse(Set<TempCodeDTO> jsonCodes) {
        Set<TempCode> dbCodes = new HashSet<TempCode>();
        for (TempCodeDTO jsonCode : jsonCodes) {
            TempCode dbCode = new TempCode();
            dbCode.setCode(jsonCode.getTempCode());
            dbCode.setExpirationDate(new DateTime(jsonCode.getExpirationDate()));
            dbCode.setNextUpdateTimestamp(new DateTime(jsonCode.getServerNextBatchAvailableDate()));
            dbCode.setServerTimestamp(new DateTime(jsonCode.getServerBatchPostedDate()));
            dbCodes.add(dbCode);
        }
        return dbCodes;
    }
}
