package com.wsu.auth.util;



import java.util.regex.Pattern;

public class UserNameValidator {

    private static final Pattern STUDENT_ID_PATTERN =
            Pattern.compile("^[A-Z]{2,4}/\\d{4,6}/\\d{2}$");

    private static final Pattern STAFF_ID_PATTERN =
            Pattern.compile("^[A-Z]{2,4}/\\d{2,5}$");   // confirm real format with HR/registrar

    public static boolean isValidStudentId(String id) {
        return id != null && STUDENT_ID_PATTERN.matcher(id).matches();
    }

    public static boolean isValidStaffId(String id) {
        return id != null && STAFF_ID_PATTERN.matcher(id).matches();
    }
}