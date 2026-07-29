package com.chatsito.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoConfiguration {
    @Bean
    MappingMongoConverter mappingMongoConverter(
            MongoDatabaseFactory databaseFactory,
            MongoMappingContext mappingContext) {
        var converter = new MappingMongoConverter(
                new DefaultDbRefResolver(databaseFactory), mappingContext);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }
}
