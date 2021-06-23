package org.folio.exception;

import org.folio.rest.jaxrs.model.Error;

public enum ErrorCodes {

  GENERIC_ERROR_CODE("genericError", "Generic error"),
  MISMATCH_BETWEEN_ID_IN_PATH_AND_BODY("idMismatch", "Mismatch between id in path and request body"),
  ORGANIZATION_UNITS_NOT_FOUND("organizationAcqUnitsNotFound", "Acquisitions units assigned to organization cannot be found"),
  USER_HAS_NO_ACQ_PERMISSIONS("userHasNoAcqUnitsPermission", "User does not have permissions to manage acquisition units assignments - operation is restricted"),
  USER_HAS_NO_PERMISSIONS("userHasNoPermission", "User does not have permissions - operation is restricted");

  private final String code;
  private final String description;

  ErrorCodes(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public String getCode() {
    return code;
  }

  @Override
  public String toString() {
    return code + ": " + description;
  }

  public Error toError() {
    return new Error().withCode(code)
      .withMessage(description);
  }
}
