package org.teamswift.crow.rest.utils;

import lombok.Getter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

@SpringBootTest(
    classes = {
            EnumUtilsTest.class
    }
)
@RunWith(SpringRunner.class)
public class EnumUtilsTest {

    @Getter
    public enum TestEnum {
        Foo(1),
        Bar(2);

        private final int num;

        TestEnum(int i) {
            this.num = i;
        }
    }

    @Getter
    public enum TestEnum2 {
        Foo,
        Bar;
    }

    @Test
    public void testEnumToListMap() {
        List<Map<String, Object>> result = EnumUtils.enumToListMap(TestEnum.class);
        Assert.notEmpty(result, "Enum utils not work well.");

//        result = EnumUtils.enumToListMap(TestEnum2.class);
//        Assert.notEmpty(result, "Enum utils not work well.");
    }

}
