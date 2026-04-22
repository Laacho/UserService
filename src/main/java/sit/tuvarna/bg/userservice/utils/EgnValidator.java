package sit.tuvarna.bg.userservice.utils;


import lombok.extern.slf4j.Slf4j;
import sit.tuvarna.bg.userservice.aop.Loggable;

import java.time.LocalDate;
import java.time.Month;

/**
 * * EGN structure (10 digits): YYMMDDSSSC
 * * ─────────────────────────────────────────────
 * *  YY  — last two digits of birth year
 * *  MM  — birth month with century encoding:
 * *          01–12  → born 1900–1999
 * *          21–32  → born 1800–1899  (month = MM − 20)
 * *          41–52  → born 2000–2099  (month = MM − 40)
 * *  DD  — birth day
 * *  SSS — sequence number (odd = male, even = female)
 * *  C   — checksum digit  (weights: 2 4 8 5 10 9 7 3 6, mod 11; 10→0)
 */
@Slf4j
public class EgnValidator {

    private static final int[] WEIGHTS = {2, 4, 8, 5, 10, 9, 7, 3, 6};

    @Loggable
    public static EgnValidationResult validate(String egn) {
        if (egn == null) {
            log.error("egn is null");
            return EgnValidationResult.invalid("EGN cannot be null.");
        }
        String cleaned = egn.trim();
        if (!cleaned.matches("\\d{10}")) {
            log.error("Egn must be exactly 10 digits");
            return EgnValidationResult.invalid(
                    "EGN must be exactly 10 digits. Got: \"" + cleaned + "\".");
        }

        int[] d = new int[10];
        for (int i = 0; i < 10; i++) d[i] = cleaned.charAt(i) - '0';

        int yy = d[0] * 10 + d[1];
        int mm = d[2] * 10 + d[3];
        int dd = d[4] * 10 + d[5];

        int year,month;
        if(mm>=1 && mm<=12){
            year=1900+yy;
            month=mm;
        }
        else if(mm>=21 && mm<=32){
            year=1800+yy;
            month=mm-20;
        }
        else if(mm>=41 && mm<=52){
            year=2000+yy;
            month=mm-40;
        }
        else{
            log.error("Invalid month encoding");
            return EgnValidationResult.invalid(
                    String.format("Invalid month encoding %02d. Must be 01-12, 21-32, or 41-52.", mm));
        }

        LocalDate birthDate;
        try{
            birthDate = LocalDate.of(year, Month.of(month), dd);
        } catch (Exception e){
            log.error(e.getMessage());
            return EgnValidationResult.invalid(
                    String.format("Invalid calendar date: %04d-%02d-%02d.", year, month, dd));
        }

        if(birthDate.isAfter(LocalDate.now())){
            log.error("Birth date cannot be in the future.");
            return EgnValidationResult.invalid("Birth date cannot be in the future.");
        }
        int sequence=d[6]*100+d[7]*10+d[8];
        //todo for now this will do nothing, but can be used
        String gender= sequence%2==0 ? "female" : "male";


        int sum=0;
        for (int i=0;i<9;i++) sum+=d[i]*WEIGHTS[i];
        int expected=sum%11;
        if(expected==10) expected=0;
        if(d[9] != expected){
            log.error("Checksum mismatch: expected digit {}, got {}.", expected, d[9]);
            return EgnValidationResult.invalid(
                    String.format("Checksum mismatch: expected digit %d, got %d.", expected, d[9]));
        }

        return EgnValidationResult.valid(birthDate);
    }
}
