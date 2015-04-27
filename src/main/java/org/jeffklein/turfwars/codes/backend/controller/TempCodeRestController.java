package org.jeffklein.turfwars.codes.backend.controller;

import org.jeffklein.turfwars.codes.dataaccess.model.TempCode;
import org.jeffklein.turfwars.codes.dataaccess.service.TempCodeService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            code.expirationDate = tempCode.getExpirationDate();
            codes.add(code);
        }
        model.put("codes", codes);
        return model;
    }

    private static class Code {
        public String code;
        public DateTime expirationDate;
    }
}
