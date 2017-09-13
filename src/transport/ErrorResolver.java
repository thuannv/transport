/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transport;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author steven
 */
public final class ErrorResolver {
    
    private static final int ERROR_CODE_TOKEN_EXPIRE = 100;
    
    private static final int ERROR_CODE_INVALID_TOKEN = 101;
            
    private static final int ERROR_CODE_SERVER_ERROR = 102;
    
    private static final Map<Integer, String> ERRORS = new HashMap<>();
    
    static {
        ERRORS.put(ERROR_CODE_TOKEN_EXPIRE, "Token expired");
        ERRORS.put(ERROR_CODE_INVALID_TOKEN, "Invalid token");
        ERRORS.put(ERROR_CODE_SERVER_ERROR, "Server error");
    }
    
    private ErrorResolver() {}

    public static String resolve(int erroCode) {
        String error = ERRORS.get(erroCode);
        return error == null ? "Unknown error" : error;
    }
    
    public static boolean isServerError(int code) {
        return ERROR_CODE_SERVER_ERROR == code;
    }
    
    public static boolean isTokenExpired(int code) {
        return ERROR_CODE_TOKEN_EXPIRE == code;
    }
    
    public static boolean isInvalidToken(int code) {
        return ERROR_CODE_INVALID_TOKEN == code;
    }
}
