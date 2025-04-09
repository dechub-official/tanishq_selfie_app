/********************************************************************************
 * Copyright (c) 2017 Accel Frontline Ltd.
 * All rights reserved.
 *******************************************************************************/
package com.dechub.tanishq.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;


/**
 * The Class APIResponseBuilder.
 */
public class APIResponseBuilder {

    private static final Logger log = LoggerFactory.getLogger(APIResponseBuilder.class);

    /**
     * Builds the error response.
     *
     * @param message       the message
     * @param errorMessages the error messages
     * @param statusCode    the status code
     * @return the response entity
     */
    public static ResponseEntity<ResponseDataDTO> buildErrorResponse(final String message,
                                                                     final Map<String, String> errorMessages, final HttpStatus statusCode) {
        final ResponseDataDTO errorDto = APIResponseBuilder.buildResponse(message);
        errorDto.setResult(errorMessages);
        return new ResponseEntity<>(errorDto, statusCode);
    }

    /**
     * Builds the response.
     *
     * @param message the message
     * @return the response data DTO
     */
    public static ResponseDataDTO buildResponse(final String message) {
        final ResponseDataDTO dataDto = new ResponseDataDTO();
        dataDto.setMessage(message);
        return dataDto;
    }

    /**
     * Builds the success response.
     *
     * @param message the message
     * @param result  the result
     * @return the response entity
     */
    public static ResponseEntity<ResponseDataDTO> buildSuccessResponse(final String message, final Object result) {
        final ResponseDataDTO successDto = new ResponseDataDTO();
        successDto.setMessage(message);
        successDto.setResult(result);
        return new ResponseEntity<>(successDto, HttpStatus.OK);
    }

    /**
     * Builds the success response.
     *
     * @param
     * @param responseDataDTO the responseObj
     * @return the response entity
     * @author Kalidass.K
     */
    public static ResponseEntity<ResponseDataDTO> buildResponseFromDto(ResponseDataDTO responseDataDTO) {
        if (CommonConstants.SUCCESS_CONST.equalsIgnoreCase(responseDataDTO.getMessage()))
            return new ResponseEntity<>(responseDataDTO, HttpStatus.OK);
        else if (responseDataDTO.getMessage().contains(ErrorMessages.ERROR)|| responseDataDTO.getMessage().contains(ErrorMessages.ORA)) {
            log.error(CommonConstants.EXCEPTION_MESSAGE, responseDataDTO.getMessage());
            return APIResponseBuilder.buildErrorResponse(ErrorMessages.DATABASE_ERROR, null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else if (ErrorMessages.INTERNAL_SERVER_ERROR.equalsIgnoreCase(responseDataDTO.getMessage())) {
            return APIResponseBuilder.buildErrorResponse(ErrorMessages.INTERNAL_SERVER_ERROR_DESC, null, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return APIResponseBuilder.buildErrorResponse(responseDataDTO.getMessage(), null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }


    /**
     * Instantiates a new API response builder.
     */
    private APIResponseBuilder() {
        // Private Constructor
    }
}
