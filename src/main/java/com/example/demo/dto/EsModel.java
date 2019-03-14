package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @Title: EsModel实体
 * @Description:
 * @Author: chenx
 * @Date: 2019/3/14
 */
@Data
@ToString
@NoArgsConstructor
public class EsModel {
    private String id;
    private String name;
    private int age;
    private Date date;
}
