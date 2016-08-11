package by.comrade.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoTypeMapper;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories
public class MongoConfig {

    @Bean
    public MongoClient mongoClient() {
        return new MongoClient();
    }

    @Bean
    public MongoDbFactory mongoDbFactory() {
        return new SimpleMongoDbFactory(mongoClient(), "comrade");
    }

    @Bean
    public MongoTemplate mongoTemplate(Mongo mongo) {
        MongoTypeMapper typeMapper = new DefaultMongoTypeMapper(null);
        //TODO fix using deprecated constructor
        MappingMongoConverter converter = new MappingMongoConverter(mongoDbFactory(), new MongoMappingContext());
        converter.setTypeMapper(typeMapper);

        return new MongoTemplate(mongoDbFactory(), converter);
    }
}
