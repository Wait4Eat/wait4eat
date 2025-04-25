package com.example.wait4eat.domain.store.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.format.DateTimeFormatter;

@Document(indexName = "stores") // index 이름
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "korean_nori", searchAnalyzer = "korean_nori")
    private String name;

    @Field(type = FieldType.Text, analyzer = "korean_nori", searchAnalyzer = "korean_nori")
    private String address;

    @Field(type = FieldType.Text, analyzer = "korean_nori", searchAnalyzer = "korean_nori")
    private String description;

    @Field(type = FieldType.Keyword)
    private String openTime;

    @Field(type = FieldType.Keyword)
    private String closeTime;

    @Field(type = FieldType.Integer)
    private int depositAmount;

    @Field(type = FieldType.Integer)
    private int waitingTeamCount;

    public static StoreDocument from(Store store) {
        return StoreDocument.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .description(store.getDescription())
                .openTime(store.getOpenTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .closeTime(store.getCloseTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .depositAmount(store.getDepositAmount())
                .build();
    }
}

