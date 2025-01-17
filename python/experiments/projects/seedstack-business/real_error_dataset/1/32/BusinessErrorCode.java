/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.business.internal;

import org.seedstack.shed.exception.ErrorCode;

public enum BusinessErrorCode implements ErrorCode {
    AMBIGUOUS_CONSTRUCTOR_FOUND, AMBIGUOUS_METHOD_FOUND, VALUE_CANNOT_BE_COMPARED, CLASS_IS_NOT_AN_ANNOTATION,
    CONFLICTING_DTO_FACTORY_INDEX_MATCHING, CONFLICTING_DTO_FACTORY_MATCHING, CONFLICTING_DTO_ID_INDEX_MATCHING,
    CONFLICTING_DTO_ID_MATCHING, CONFLICTING_DTO_TUPLE_MATCHING, DOMAIN_OBJECT_CONSTRUCTOR_NOT_FOUND,
    ERROR_ACCESSING_FIELD, EVENT_CYCLE_DETECTED,
    EXCEPTION_OCCURRED_DURING_EVENT_HANDLER_INVOCATION, INCOMPATIBLE_IDENTITY_TYPES, ILLEGAL_FACTORY,
    ILLEGAL_IDENTITY_GENERATOR, ILLEGAL_POLICY, ILLEGAL_REPOSITORY, ILLEGAL_SERVICE, NO_CONVERTER_FOUND,
    NO_IDENTITY_CAN_BE_RESOLVED_FROM_DTO, NO_IDENTITY_FIELD_DECLARED_FOR_ENTITY, NO_IDENTITY_GENERATOR_SPECIFIED,
    RESOLVED_DTO_ID_IS_INVALID, RESOLVED_DTO_ID_IS_NOT_PRODUCIBLE, UNABLE_TO_CREATE_TUPLE, UNABLE_TO_FIND_ASSEMBLER,
    UNABLE_TO_FIND_ASSEMBLER_WITH_QUALIFIER, UNABLE_TO_FIND_FACTORY_METHOD,
    UNABLE_TO_FIND_SUITABLE_DTO_INFO_RESOLVER, UNABLE_TO_INVOKE_CONSTRUCTOR, UNABLE_TO_INVOKE_FACTORY_METHOD,
    UNABLE_TO_CREATE_DEFAULT_IMPLEMENTATION, UNQUALIFIED_IDENTITY_GENERATOR, UNRESOLVED_FIELD
}
