package org.jeffklein.turfwars.codes.backend.controller;

import org.jeffklein.turfwars.codes.dataaccess.model.TempCode;
import org.jeffklein.turfwars.codes.dataaccess.service.TempCodeService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimePrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * TODO: javadoc needed
 *
 * @author jeffklein
 */
@RestController
public class TempCodeRestController {

    @Autowired
    private TempCodeService tempCodeService;

    @RequestMapping("/codes/temp/all")
    public Map<String, Object> home() {
        Map<String, Object> model = new HashMap<String, Object>();
        List<TempCode> tempCodes = tempCodeService.findAllTempCodes();
        List codes = new ArrayList();
        for (TempCode tempCode : tempCodes) {
            Code code = new Code();
            code.code = tempCode.getCode();
            code.expirationDateTimeMillis = tempCode.getExpirationDate().getMillis();
            DateTimeFormatter formatter = DateTimeFormat.forPattern("MM-dd-yyyy HH:mm:ss z").withZoneUTC();
            code.expirationDateTimeFormmattedUTC = tempCode.getExpirationDate().toString(formatter);
            codes.add(code);
        }
        model.put("temp_codes_size", codes.size());
        model.put("temp_codes", codes);
        return model;
    }

    private static class Code {
        public String code;
        public long expirationDateTimeMillis;
        public String expirationDateTimeFormmattedUTC;
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
            tempCode.setNextUpdateTimestamp(new DateTime(DateTimeZone.forID("UTC")));
            tempCode.setServerTimestamp(new DateTime(DateTimeZone.forID("UTC")));
            //System.out.println(codeStr + " || " + dateStr+" || "+timeStr+" || "+combineDateAndTime(dateStr, timeStr).toString());
            twukTemps.add(tempCode);
        }
        this.tempCodeService.saveTempCodeBatch(twukTemps);
        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }

    public static DateTime combineDateAndTime(String dateStr, String timeStr) {
        DateTime date = determineDate(dateStr);
        DateTime time = determineTime(timeStr);
        return new DateTime(DateTimeZone.forID("UTC")).withDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth())
                .withTime(time.getHourOfDay(),time.getMinuteOfHour(), time.getSecondOfMinute(), time.getMillisOfSecond());
    }

    public static void main(String[] args) {
        // TODO: turn this into a test
        String dateStr = "May 10";
        String timeStr = "01:44 PM";
        DateTime mergeDateTime = combineDateAndTime(dateStr, timeStr);
        System.out.println(mergeDateTime.toString());
    }
    public static DateTime determineDate(String dateStr) {
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