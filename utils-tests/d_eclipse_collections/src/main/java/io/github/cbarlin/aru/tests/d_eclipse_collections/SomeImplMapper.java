package io.github.cbarlin.aru.tests.d_eclipse_collections;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SomeImplMapper {

    SomeImplMapper INSTANCE = Mappers.getMapper( SomeImplMapper.class );

    @Mapping(source = "field", target = "anotherField")
    SomeImplB fromA(final SomeImplA someImplA);

}
