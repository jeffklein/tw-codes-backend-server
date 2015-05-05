package org.jeffklein.turfwars.codes.backend.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jeffklein.turfwars.codes.dataaccess.model.TempCode;
import org.jeffklein.turfwars.codes.dataaccess.service.TempCodeService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * TODO: javadoc needed
 *
 * @author jeffklein
 */
@RestController
public class TempCodeRestController {

    private static Log LOG = LogFactory.getLog(TempCodeRestController.class);

    @Autowired
    private TempCodeService tempCodeService;

    @RequestMapping("/codes/temp/all")
    public Map<String, Object> allTemps() {
        return this.createModelFromTempCodeList(tempCodeService.findAllTempCodes());
    }

    @RequestMapping("/codes/temp/valid")
    public Map<String, Object> validTemps() {
        return this.createModelFromTempCodeList(tempCodeService.findAllUnexpiredTempCodes());
    }

    private Map<String, Object> createModelFromTempCodeList(List<TempCode> tempCodes) {
        Map<String, Object> model = new HashMap<String, Object>();
        List codes = new ArrayList();
        for (TempCode tempCode : tempCodes) {
            Code code = new Code();
            code.code = tempCode.getCode();
            code.expiresMillis = tempCode.getExpirationDate().getMillis();
            DateTimeFormatter formatter = DateTimeFormat.forPattern("MM-dd-yyyy HH:mm:ss z").withZoneUTC();
            code.expiresFormatted = tempCode.getExpirationDate().toString(formatter);
            codes.add(code);
        }
        model.put("temp_codes_size", codes.size());
        model.put("temp_codes", codes);
        return model;
    }

    private static class Code {
        public String code;
        public long expiresMillis;
        public String expiresFormatted;
    }

    @RequestMapping(
            value = "/codes/temp/add",
            consumes = "application/json",
            method = {RequestMethod.POST, RequestMethod.PUT}
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> twUkTempCodeWebHook(@RequestBody LinkedHashMap<String, Object> inputMap) {
        Set<TempCode> twukTemps = new HashSet<TempCode>();
        LinkedHashMap<String, Object> resultsMap= (LinkedHashMap<String, Object>)inputMap.get("results");
        ArrayList<LinkedHashMap<String, Object>> codesList = (ArrayList<LinkedHashMap<String, Object>>) resultsMap.get("codes");
        for(LinkedHashMap<String, Object> code : codesList) {
            TempCode tempCode = new TempCode();
            String codeStr = (String)code.get("code");
            tempCode.setCode(codeStr);

            String expiresRaw = (String) code.get("expiresraw"); //Expires: May 09 ~ 05:42 AM
            String dateTimeRaw = expiresRaw.split("Expires: ")[1];
            String[] dateTimeRawSplit = dateTimeRaw.split(" ~ ");
            String dateStr = dateTimeRawSplit[0]; // May 10
            String timeStr = dateTimeRawSplit[1]; // 01:44 PM
            tempCode.setExpirationDate(combineDateAndTime(dateStr, timeStr));
            //TODO: parse next update and server timestamps out of the kimono labs JSON
            tempCode.setNextUpdateTimestamp(new DateTime(DateTimeZone.forID("UTC")));
            tempCode.setServerTimestamp(new DateTime(DateTimeZone.forID("UTC")));
            twukTemps.add(tempCode);
        }
        Integer count = (Integer)inputMap.get("count");
        Assert.isTrue(count == twukTemps.size(), "count from json: "+count+". twuktempsize: "+twukTemps.size());
        Integer numPersisted = this.tempCodeService.saveTempCodeBatch(twukTemps);
        LOG.info("Persisted "+numPersisted+" out of "+twukTemps.size()+" temp codes from Kimono Labs TW-UK webhook.");
        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }

    public static DateTime combineDateAndTime(String dateStr, String timeStr) {
        DateTime date = determineDate(dateStr);
        DateTime time = determineTime(timeStr);
        return new DateTime(DateTimeZone.forID("UTC")).withDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth())
                .withTime(time.getHourOfDay(),time.getMinuteOfHour(), time.getSecondOfMinute(), time.getMillisOfSecond());
    }

    private static DateTime determineDate(String dateStr) {
        dateStr += " UTC";
        DateTime partialDate = DateTime.parse(dateStr, DateTimeFormat.forPattern("MMM dd zzz"));
        DateTime currDate = new DateTime(DateTimeZone.forID("UTC"));
        int currYear = currDate.getYear();
        if (partialDate.getMonthOfYear() == 1 && currDate.getMonthOfYear() == 12) { // this is Dec, but code expires in Jan
            partialDate = new DateTime(DateTimeZone.forID("UTC")).withDate(currYear+1, partialDate.getMonthOfYear(), partialDate.getDayOfMonth()).withTime(0,0,0,0);
        }
        else {
            partialDate = new DateTime(DateTimeZone.forID("UTC")).withDate(currYear, partialDate.getMonthOfYear(), partialDate.getDayOfMonth()).withTime(0,0,0,0);
        }
        return partialDate;
    }
    private static DateTime determineTime(String timeStr) {
        timeStr += " UTC";
        DateTime time = DateTime.parse(timeStr, DateTimeFormat.forPattern("hh:mm aa zzz"));
        return time;
    }
}