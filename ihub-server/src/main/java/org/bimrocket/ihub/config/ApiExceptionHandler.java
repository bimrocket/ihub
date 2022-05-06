/*
 * BIMROCKET
 *
 * Copyright (C) 2022, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.bimrocket.ihub.config;

import org.bimrocket.ihub.dto.ApiError;
import org.bimrocket.ihub.exceptions.InvalidSetupException;
import org.bimrocket.ihub.exceptions.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 *
 * @author realor
 */
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler
{

  @ExceptionHandler(InvalidSetupException.class)
  protected ResponseEntity<ApiError> exceptionHandle(InvalidSetupException ex,
    WebRequest request)
  {
    return new ResponseEntity<>(
      new ApiError(ex.getErrorCode(), BAD_REQUEST, ex.getMessage()),
      BAD_REQUEST);
  }

  @ExceptionHandler(NotFoundException.class)
  protected ResponseEntity<ApiError> exceptionHandle(NotFoundException ex,
    WebRequest request)
  {
    return new ResponseEntity<>(
      new ApiError(ex.getErrorCode(), NOT_FOUND, ex.getMessage()), NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ApiError> exceptionHandle(Exception ex,
    WebRequest request)
  {
    String message = ex.getMessage();
    if (message == null)
    {
      message = ex.toString();
    }

    return new ResponseEntity<>(new ApiError(0, INTERNAL_SERVER_ERROR, message),
      INTERNAL_SERVER_ERROR);
  }
}
