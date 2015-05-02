package org.jeffklein.turfwars.codes.backend.controller;

import org.jeffklein.turfwars.codes.dataaccess.model.TempCode;
import org.jeffklein.turfwars.codes.dataaccess.service.TempCodeService;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
    public ResponseEntity<?> tempCodeWebHook(@RequestBody Object input) {
        LinkedHashMap<String, Object> inputMap = (LinkedHashMap<String, Object>) input;
        LinkedHashMap<String, Object> resultsMap= (LinkedHashMap<String, Object>)inputMap.get("results");
        ArrayList<LinkedHashMap<String, Object>> codesList = (ArrayList<LinkedHashMap<String, Object>>) resultsMap.get("codes");
        for(LinkedHashMap<String, Object> code : codesList) {
            System.out.println(code.get("code") + ": " + code.get("expiresraw"));
        }
        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }
}
