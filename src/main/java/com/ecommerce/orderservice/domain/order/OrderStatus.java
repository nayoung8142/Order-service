package com.ecommerce.orderservice.domain.order;

public enum OrderStatus {

    SUCCEEDED,
    FAILED,
    WAITING,
    CANCELED,
    OUT_OF_STOCK,
    NOT_EXIST,
    SERVER_ERROR,
    BAD_REQUEST
}
