package io.github.cbarlin.aru.tests.c_deeply_nested_structure;

import jakarta.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({ThirdLevelAFromA.class, ThirdLevelBFromA.class, ThirdLevelCFromA.class})
public interface ThirdLevelInterface
{

}
