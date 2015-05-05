package org.jeffklein.turfwars.codes.backend.scheduled;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static Log LOG = LogFactory.getLog(TempCodeApiPoller.class);

    /**
     * Cron expression is set to poll once per hour, on the hour.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void pollServerForTempCodes() {
        Set<TempCodeDTO> apiResponse = apiClient.getTempCodes();
        Set<TempCode> toPersist = this.copyTempCodesFromJsonResponse(apiResponse);
        Integer count = tempCodeService.saveTempCodeBatch(toPersist);
        LOG.info("Persisted "+count+" temp codes out of "+apiResponse.size()+" that were retrieved via the TurfWarsApiClient");
    }

    private Set<TempCode> copyTempCodesFromJsonResponse(Set<TempCodeDTO> jsonCodes) {
        Set<TempCode> dbCodes = new HashSet<TempCode>();
        for (TempCodeDTO jsonCode : jsonCodes) {
            TempCode dbCode = new TempCode();
            dbCode.setCode(jsonCode.getCode());
            dbCode.setExpirationDate(new DateTime(jsonCode.getExpirationDate()*1000));
            dbCode.setNextUpdateTimestamp(new DateTime(jsonCode.getNextUpdateTimestamp()*1000));
            dbCode.setServerTimestamp(new DateTime(jsonCode.getServerTimestamp()*1000));
            dbCodes.add(dbCode);
        }
        return dbCodes;
    }
}
