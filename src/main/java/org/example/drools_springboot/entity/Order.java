package org.example.drools_springboot.entity;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Order {
    private Double totalAmount;
    private String status;
}
